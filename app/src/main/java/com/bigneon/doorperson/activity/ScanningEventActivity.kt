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
import com.bigneon.doorperson.controller.TicketDataHandler.Companion.storeTickets
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.AppUtils.Companion.checkLogged
import com.bigneon.doorperson.util.ConnectionDialog
import com.bigneon.doorperson.util.NetworkUtils
import com.bigneon.doorperson.util.NetworkUtils.Companion.addNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.removeNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.setWiFiEnabled
import kotlinx.android.synthetic.main.activity_scanning_event.*
import kotlinx.android.synthetic.main.content_scanning_event.*

class ScanningEventActivity : AppCompatActivity() {
    private var eventId: String = ""
    private var eventDataHandler: EventDataHandler? = null
    private var searchGuestText: String = ""

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
        eventDataHandler = EventDataHandler()

        checkLogged()

        eventId = intent.getStringExtra("eventId") ?: ""
        storeTickets(eventId) // download sync (create/update tickets)

        searchGuestText = intent.getStringExtra("searchGuestText") ?: ""

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getEventSummary()

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
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }

        scanning_event_layout.setOnRefreshListener {
            if (scanning_event_layout.isEnabled) {
                scanning_event_layout.isEnabled = false
                scanning_event_layout.isRefreshing = true
            }
        }
    }

    private fun getEventSummary() {
        val event = eventDataHandler?.getEventByID(getContext(), eventId)
        val redeemedTicketNumberForEvent = TicketDataHandler.getRedeemedTicketNumberForEvent(getContext(), eventId)
        val allTicketNumberForEvent = TicketDataHandler.getAllTicketNumberForEvent(getContext(), eventId)

        if (event != null && redeemedTicketNumberForEvent != null && allTicketNumberForEvent != null) {
            scanning_event_name.text = event.name ?: ""
            number_of_redeemed.text = getString(
                R.string._1_d_of_2_d_redeemed,
                redeemedTicketNumberForEvent,
                allTicketNumberForEvent
            )
            number_of_checked.text = getString(
                R.string._1_d_checked,
                TicketDataHandler.getCheckedTicketNumberForEvent(eventId)
            )
        } else {
            object : ConnectionDialog() {
                override fun positiveButtonAction(context: Context) {
                    AppUtils.enableOfflineMode()
                    getEventSummary()
                }

                override fun negativeButtonAction(context: Context) {
                    setWiFiEnabled(context)
                    while (!NetworkUtils.isNetworkAvailable(context)) Thread.sleep(1000)
                    getEventSummary()
                }
            }.showDialog(getContext())

        }

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