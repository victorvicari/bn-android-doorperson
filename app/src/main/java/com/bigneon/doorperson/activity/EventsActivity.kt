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
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.db.SQLiteHelper
import com.bigneon.doorperson.db.SyncController
import com.bigneon.doorperson.db.SyncController.Companion.eventListItemOffset
import com.bigneon.doorperson.db.SyncController.Companion.eventListItemPosition
import com.bigneon.doorperson.db.ds.EventsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.service.SyncService
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.NetworkUtils
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.content_events.*

class EventsActivity : AppCompatActivity(), IEventListRefresher {
    private var eventsDS: EventsDS? = null

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(com.bigneon.doorperson.R.layout.activity_events)

        SharedPrefs.setContext(this)
        RestAPI.setContext(this)
        SyncController.setContext(this)
        SQLiteHelper.setContext(this)
        NetworkUtils.setContext(this)

        AppUtils.checkLogged(getContext())

        eventsDS = EventsDS()
        SyncController.eventListRefresher = this

        // Start synchronization service
        startService(Intent(this, SyncService::class.java))

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

        events_layout.setOnRefreshListener {
            // Sync local DB with remote server
            SyncController.synchronizeAllTables()

            // Refresh view from DB
            refreshEventList()

            // Hide swipe to refresh icon animation
            events_layout.isRefreshing = false
        }
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

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
    }
}

interface IEventListRefresher {
    fun refreshEventList()
}
