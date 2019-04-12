package com.bigneon.doorperson.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
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
import com.bigneon.doorperson.db.ds.EventsDS
import com.bigneon.doorperson.db.sync.SyncController
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.util.NetworkUtils
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.content_events.*


class EventsActivity : AppCompatActivity() {
    private var eventsDS: EventsDS? = null
    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
                Toast.makeText(getContext(), "Network is available!", Toast.LENGTH_LONG).show()
                SyncController().synchronizeAllTables()
            }

            override fun networkUnavailable() {
                Toast.makeText(getContext(), "Network is unavailable!", Toast.LENGTH_LONG).show()
            }
        }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_events)

        SharedPrefs.setContext(this);
        RestAPI.setContext(this);
        SyncController.setContext(this);
        SQLiteHelper.setContext(this);

        eventsDS = EventsDS()

        // If we need synchronization every minute
        val filter = IntentFilter()
        // Run every 1 minute!
        filter.addAction("android.intent.action.TIME_TICK")
        val syncAllTablesReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                SyncController().synchronizeAllTables()
            }
        }
        registerReceiver(syncAllTablesReceiver, filter)

        NetworkUtils.instance().addNetworkStateListener(getContext(), networkStateReceiverListener)

        fun setAccessToken(accessToken: String?) {
            if (accessToken == null) {
                startActivity(Intent(getContext(), LoginActivity::class.java))
            } else {
                setSupportActionBar(events_toolbar)
                //this line shows back button
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                events_toolbar.navigationIcon!!.setColorFilter(
                    ContextCompat.getColor(getContext(), com.bigneon.doorperson.R.color.colorAccent),
                    PorterDuff.Mode.SRC_ATOP
                )

                events_toolbar.setNavigationOnClickListener {
                    startActivity(Intent(getContext(), LoginActivity::class.java))
                }

                // Refresh event list every minute
                val intentFilter = IntentFilter()
                // Run every 1 minute!
                intentFilter.addAction("android.intent.action.TIME_TICK")
                val refreshEventListReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        refreshEventList()
                    }
                }
                registerReceiver(refreshEventListReceiver, intentFilter)

                // Refresh/load event list initially
                refreshEventList()
            }
        }
        RestAPI.accessToken(::setAccessToken)
    }

    override fun onPause() {
        NetworkUtils.instance().removeNetworkStateListener(getContext(), networkStateReceiverListener)
        super.onPause()
    }

    private fun refreshEventList() {
        val eventsListView: RecyclerView =
            events_layout.findViewById(com.bigneon.doorperson.R.id.events_list_view)
        val eventList = eventsDS!!.getAllEvents()
        eventsListView.layoutManager =
            LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)

        eventsListView.adapter = EventListAdapter(eventList!!)

        eventsListView.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val eventId = eventList[position].id
                val intent = Intent(getContext(), ScanningEventActivity::class.java)
                intent.putExtra("eventId", eventId)
                startActivity(intent)
            }
        })
    }
}
