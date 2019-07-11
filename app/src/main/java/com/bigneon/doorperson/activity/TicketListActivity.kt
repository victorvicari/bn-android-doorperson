package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.bigneon.doorperson.adapter.OnItemClickListener
import com.bigneon.doorperson.adapter.TicketListAdapter
import com.bigneon.doorperson.adapter.addOnItemClickListener
import com.bigneon.doorperson.controller.RecyclerItemTouchHelper
import com.bigneon.doorperson.controller.TicketDataHandler
import com.bigneon.doorperson.listener.PaginationScrollListener
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.AppUtils.Companion.enableOfflineMode
import com.bigneon.doorperson.util.ConnectionDialog
import com.bigneon.doorperson.util.NetworkUtils
import com.bigneon.doorperson.util.NetworkUtils.Companion.setWiFiEnabled
import kotlinx.android.synthetic.main.activity_ticket_list.*
import kotlinx.android.synthetic.main.content_ticket_list.*

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 27.06.2019..
 ****************************************************/
class TicketListActivity : AppCompatActivity() {
    private var eventId: String? = null
    private var mAdapter: TicketListAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private val PAGE_START = 0
    private var currentPage = PAGE_START
    private var isLastPage = false
    private var isLoading = false
    private var itemCount = 0
    //    private var searchTextChanged: Boolean = false
    private var screenRotation: Boolean = false
    private var searchGuestText: String = ""

    companion object {
        val recyclerItemTouchHelper: RecyclerItemTouchHelper = RecyclerItemTouchHelper()
    }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_ticket_list)
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

        eventId = intent.getStringExtra("eventId")
        val itemTouchHelper = ItemTouchHelper(recyclerItemTouchHelper)
        recyclerItemTouchHelper.parentLayout = tickets_layout

        itemTouchHelper.attachToRecyclerView(ticket_list_view)
        ticket_list_view.setHasFixedSize(true)

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this)
        ticket_list_view.layoutManager = mLayoutManager

        mAdapter = TicketListAdapter(ArrayList())

        adaptTicketList()

        ticket_list_view.addOnScrollListener(object : PaginationScrollListener(mLayoutManager!!) {
            override fun loadMoreItems() {
                isLoading = true
                loadNewPage(eventId!!, ++currentPage)
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        ticket_list_view.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(adapterPosition: Int, view: View) {
                val ticket = mAdapter!!.list?.get(adapterPosition)

                val intent = Intent(getContext(), TicketActivity::class.java)
                intent.putExtra("ticketId", ticket?.ticketId)
                intent.putExtra("eventId", eventId)
                intent.putExtra("redeemKey", ticket?.redeemKey)
                intent.putExtra("searchGuestText", searchGuestText)
                intent.putExtra("firstName", ticket?.firstName)
                intent.putExtra("lastName", ticket?.lastName)
                intent.putExtra("priceInCents", ticket?.priceInCents)
                intent.putExtra("ticketType", ticket?.ticketType)
                intent.putExtra("status", ticket?.status)
                startActivity(intent)
            }
        })

        search_guest.post {
            search_guest.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                    searchGuestText = search_guest.text.toString()
//                    adaptListView(findViewById(com.bigneon.doorperson.R.id.ticket_list_view))
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

        tickets_swipe_refresh_layout.setOnRefreshListener {
            itemCount = 0
            currentPage = PAGE_START
            isLastPage = false
            mAdapter?.clear()
            adaptTicketList()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        screenRotation = true
    }

    private fun adaptTicketList() {
        ticket_list_view.adapter = mAdapter
        recyclerItemTouchHelper.adapter = mAdapter

        if (currentPage != PAGE_START) mAdapter?.removeLoading()

        if (recyclerItemTouchHelper.ticketList == null || recyclerItemTouchHelper.ticketList!!.size == 0) {
            val items = TicketDataHandler.loadPageOfTickets(getContext(), eventId!!, 0)
            if (items == null) {
                object : ConnectionDialog() {
                    override fun positiveButtonAction(context: Context) {
                        enableOfflineMode()
                        adaptTicketList()
                    }

                    override fun negativeButtonAction(context: Context) {
                        setWiFiEnabled(context)
                        while (!NetworkUtils.isNetworkAvailable(context)) Thread.sleep(1000)
                        adaptTicketList()
                    }
                }.showDialog(getContext())
            }
            recyclerItemTouchHelper.ticketList = items as java.util.ArrayList<TicketModel>?
        }

        mAdapter?.list = recyclerItemTouchHelper.ticketList

        if (mAdapter?.list != null) {
            if (mAdapter?.list?.size!! > 0) {
                no_guests_found_placeholder.visibility = View.GONE
                ticket_list_view.visibility = View.VISIBLE
            }

            if (AppUtils.ticketListItemPosition >= 0) {
                (ticket_list_view.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    AppUtils.ticketListItemPosition,
                    AppUtils.ticketListItemOffset
                )
            }
        }
        isLoading = false
        tickets_swipe_refresh_layout.isRefreshing = false
    }

    private fun loadNewPage(eventId: String, page: Int) {
        val items = TicketDataHandler.loadPageOfTickets(getContext(), eventId, page)
        if (items == null) {
            object : ConnectionDialog() {
                override fun positiveButtonAction(context: Context) {
                    enableOfflineMode()
                    loadNewPage(eventId, page)
                }

                override fun negativeButtonAction(context: Context) {
                    setWiFiEnabled(context)
                    while (!NetworkUtils.isNetworkAvailable(context)) Thread.sleep(1000)
                    loadNewPage(eventId, page)
                }
            }.showDialog(getContext())
        }
        recyclerItemTouchHelper.ticketList?.addAll(items!!)
        adaptTicketList()
        if (items!!.isNotEmpty())
            mAdapter?.addLoading()
        else
            isLastPage = true
    }

    override fun onBackPressed() {
        val intent = Intent(getContext(), ScanTicketsActivity::class.java)
        intent.putExtra("eventId", eventId)
        startActivity(intent)
        finish()
    }
}