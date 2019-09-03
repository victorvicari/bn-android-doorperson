package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.bigneon.doorperson.adapter.OnItemClickListener
import com.bigneon.doorperson.adapter.TicketListAdapter
import com.bigneon.doorperson.adapter.addOnItemClickListener
import com.bigneon.doorperson.controller.RecyclerItemTouchHelper
import com.bigneon.doorperson.controller.TicketDataHandler
import com.bigneon.doorperson.controller.TicketDataHandler.Companion.addRefreshTicketsListener
import com.bigneon.doorperson.controller.TicketDataHandler.Companion.removeRefreshTicketsListener
import com.bigneon.doorperson.listener.PaginationScrollListener
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.AppUtils.Companion.enableOfflineMode
import com.bigneon.doorperson.util.ConnectionDialog
import com.bigneon.doorperson.util.NetworkUtils
import com.bigneon.doorperson.util.NetworkUtils.Companion.addNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.removeNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.setWiFiEnabled
import kotlinx.android.synthetic.main.activity_ticket_list.*
import kotlinx.android.synthetic.main.content_ticket_list.*

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 27.06.2019..
 ****************************************************/
class TicketListActivity : AppCompatActivity() {
    private val TAG = TicketListActivity::class.java.simpleName
    private var eventId: String? = null
    private var ticketId: String? = null
    private var status: String? = null
    private var mAdapter: TicketListAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null

    //    private var searchTextChanged: Boolean = false
    private var screenRotation: Boolean = false
    private var searchGuestText: String = ""
    private var isLastPage = false
    private var isLoading = false
    private var itemCount = 0

    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
            }

            override fun networkUnavailable() {
            }
        }

    private var refreshTicketsListener: TicketDataHandler.RefreshTickets =
        object : TicketDataHandler.RefreshTickets {
            override fun updateTicket(ticketId: String, status: String) {
                val ticket = recyclerItemTouchHelper.ticketList!!
                    .stream()
                    .filter { t -> t.ticketId == ticketId }
                    .findAny().orElse(null)
                if (ticket != null) {
                    ticket.status = status
                }
            }

            override fun refreshTicketList() {
                adaptTicketList()
            }
        }

    companion object {
        val recyclerItemTouchHelper: RecyclerItemTouchHelper = RecyclerItemTouchHelper()
        var currentPage = 0
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

        if (intent.extras!!.containsKey("searchGuestText")) {
            searchGuestText = intent.getStringExtra("searchGuestText")
            search_guest.setText(searchGuestText)
            intent.removeExtra("searchGuestText")
        } else {
            if (!screenRotation)
                searchGuestText = ""
        }

        eventId = intent.getStringExtra("eventId")
        ticketId = intent.getStringExtra("ticketId")
        status = intent.getStringExtra("status")

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
                // Starting loading process
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
                intent.putExtra("redeemedBy", ticket?.redeemedBy)
                intent.putExtra("redeemedAt", ticket?.redeemedAt)
                intent.putExtra("searchGuestText", searchGuestText)
                intent.putExtra("firstName", ticket?.firstName)
                intent.putExtra("lastName", ticket?.lastName)
                intent.putExtra("priceInCents", ticket?.priceInCents)
                intent.putExtra("ticketType", ticket?.ticketType)
                intent.putExtra("status", ticket?.status)
                startActivity(intent)
            }
        })

        var countDownTimerIsTicking = false
        val countDownTimer = object : CountDownTimer(1000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "Finish in: $millisUntilFinished ms")
            }

            override fun onFinish() {
                searchGuestText = search_guest.text.toString()
                countDownTimerIsTicking = false
                refreshTicketList()
            }
        }

        search_guest.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                if (countDownTimerIsTicking) {
                    countDownTimer.cancel()
                }
                countDownTimer.start()
                countDownTimerIsTicking = true
            }

            override fun afterTextChanged(s: Editable) {}
        })

        ticket_list_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), com.bigneon.doorperson.R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        ticket_list_toolbar.setNavigationOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }

        tickets_swipe_refresh_layout.setOnRefreshListener {
            refreshTicketList()
        }
    }

    private fun refreshTicketList() {
        itemCount = 0
        currentPage = 0
        isLastPage = false
        mAdapter?.clear()
        adaptTicketList()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        screenRotation = true
    }

    private fun adaptTicketList() {
        ticket_list_view.adapter = mAdapter
        recyclerItemTouchHelper.adapter = mAdapter

        if (recyclerItemTouchHelper.ticketList == null || recyclerItemTouchHelper.ticketList!!.size == 0) {
            val items = TicketDataHandler.loadPageOfTickets(getContext(), eventId!!, searchGuestText, 0)
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
        } else {
            val ticket = recyclerItemTouchHelper.ticketList!!
                .stream()
                .filter { t -> t.ticketId == ticketId }
                .findAny().orElse(null)
            if (ticket != null)
                ticket.status = status ?: ""
        }

        if (recyclerItemTouchHelper.ticketList != null) {
            mAdapter?.list = recyclerItemTouchHelper.ticketList

            // Removing placeholder it there is any tickets on the list
            if (mAdapter?.list?.size!! > 0) {
                no_guests_found_placeholder.visibility = View.GONE
                ticket_list_view.visibility = View.VISIBLE
            }
        }

        // Setting up previous scroll position
        if (AppUtils.ticketListItemPosition >= 0) {
            (ticket_list_view.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                AppUtils.ticketListItemPosition,
                AppUtils.ticketListItemOffset
            )
        }

        // Finishing loading process
        isLoading = false

        // Removing swipe to refresh progress bar
        tickets_swipe_refresh_layout.isRefreshing = false
    }

    private fun loadNewPage(eventId: String, page: Int) {
        val items = TicketDataHandler.loadPageOfTickets(getContext(), eventId, searchGuestText, page)
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
        if (items!!.isEmpty())
            isLastPage = true
    }

    override fun onStart() {
        addNetworkStateListener(this, networkStateReceiverListener)
        addRefreshTicketsListener(refreshTicketsListener)
        super.onStart()
    }

    override fun onStop() {
        removeNetworkStateListener(this, networkStateReceiverListener)
        removeRefreshTicketsListener(refreshTicketsListener)
        super.onStop()
    }

    override fun onBackPressed() {
        val intent = Intent(getContext(), ScanTicketsActivity::class.java)
        intent.putExtra("eventId", eventId)
        intent.putExtra("searchGuestText", searchGuestText)
        startActivity(intent)
        finish()
    }
}