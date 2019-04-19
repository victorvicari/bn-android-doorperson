package com.bigneon.doorperson.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.bigneon.doorperson.adapter.EventListAdapter
import com.bigneon.doorperson.adapter.OnItemClickListener
import com.bigneon.doorperson.adapter.addOnItemClickListener
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.db.SQLiteHelper
import com.bigneon.doorperson.db.SyncController
import com.bigneon.doorperson.db.SyncController.Companion.eventListItemOffset
import com.bigneon.doorperson.db.SyncController.Companion.eventListItemPosition
import com.bigneon.doorperson.db.SyncController.Companion.isNetworkAvailable
import com.bigneon.doorperson.db.ds.EventsDS
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.util.NetworkUtils
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.content_events.*


class EventsActivity : AppCompatActivity(), IEventListRefresher {
    private val TAG = EventsActivity::class.java.simpleName
    private var eventsDS: EventsDS? = null


    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
                isNetworkAvailable = true
                SyncController().synchronizeAllTables()
                Toast.makeText(getContext(), "Network is available!", Toast.LENGTH_LONG).show()
            }

            override fun networkUnavailable() {
                isNetworkAvailable = false
                Toast.makeText(getContext(), "Network is unavailable!", Toast.LENGTH_LONG).show()
            }
        }

    private val syncAllTablesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isNetworkAvailable)
                SyncController().synchronizeAllTables()
        }
    }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_events)

        SharedPrefs.setContext(this)
        RestAPI.setContext(this)
        SyncController.setContext(this)
        SQLiteHelper.setContext(this)

        eventsDS = EventsDS()
        SyncController.eventListRefresher = this

        // If we need synchronization every minute
        val filter = IntentFilter()
        // Run every 1 minute!
        filter.addAction("android.intent.action.TIME_TICK")

        NetworkUtils.instance().addNetworkStateListener(getContext(), networkStateReceiverListener)
        registerReceiver(syncAllTablesReceiver, filter)

        fun setAccessToken(accessToken: String?) {
            if (accessToken == null) {
                startActivity(Intent(getContext(), LoginActivity::class.java))
            } else {
                setSupportActionBar(events_toolbar)
                refreshEventList()
            }
        }
        RestAPI.accessToken(::setAccessToken)

        profile_settings.setOnClickListener {
            startActivity(Intent(getContext(), ProfileActivity::class.java))
        }
    }

    override fun onPause() {
        NetworkUtils.instance().removeNetworkStateListener(getContext(), networkStateReceiverListener)
        unregisterReceiver(syncAllTablesReceiver)
        super.onPause()
    }

    override fun refreshEventList() {
        val eventsListView: RecyclerView =
            events_layout.findViewById(com.bigneon.doorperson.R.id.events_list_view)
        val eventList = eventsDS!!.getAllEvents()
        if (eventList != null) {
            eventsListView.layoutManager =
                LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)

            eventsListView.adapter = EventListAdapter(eventList)

            if (eventListItemPosition >= 0) {
                (eventsListView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    eventListItemPosition,
                    eventListItemOffset
                )
            }

            eventsListView.addOnItemClickListener(object : OnItemClickListener {
                override fun onItemClicked(position: Int, view: View) {
                    val eventId = eventList[position].id
                    val intent = Intent(getContext(), ScanningEventActivity::class.java)
                    intent.putExtra("eventId", eventId)
                    startActivity(intent)
                }
            })

            eventsListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    eventListItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    eventListItemOffset =
                        recyclerView.layoutManager?.findViewByPosition(eventListItemPosition)!!.top
                }
            })
        }
    }
}

interface IEventListRefresher {
    fun refreshEventList()
}
