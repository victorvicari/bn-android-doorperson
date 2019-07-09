package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bigneon.doorperson.R
import com.bigneon.doorperson.controller.EventDataHandler
import com.bigneon.doorperson.controller.TicketDataHandler
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.NetworkUtils.Companion.addNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.removeNetworkStateListener
import kotlinx.android.synthetic.main.activity_scanning_event.*
import kotlinx.android.synthetic.main.content_scanning_event.*

class ScanningEventActivity : AppCompatActivity() {
    private var eventId: String = ""

    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
                no_internet_toolbar_icon.visibility = View.GONE
            }

            override fun networkUnavailable() {
                no_internet_toolbar_icon.visibility = View.VISIBLE
            }
        }

//    private var refreshTicketListener: SyncController.RefreshTicketListener =
//        object : SyncController.RefreshTicketListener {
//            override fun refreshTicketList(eventId: String) {
//                getEventSummary()
//            }
//        }

//    private var loadingTicketListener: SyncController.LoadingTicketListener =
//        object : SyncController.LoadingTicketListener {
//            override fun finish() {
//                scanning_events_button.visibility = View.VISIBLE
//                loading_events_button.visibility = View.GONE
//                number_of_loaded.visibility = View.GONE
//                number_of_redeemed.visibility = View.VISIBLE
//                number_of_checked.visibility = View.VISIBLE
//                getEventSummary()
//                loading_events_button.isEnabled = true
//                scanning_event_layout.isEnabled = true
//
//                // Hide swipe to refresh icon animation
//                scanning_event_layout.isRefreshing = false
//            }
//        }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning_event)
        setSupportActionBar(scanning_events_toolbar)

        AppUtils.checkLogged()

        eventId = intent.getStringExtra("eventId")
        TicketDataHandler.storeTickets(eventId)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getEventSummary()

//        if (TicketDataHandler.getAllTicketNumberForEvent(eventId) == 0) {
//            loading_events_button.visibility = View.VISIBLE
//            scanning_events_button.visibility = View.GONE
//            number_of_loaded.visibility = View.VISIBLE
//            number_of_redeemed.visibility = View.GONE
//            number_of_checked.visibility = View.GONE
//            getLoadedSummary()
//            scanning_event_layout.isEnabled = false
//        } else {
//            scanning_events_button.visibility = View.VISIBLE
//            loading_events_button.visibility = View.GONE
//            number_of_loaded.visibility = View.GONE
//            number_of_redeemed.visibility = View.VISIBLE
//            number_of_checked.visibility = View.VISIBLE
//            getEventSummary()
//            scanning_event_layout.isEnabled = true
//        }

        scanning_events_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        scanning_events_toolbar.setNavigationOnClickListener {
//            if (loading_events_button.isEnabled)
                startActivity(Intent(getContext(), EventsActivity::class.java))
        }

        scanning_events_button.setOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

//        loading_events_button.setOnClickListener {
//            loading_events_button.isEnabled = false
//            SyncController.loadTicketsForEvent(eventId)
//        }

        scanning_event_layout.setOnRefreshListener {
            if (scanning_event_layout.isEnabled) {
                scanning_event_layout.isEnabled = false
                scanning_event_layout.isRefreshing = true
                // Sync local DB with remote server
//                SyncController.updateEvent(eventId)
            }
        }
    }

//    private fun getLoadedSummary() {
//        val event =  EventDataHandler.eventsDS.getEvent(eventId)
//        number_of_loaded.text = getString(
//            R.string._1_d_of_2_d_loaded,
//            TicketDataHandler.getAllTicketNumberForEvent(eventId),
//            event?.totalNumOfTickets
//        )
//    }

    private fun getEventSummary() {
        scanning_event_name.text = EventDataHandler.getEventByID(getContext(), eventId)?.name ?: ""

        number_of_redeemed.text = getString(
            R.string._1_d_of_2_d_redeemed,
            TicketDataHandler.getRedeemedTicketNumberForEvent(getContext(), eventId),
            TicketDataHandler.getAllTicketNumberForEvent(getContext(), eventId)
        )

        number_of_checked.text = getString(
            R.string._1_d_checked,
            TicketDataHandler.getCheckedTicketNumberForEvent(eventId)
        )
    }

    override fun onStart() {
        addNetworkStateListener(this, networkStateReceiverListener)
        super.onStart()
    }

    override fun onStop() {
        removeNetworkStateListener(this, networkStateReceiverListener)
        super.onStop()
    }

    override fun onBackPressed() {
        startActivity(Intent(getContext(), EventsActivity::class.java))
        finish()
    }
}