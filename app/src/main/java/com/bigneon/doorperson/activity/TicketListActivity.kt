package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bigneon.doorperson.adapter.BaseAdapter
import com.bigneon.doorperson.adapter.BaseAdapter.FooterType
import com.bigneon.doorperson.adapter.TicketListAdapter
import com.bigneon.doorperson.config.AppConstants.Companion.MIN_TIMESTAMP
import com.bigneon.doorperson.config.AppConstants.Companion.PAGE_LIMIT
import com.bigneon.doorperson.db.SyncController
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.NetworkUtils
import kotlinx.android.synthetic.main.activity_ticket_list.*
import kotlinx.android.synthetic.main.content_ticket_list.*


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 27.06.2019..
 ****************************************************/
class TicketListActivity : AppCompatActivity(), BaseAdapter.OnItemClickListener, BaseAdapter.OnReloadClickListener {
    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
                no_internet_toolbar_icon.visibility = View.GONE
            }

            override fun networkUnavailable() {
                no_internet_toolbar_icon.visibility = View.VISIBLE
            }
        }
    private var refreshTicketListener: SyncController.RefreshTicketListener =
        object : SyncController.RefreshTicketListener {
            override fun refreshTicketList(eventId: String) {
//                if (TicketListActivityOLD.eventId == eventId)
//                    refreshList(eventId)
            }
        }
    private var loadingTicketListener: SyncController.LoadingTicketListener =
        object : SyncController.LoadingTicketListener {
            override fun finish() {
                tickets_swipe_refresh_layout.isEnabled = true
                // Hide swipe to refresh icon animation
                tickets_swipe_refresh_layout.isRefreshing = false
            }
        }

    private var ticketListAdapter: TicketListAdapter? = null

    companion object {
        private var eventId: String? = null
        //        private var searchTextChanged: Boolean = false
//        private var screenRotation: Boolean = false
//        private var searchGuestText: String = ""
        var ticketList: ArrayList<TicketModel> = ArrayList()
        var currentLoadedPage = 0
//        val finallyFilteredTicketList = ArrayList<TicketModel>()
    }

    private var isLoading: Boolean = false
    private var isLastPage: Boolean = false

    private fun getContext(): Context {
        return this
    }

    private fun loadPageOfTickets(eventId: String, page: Int) {
        isLoading = true

        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken != null) {
                fun setTickets(tickets: ArrayList<TicketModel>?) {
                    ticketListAdapter?.removeFooter()
                    isLoading = false

                    if (!tickets.isNullOrEmpty()) {
                        ticketList.addAll(tickets)
                        ticketListAdapter?.addAll(ticketList)

                        if (tickets.size >= PAGE_LIMIT) {
                            ticketListAdapter?.addFooter()
                        } else {
                            isLastPage = true
                        }
                    }

                    for (listener in SyncController.refreshTicketListeners)
                        listener.refreshTicketList(eventId)
                }

                RestAPI.getTicketsForEvent(
                    accessToken,
                    eventId,
                    MIN_TIMESTAMP,
                    PAGE_LIMIT,
                    page,
                    ::setTickets
                )
            }
        }

        RestAPI.accessToken(::setAccessTokenForEvent)
    }

    override fun onItemClick(position: Int, view: View) {
        val ticket = ticketListAdapter?.getItem(position);

        if (ticket != null) {
            val intent = Intent(getContext(), TicketActivity::class.java)
            intent.putExtra("ticketId", ticket.ticketId)
            intent.putExtra("eventId", eventId)
            intent.putExtra("redeemKey", ticket.redeemKey)
//            intent.putExtra("searchGuestText", searchGuestText) // TODO
            intent.putExtra("searchGuestText", "")
            intent.putExtra("firstName", ticket.firstName)
            intent.putExtra("lastName", ticket.lastName)
            intent.putExtra("priceInCents", ticket.priceInCents)
            intent.putExtra("ticketType", ticket.ticketType)
            intent.putExtra("status", ticket.status)
            startActivity(intent)
        }
    }

    override fun onReloadClick() {
        ticketListAdapter?.updateFooter(FooterType.LOAD_MORE)
        loadPageOfTickets(eventId!!, currentLoadedPage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_ticket_list)
        setSupportActionBar(ticket_list_toolbar)

        eventId = intent.getStringExtra("eventId")

        ticket_list_view.layoutManager = LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)

        ticketListAdapter = TicketListAdapter()
        ticketListAdapter!!.setOnItemClickListener(this)
        ticketListAdapter!!.setOnReloadClickListener(this)

        loadPageOfTickets(eventId!!, currentLoadedPage)

        ticket_list_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = (recyclerView?.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (recyclerView.layoutManager as LinearLayoutManager).itemCount
                val firstVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                AppUtils.ticketListItemPosition = firstVisibleItemPosition

                AppUtils.ticketListItemOffset =
                    if (recyclerView.layoutManager?.findViewByPosition(AppUtils.ticketListItemPosition) != null) recyclerView.layoutManager?.findViewByPosition(
                        AppUtils.ticketListItemPosition
                    )!!.top else 0

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_LIMIT
                    ) {
                        loadPageOfTickets(eventId!!, ++currentLoadedPage)
                    }
                }
            }
        })

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        NetworkUtils.instance().addNetworkStateListener(this, networkStateReceiverListener)
        SyncController.addRefreshTicketListener(refreshTicketListener)
        SyncController.addLoadingTicketListener(loadingTicketListener)
        super.onStart()
    }

    override fun onStop() {
        NetworkUtils.instance().removeNetworkStateListener(this, networkStateReceiverListener)
        SyncController.removeRefreshTicketListener(refreshTicketListener)
        SyncController.removeLoadingTicketListener(loadingTicketListener)
        super.onStop()
    }

    override fun onBackPressed() {
        val intent = Intent(getContext(), ScanTicketsActivity::class.java)
        intent.putExtra("eventId", eventId)
        startActivity(intent)
        finish()
    }
}