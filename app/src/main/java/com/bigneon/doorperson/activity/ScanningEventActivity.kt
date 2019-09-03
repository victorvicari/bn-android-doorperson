package com.bigneon.doorperson.activity

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.controller.EventDataHandler
import com.bigneon.doorperson.controller.TicketDataHandler
import com.bigneon.doorperson.controller.TicketDataHandler.Companion.addRefreshTicketsListener
import com.bigneon.doorperson.controller.TicketDataHandler.Companion.removeRefreshTicketsListener
import com.bigneon.doorperson.controller.TicketDataHandler.Companion.storeTickets
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.AppUtils.Companion.checkLogged
import com.bigneon.doorperson.util.ConnectionDialog
import com.bigneon.doorperson.util.NetworkUtils
import com.bigneon.doorperson.util.NetworkUtils.Companion.addNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.isNetworkAvailable
import com.bigneon.doorperson.util.NetworkUtils.Companion.removeNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.setWiFiEnabled
import kotlinx.android.synthetic.main.activity_scanning_event.*
import kotlinx.android.synthetic.main.content_scanning_event.*

class ScanningEventActivity : AppCompatActivity() {
    private var eventId = ""
    private var eventDataHandler: EventDataHandler? = null
    private var searchGuestText: String = ""
    private var allTicketNumberForEvent = 0
    private var isBackPressed = false

    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
                no_internet_toolbar_icon.visibility = View.GONE

                if (!isLoadingInProgress() && !isBackPressed) {
                    loading_text.text = getString(com.bigneon.doorperson.R.string.loading_tickets_is_about_to_begin)
                    loading_progress_bar.isIndeterminate = true
                    storeTickets(eventId) // download sync (create/update tickets)
                }
            }

            override fun networkUnavailable() {
                no_internet_toolbar_icon.visibility = View.VISIBLE
                loading_text.text = getString(com.bigneon.doorperson.R.string.loading_tickets_waiting_for_network)
                loading_progress_bar.isIndeterminate = true
            }
        }

    private var refreshTicketsListener: TicketDataHandler.RefreshTickets =
        object : TicketDataHandler.RefreshTickets {
            override fun updateTicket(ticketId: String, status: String) {

            }

            override fun refreshTicketList() {
                getEventSummary()
            }
        }

    private var loadingMessageReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, int: Intent?) {
                // Check if received broadcast from the proper event that is just being loaded
                if (int!!.getStringExtra("eventId") != eventId)
                    return

                when (val page = int.getIntExtra("page", 0)) {
                    0 -> {
                        loading_progress_bar.isIndeterminate = false
                        loading_progress_bar.progress = 100
                        loading_text.text =
                            if (allTicketNumberForEvent == 0)
                                "Event has no tickets to load."
                            else
                                "All $allTicketNumberForEvent have been loaded."
//                        isLoadingInProgress = false
                    }
                    else -> {
                        if (allTicketNumberForEvent > 0) {
                            loading_progress_bar.isIndeterminate = false
                            loading_progress_bar.progress =
                                (page * AppConstants.SYNC_PAGE_LIMIT * 100) / allTicketNumberForEvent
                            loading_text.text =
                                "${page * AppConstants.SYNC_PAGE_LIMIT} tickets loaded. (${loading_progress_bar.progress}%)"
//                            isLoadingInProgress = true
                        }
                    }
                }
            }
        }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_scanning_event)
        setSupportActionBar(scanning_events_toolbar)
        eventDataHandler = EventDataHandler()

        checkLogged()

        eventId = intent.getStringExtra("eventId")

        allTicketNumberForEvent = TicketDataHandler.getAllTicketNumberForEvent(getContext(), eventId) ?: 0
        LocalBroadcastManager.getInstance(this).registerReceiver(
            loadingMessageReceiver,
            IntentFilter("loading_tickets_process")
        )

        if (isNetworkAvailable(getContext())) {
            if (!isLoadingInProgress()) {
                loading_text.text = getString(com.bigneon.doorperson.R.string.loading_tickets_is_about_to_begin)
                storeTickets(eventId) // download sync (create/update tickets)
            }
        } else {
            loading_text.text = getString(com.bigneon.doorperson.R.string.loading_tickets_waiting_for_network)
        }
        loading_progress_bar.isIndeterminate = true

        searchGuestText = intent.getStringExtra("searchGuestText") ?: ""

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getEventSummary()

        scanning_events_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), com.bigneon.doorperson.R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        scanning_events_toolbar.setNavigationOnClickListener {
            if (!isLoadingInProgress()) {
                isBackPressed = true
                startActivity(Intent(getContext(), EventListActivity::class.java))
            } else {
                Snackbar
                    .make(scanning_event_layout, "Loading tickets in progress. Please wait...", Snackbar.LENGTH_SHORT)
                    .setDuration(2000).show()
            }
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
        val checkedTicketNumberForEvent = TicketDataHandler.getCheckedTicketNumberForEvent(eventId)

        if (event != null && redeemedTicketNumberForEvent != null && allTicketNumberForEvent != null) {
            scanning_event_name.text = event.name ?: ""
            number_of_redeemed.text = getString(
                com.bigneon.doorperson.R.string._1_d_of_2_d_redeemed,
                redeemedTicketNumberForEvent,
                allTicketNumberForEvent
            )
            number_of_checked.text = getString(
                com.bigneon.doorperson.R.string._1_d_checked,
                checkedTicketNumberForEvent
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
        addRefreshTicketsListener(refreshTicketsListener)
        super.onStart()
    }

    override fun onStop() {
        removeNetworkStateListener(this, networkStateReceiverListener)
        removeRefreshTicketsListener(refreshTicketsListener)
        super.onStop()
    }

    override fun onBackPressed() {
        if (!isLoadingInProgress()) {
            isBackPressed = true
            startActivity(Intent(getContext(), EventListActivity::class.java))
            finish()
        } else {
            Snackbar
                .make(scanning_event_layout, "Loading tickets in progress. Please wait...", Snackbar.LENGTH_SHORT)
                .setDuration(2000).show()
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            loadingMessageReceiver
        )
        super.onDestroy()
    }

    private fun isLoadingInProgress(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.bigneon.doorperson.service.StoreTicketsService" == service.service.className) {
                return true
            }
        }
        return false
    }
}