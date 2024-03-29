package com.bigneon.studio.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bigneon.studio.adapter.EventListAdapter
import com.bigneon.studio.adapter.OnItemClickListener
import com.bigneon.studio.adapter.addOnItemClickListener
import com.bigneon.studio.controller.EventDataHandler
import com.bigneon.studio.receiver.NetworkStateReceiver
import com.bigneon.studio.util.AppUtils
import com.bigneon.studio.util.AppUtils.Companion.checkLogged
import com.bigneon.studio.util.AppUtils.Companion.eventListItemOffset
import com.bigneon.studio.util.AppUtils.Companion.eventListItemPosition
import com.bigneon.studio.util.ConnectionDialog
import com.bigneon.studio.util.NetworkUtils.Companion.addNetworkStateListener
import com.bigneon.studio.util.NetworkUtils.Companion.isNetworkAvailable
import com.bigneon.studio.util.NetworkUtils.Companion.removeNetworkStateListener
import com.bigneon.studio.util.NetworkUtils.Companion.setWiFiEnabled
import kotlinx.android.synthetic.main.activity_event_list.*
import kotlinx.android.synthetic.main.content_events.*

class EventListActivity : AppCompatActivity() {
    private var eventsListView: RecyclerView? = null
    private var eventDataHandler: EventDataHandler? = null

    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
                no_internet_toolbar_icon.visibility = View.GONE
            }

            override fun networkUnavailable() {
                no_internet_toolbar_icon.visibility = View.VISIBLE
            }
        }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.studio.R.layout.activity_event_list)
        eventDataHandler = EventDataHandler()

        checkLogged()

        eventsListView = events_layout.findViewById(com.bigneon.studio.R.id.events_list_view)

        eventsListView!!.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val event = eventDataHandler?.getEventByPosition(getContext(), position)

                if (event != null) {
                    val eventId = event.id
                    val intent = Intent(getContext(), ScanningEventActivity::class.java)
                    intent.putExtra("eventId", eventId)
                    startActivity(intent)
                } else {
                    object : ConnectionDialog() {
                        override fun positiveButtonAction(context: Context) {
                            AppUtils.enableOfflineMode()
                            onItemClicked(position, view)
                        }

                        override fun negativeButtonAction(context: Context) {
                            setWiFiEnabled(context)
                            while (!isNetworkAvailable(context)) Thread.sleep(1000)
                            onItemClicked(position, view)
                        }
                    }.showDialog(getContext())
                }
            }
        })

        eventsListView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                eventListItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                eventListItemOffset =
                    if (recyclerView.layoutManager?.findViewByPosition(eventListItemPosition) != null)
                        recyclerView.layoutManager?.findViewByPosition(eventListItemPosition)!!.top else 0
            }
        })

        profile_settings.setOnClickListener {
            startActivity(Intent(getContext(), ProfileActivity::class.java))
        }

        events_layout.setOnRefreshListener {
            refreshList()
        }

        AppUtils.ticketListItemPosition = 0
        AppUtils.ticketListItemOffset = 0

        refreshList()
    }

    override fun onStart() {
        addNetworkStateListener(this, networkStateReceiverListener)
        super.onStart()
    }

    private fun refreshList() {
        val events = eventDataHandler?.loadAllEvents(getContext())
        if (events != null) {
            eventsListView?.layoutManager =
                LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)

            eventsListView?.adapter = EventListAdapter(events)
            eventDataHandler?.storeEvents(events)

            if (eventListItemPosition >= 0) {
                (eventsListView?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    eventListItemPosition,
                    eventListItemOffset
                )
            }
            TicketListActivity.recyclerItemTouchHelper.ticketList = null
            TicketListActivity.currentPage = 0

            // Hide swipe to refresh icon animation if shown
            events_layout.isRefreshing = false
        } else {
            object : ConnectionDialog() {
                override fun positiveButtonAction(context: Context) {
                    AppUtils.enableOfflineMode()
                    refreshList()
                }

                override fun negativeButtonAction(context: Context) {
                    setWiFiEnabled(context)
                    while (!isNetworkAvailable(context)) Thread.sleep(1000)
                    refreshList()
                }
            }.showDialog(getContext())
        }
    }

    override fun onStop() {
        removeNetworkStateListener(this, networkStateReceiverListener)
        super.onStop()
    }

    override fun onBackPressed() {
        finish()
        moveTaskToBack(true)
    }
}

