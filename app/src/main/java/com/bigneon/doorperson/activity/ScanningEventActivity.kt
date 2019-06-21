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
    private var refreshTicketListener: SyncController.RefreshTicketListener =
        object : SyncController.RefreshTicketListener {
            override fun refreshTicketList(eventId: String, page: Int) {
                getLoadedSummary()
                // TODO - Implement progress bar considering page and total ticket number
            }
        }

    private var loadingTicketListener: SyncController.LoadingTicketListener =
        object : SyncController.LoadingTicketListener {
            override fun finish() {
                scanning_events_button.visibility = View.VISIBLE
                loading_events_button.visibility = View.GONE
                loading_events_button.isEnabled = true
                number_of_loaded.visibility = View.GONE
                number_of_redeemed.visibility = View.VISIBLE
                number_of_checked.visibility = View.VISIBLE
                getEventSummary()
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

        if(ticketsDS!!.getAllTicketNumberForEvent(eventId) == 0) {
            loading_events_button.visibility = View.VISIBLE
            scanning_events_button.visibility = View.GONE
            number_of_loaded.visibility = View.VISIBLE
            number_of_redeemed.visibility = View.GONE
            number_of_checked.visibility = View.GONE
            getLoadedSummary()
        } else {
            scanning_events_button.visibility = View.VISIBLE
            loading_events_button.visibility = View.GONE
            number_of_loaded.visibility = View.GONE
            number_of_redeemed.visibility = View.VISIBLE
            number_of_checked.visibility = View.VISIBLE
            getEventSummary()
        }

        scanning_events_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        scanning_events_toolbar.setNavigationOnClickListener {
            startActivity(Intent(getContext(), EventsActivity::class.java))
        }

        scanning_events_button.setOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        loading_events_button.setOnClickListener {
            loading_events_button.isEnabled = false
            SyncController.loadTicketsForEvent(eventId)
        }

        scanning_event_layout.setOnRefreshListener {
            // Sync local DB with remote server
//            SyncController.synchronizeAllTables(true)

            // Hide swipe to refresh icon animation
            scanning_event_layout.isRefreshing = false
        }
    }

    private fun getLoadedSummary() {
        val event = eventsDS!!.getEvent(eventId)
        number_of_loaded.text = getString(
                R.string._1_d_of_2_d_loaded,
            ticketsDS!!.getAllTicketNumberForEvent(eventId),
            event?.totalNumOfTickets
        )
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
        startActivity(Intent(getContext(), EventsActivity::class.java))
        finish()
    }
}