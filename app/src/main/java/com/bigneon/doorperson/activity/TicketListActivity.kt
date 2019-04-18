package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.bigneon.doorperson.adapter.OnItemClickListener
import com.bigneon.doorperson.adapter.TicketListAdapter
import com.bigneon.doorperson.adapter.addOnItemClickListener
import com.bigneon.doorperson.controller.RecyclerItemTouchHelper
import com.bigneon.doorperson.db.SyncController
import com.bigneon.doorperson.db.SyncController.Companion.ticketListItemOffset
import com.bigneon.doorperson.db.SyncController.Companion.ticketListItemPosition
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.model.TicketModel
import kotlinx.android.synthetic.main.activity_ticket_list.*
import kotlinx.android.synthetic.main.content_ticket_list.*
import kotlinx.android.synthetic.main.content_ticket_list.view.*

class TicketListActivity : AppCompatActivity(), ITicketListRefresher {
    private var eventId: String? = null
    private val recyclerItemTouchHelper: RecyclerItemTouchHelper = RecyclerItemTouchHelper()
    private var ticketsDS: TicketsDS? = null

    companion object {
        private var searchTextChanged: Boolean = false
        private var screenRotation: Boolean = false
        private var searchGuestText: String = ""
        var ticketList: ArrayList<TicketModel>? = null
        val finallyFilteredTicketList = ArrayList<TicketModel>()
    }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_ticket_list)

        ticketsDS = TicketsDS()
        SyncController.ticketListRefresher = this

        setSupportActionBar(ticket_list_toolbar)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.extras.containsKey("searchGuestText")) {
            searchGuestText = intent.getStringExtra("searchGuestText")
            search_guest.setText(searchGuestText)
            intent.removeExtra("searchGuestText")
        } else {
            if (!screenRotation)
                searchGuestText = ""
        }

        //searchGuestText = search_guest.text.toString()
        eventId = intent.getStringExtra("eventId")

        ticket_list_view.layoutManager =
            LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)

        val itemTouchHelper = ItemTouchHelper(recyclerItemTouchHelper)
        recyclerItemTouchHelper.parentLayout = tickets_layout
        itemTouchHelper.attachToRecyclerView(ticket_list_view)

        search_guest.post {
            search_guest.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                    searchGuestText = search_guest.text.toString()
                    adaptListView(findViewById(com.bigneon.doorperson.R.id.ticket_list_view))
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }

        ticket_list_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), com.bigneon.doorperson.R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        ticket_list_toolbar.setNavigationOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        // Refresh/load ticket list initially
        refreshTicketList(eventId)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        screenRotation = true
    }

    private fun adaptListView(ticketListView: RecyclerView) {
        val searchWords = searchGuestText.split(" ")
        if (searchGuestText != "") {
            searchTextChanged = true
        }
        finallyFilteredTicketList.clear()

        for (word in searchWords) {
            val filteredTicketList = ticketList?.filter {
                it.firstName?.toLowerCase()!!.contains(word.toLowerCase()) || it.lastName?.toLowerCase()!!.contains(
                    word.toLowerCase()
                )
            } as ArrayList<TicketModel>
            filteredTicketList.forEach { if (it !in finallyFilteredTicketList) finallyFilteredTicketList.add(it) }
            finallyFilteredTicketList.sortedWith(compareBy({ it.lastName }, { it.firstName }))
        }

        if (screenRotation || searchTextChanged) {
            ticketListView.adapter =
                TicketListAdapter(finallyFilteredTicketList)
            recyclerItemTouchHelper.ticketList = finallyFilteredTicketList
            recyclerItemTouchHelper.adapter = ticketListView.adapter as TicketListAdapter

            screenRotation = false
            searchTextChanged = false
        } else {
            ticketListView.adapter =
                TicketListAdapter(ticketList!!)
            recyclerItemTouchHelper.ticketList = ticketList
            recyclerItemTouchHelper.adapter = ticketListView.adapter as TicketListAdapter
        }

        if (ticketListView.adapter?.itemCount!! > 0) {
            ticket_list_view.visibility = View.VISIBLE
            no_guests_found_placeholder.visibility = View.GONE
        } else {
            ticket_list_view.visibility = View.GONE
            no_guests_found_placeholder.visibility = View.VISIBLE
        }
    }

    override fun refreshTicketList(eventId: String?) {
        if (eventId != this.eventId)
            return

        tickets_layout.loading_guests_progress_bar.visibility = View.GONE

        ticketList = ticketsDS!!.getAllTicketsForEvent(this.eventId!!) ?: return
        adaptListView(ticket_list_view)

        if (ticketListItemPosition >= 0) {
            (ticket_list_view.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                ticketListItemPosition,
                ticketListItemOffset
            )
        }

        ticket_list_view.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(adapterPosition: Int, view: View) {

                val filteredList =
                    if (TicketListActivity.finallyFilteredTicketList.size > 0)
                        TicketListActivity.finallyFilteredTicketList else ticketList

                val intent = Intent(getContext(), TicketActivity::class.java)
                intent.putExtra("ticketId", filteredList?.get(adapterPosition)?.ticketId)
                intent.putExtra("eventId", eventId)
                intent.putExtra("redeemKey", filteredList?.get(adapterPosition)?.redeemKey)
                intent.putExtra("searchGuestText", searchGuestText)
                intent.putExtra("firstName", filteredList?.get(adapterPosition)?.firstName)
                intent.putExtra("lastName", filteredList?.get(adapterPosition)?.lastName)
                intent.putExtra("priceInCents", filteredList?.get(adapterPosition)?.priceInCents)
                intent.putExtra("ticketTypeName", filteredList?.get(adapterPosition)?.ticketType)
                intent.putExtra("status", filteredList?.get(adapterPosition)?.status)
                startActivity(intent)
            }
        })

        ticket_list_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                ticketListItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                ticketListItemOffset = recyclerView.layoutManager?.findViewByPosition(ticketListItemPosition)!!.top
            }
        })
    }
}

interface ITicketListRefresher {
    fun refreshTicketList(eventId: String?)
}