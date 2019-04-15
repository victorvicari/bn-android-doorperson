package com.bigneon.doorperson.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.model.TicketModel
import kotlinx.android.synthetic.main.activity_ticket_list.*
import kotlinx.android.synthetic.main.content_ticket_list.*
import kotlinx.android.synthetic.main.content_ticket_list.view.*


class TicketListActivity : AppCompatActivity() {
    private var eventId: String? = null
    private var searchGuestText: String? = null
    private val recyclerItemTouchHelper: RecyclerItemTouchHelper = RecyclerItemTouchHelper()
    private var ticketsDS: TicketsDS? = null

    companion object {
        private var searchTextChanged: Boolean = false
        private var screenRotation: Boolean = false
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

        setSupportActionBar(ticket_list_toolbar)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val itemTouchHelper = ItemTouchHelper(recyclerItemTouchHelper)
        recyclerItemTouchHelper.parentLayout = tickets_layout
        itemTouchHelper.attachToRecyclerView(ticket_list_view)

        search_guest.post {
            search_guest.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                    searchTextChanged = true
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

        searchGuestText = intent.getStringExtra("searchGuestText") ?: ""
        if (searchGuestText!!.isEmpty()) {
            finallyFilteredTicketList.clear()
        } else {
            search_guest.setText(searchGuestText)
            searchTextChanged = true
        }
        eventId = intent.getStringExtra("eventId")
        val position = intent.getIntExtra("position", -1)
        val offset = intent.getIntExtra("offset", 0)

        // Refresh ticket list every minute
        val intentFilter = IntentFilter()
        // Run every 1 minute!
        intentFilter.addAction("android.intent.action.TIME_TICK")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Recover scroll position after refresh
                val firstVisiblePosition =
                    (ticket_list_view.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val firstVisibleOffset =
                    (ticket_list_view.layoutManager as LinearLayoutManager).findViewByPosition(firstVisiblePosition)?.top
                refreshTicketList(firstVisiblePosition, firstVisibleOffset ?: 0)
            }
        }
        registerReceiver(receiver, intentFilter)

        // Refresh/load ticket list initially
        refreshTicketList(position, offset)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        screenRotation = true
    }

    private fun adaptListView(ticketListView: RecyclerView) {
        val searchWords = search_guest.text.toString().split(" ")
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

    private fun refreshTicketList(position: Int, offset: Int) {
        ticketList = ticketsDS!!.getAllTicketsForEvent(this.eventId!!)
        ticket_list_view.layoutManager =
            LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
        tickets_layout.loading_guests_progress_bar.visibility = View.GONE
        adaptListView(ticket_list_view)

        if (position >= 0) {
            (ticket_list_view.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, offset)
        }

        ticket_list_view.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {

                val filteredList =
                    if (TicketListActivity.finallyFilteredTicketList.size > 0) TicketListActivity.finallyFilteredTicketList else ticketList
                val intent = Intent(getContext(), TicketActivity::class.java)
                intent.putExtra("ticketId", filteredList?.get(position)?.ticketId)
                intent.putExtra("eventId", eventId)
                intent.putExtra("redeemKey", filteredList?.get(position)?.redeemKey)
                intent.putExtra("searchGuestText", searchGuestText)
                intent.putExtra("firstName", filteredList?.get(position)?.firstName)
                intent.putExtra("lastName", filteredList?.get(position)?.lastName)
                intent.putExtra("priceInCents", filteredList?.get(position)?.priceInCents)
                intent.putExtra("ticketTypeName", filteredList?.get(position)?.ticketType)
                intent.putExtra("status", filteredList?.get(position)?.status)
                intent.putExtra("position", position)
                intent.putExtra(
                    "offset",
                    (ticket_list_view.layoutManager as LinearLayoutManager).findViewByPosition(position)!!.top
                )
                startActivity(intent)
            }
        })
    }
}