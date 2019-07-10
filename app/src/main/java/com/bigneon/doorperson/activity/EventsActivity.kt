package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bigneon.doorperson.adapter.EventListAdapter
import com.bigneon.doorperson.adapter.OnItemClickListener
import com.bigneon.doorperson.adapter.addOnItemClickListener
import com.bigneon.doorperson.controller.EventDataHandler
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.AppUtils.Companion.eventListItemOffset
import com.bigneon.doorperson.util.AppUtils.Companion.eventListItemPosition
import com.bigneon.doorperson.util.NetworkUtils.Companion.addNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.removeNetworkStateListener
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.content_events.*

class EventsActivity : AppCompatActivity() {
    private var eventsListView: RecyclerView? = null

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
        setContentView(com.bigneon.doorperson.R.layout.activity_events)

        AppUtils.checkLogged()

        eventsListView = events_layout.findViewById(com.bigneon.doorperson.R.id.events_list_view)

        eventsListView!!.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val eventId = EventDataHandler.getEventByPosition(getContext(), position)?.id
                val intent = Intent(getContext(), ScanningEventActivity::class.java)
                intent.putExtra("eventId", eventId)
                startActivity(intent)
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

            // Hide swipe to refresh icon animation
            events_layout.isRefreshing = false // TODO - Move after sync is done!
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
        val events = EventDataHandler.loadAllEvents(getContext())
        if (events != null) {
            eventsListView?.layoutManager =
                LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)

            eventsListView?.adapter = EventListAdapter(events)
            EventDataHandler.storeEvents(events)

            if (eventListItemPosition >= 0) {
                (eventsListView?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    eventListItemPosition,
                    eventListItemOffset
                )
            }
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

