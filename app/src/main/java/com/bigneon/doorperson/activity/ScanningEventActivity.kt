package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bigneon.doorperson.R
import com.bigneon.doorperson.db.SyncController
import com.bigneon.doorperson.db.ds.EventsDS
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.NetworkUtils
import kotlinx.android.synthetic.main.activity_scanning_event.*
import kotlinx.android.synthetic.main.content_scanning_event.*

class ScanningEventActivity : AppCompatActivity() {
    private var eventId: String = ""
    private var ticketsDS: TicketsDS? = null
    private var eventsDS: EventsDS? = null
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
        setContentView(R.layout.activity_scanning_event)
        setSupportActionBar(scanning_events_toolbar)

        AppUtils.checkLogged(getContext())

        eventId = intent.getStringExtra("eventId")
        ticketsDS = TicketsDS()
        eventsDS = EventsDS()

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scanning_events_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        getEventSummary()

        scanning_events_toolbar.setNavigationOnClickListener {
            startActivity(Intent(getContext(), EventsActivity::class.java))
        }

        scanning_events_button.setOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        scanning_event_layout.setOnRefreshListener {
            // Sync local DB with remote server
            SyncController.synchronizeAllTables(true)

            getEventSummary()

            // Hide swipe to refresh icon animation
            scanning_event_layout.isRefreshing = false
        }
    }

    private fun getEventSummary() {
        val event = eventsDS!!.getEvent(eventId)
        scanning_event_name.text = event?.name ?: ""

        number_of_redeemed.text = getString(
            R.string._1_d_of_2_d_redeemed,
            ticketsDS!!.getRedeemedTicketNumberForEvent(eventId),
            ticketsDS!!.getAllTicketNumberForEvent(eventId)
        )

        number_of_checked.text = getString(
            R.string._1_d_checked,
            ticketsDS!!.getCheckedTicketNumberForEvent(eventId)
        )
    }

    override fun onStart() {
        NetworkUtils.instance().addNetworkStateListener(this, networkStateReceiverListener)
        super.onStart()
    }

    override fun onStop() {
        NetworkUtils.instance().removeNetworkStateListener(this, networkStateReceiverListener)
        super.onStop()
    }

    override fun onBackPressed() {
        startActivity(Intent(getContext(), EventsActivity::class.java))
        finish()
    }
}