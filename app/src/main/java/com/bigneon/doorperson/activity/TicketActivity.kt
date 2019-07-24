package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bigneon.doorperson.R
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.controller.TicketDataHandler
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.AppUtils.Companion.checkLogged
import com.bigneon.doorperson.util.AppUtils.Companion.enableOfflineMode
import com.bigneon.doorperson.util.ConnectionDialog
import com.bigneon.doorperson.util.NetworkUtils
import com.bigneon.doorperson.util.NetworkUtils.Companion.addNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.removeNetworkStateListener
import com.bigneon.doorperson.util.NetworkUtils.Companion.setWiFiEnabled
import kotlinx.android.synthetic.main.activity_ticket.*
import kotlinx.android.synthetic.main.content_ticket.*
import kotlinx.android.synthetic.main.content_ticket.view.*

class TicketActivity : AppCompatActivity() {
    private var eventId: String? = null
    private var ticketId: String? = null
    private var status: String? = null
    private var searchGuestText: String? = null
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
        setContentView(R.layout.activity_ticket)
        setSupportActionBar(ticket_toolbar)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkLogged()

        ticketId = intent.getStringExtra("ticketId")
        eventId = intent.getStringExtra("eventId")
        val redeemKey = intent.getStringExtra("redeemKey")
        val redeemedBy = intent.getStringExtra("redeemedBy")
        val redeemedAt = intent.getStringExtra("redeemedAt")
        searchGuestText = intent.getStringExtra("searchGuestText")
        val firstName = intent.getStringExtra("firstName")
        val lastName = intent.getStringExtra("lastName")
        val priceInCents = intent.getIntExtra("priceInCents", 0)
        val ticketType = intent.getStringExtra("ticketType")
        status = intent.getStringExtra("status")

        last_name_and_first_name?.text =
            getContext().getString(R.string.last_name_first_name, lastName, firstName)
        price_and_ticket_type?.text =
            getContext().getString(
                R.string.price_ticket_type,
                priceInCents.div(100),
                ticketType
            )

        ticket_id.text = "#" + ticketId?.takeLast(8)

        val ticketStatus = status?.toLowerCase()
        val statusRedeemed = getString(R.string.redeemed).toLowerCase()
        val statusChecked = getString(R.string.checked).toLowerCase()
        val statusDuplicate = getString(R.string.duplicate).toLowerCase()

        when (ticketStatus) {
            statusRedeemed -> {
                redeemed_status?.visibility = View.VISIBLE
                checked_status?.visibility = View.GONE
                checked_no_internet?.visibility = View.GONE
                duplicate_status?.visibility = View.GONE
                purchased_status?.visibility = View.GONE
                complete_check_in?.visibility = View.GONE
            }
            statusChecked -> {
                redeemed_status?.visibility = View.GONE
                checked_status?.visibility = View.VISIBLE
                checked_no_internet?.visibility = View.VISIBLE
                duplicate_status?.visibility = View.GONE
                purchased_status?.visibility = View.GONE
                complete_check_in?.visibility = View.GONE
            }
            statusDuplicate -> {
                redeemed_status?.visibility = View.GONE
                checked_status?.visibility = View.GONE
                checked_no_internet?.visibility = View.GONE
                duplicate_status?.visibility = View.VISIBLE
                purchased_status?.visibility = View.GONE
                complete_check_in?.visibility = View.GONE
            }
            else -> {
                redeemed_status?.visibility = View.GONE
                checked_status?.visibility = View.GONE
                checked_no_internet?.visibility = View.GONE
                duplicate_status?.visibility = View.GONE
                purchased_status?.visibility = View.VISIBLE
                complete_check_in?.visibility = View.VISIBLE
            }
        }

        ticket_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        ticket_toolbar.setNavigationOnClickListener {
            val intent = Intent(getContext(), TicketListActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("ticketId", ticketId)
            intent.putExtra("status", status)
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }

        back_to_list.setOnClickListener {
            val intent = Intent(getContext(), TicketListActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("ticketId", ticketId)
            intent.putExtra("status", status)
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }

        fun completeCheckIn() {
            val ticket = TicketModel()
            ticket.eventId = eventId!!
            ticket.ticketId = ticketId!!
            ticket.redeemKey = redeemKey!!
            ticket.firstName = firstName!!
            ticket.lastName = lastName!!
            when (TicketDataHandler.completeCheckIn(getContext(), ticket)) {
                TicketDataHandler.TicketState.REDEEMED -> {
                    status = statusRedeemed
                    scanning_ticket_layout.redeemed_status?.visibility = View.VISIBLE
                    scanning_ticket_layout.purchased_status?.visibility = View.GONE
                    scanning_ticket_layout.complete_check_in?.visibility = View.GONE
                    scanning_ticket_layout.duplicate_status?.visibility = View.GONE

                    Snackbar
                        .make(
                            scanning_ticket_layout,
                            "Redeemed ${"$lastName, $firstName"}",
                            Snackbar.LENGTH_LONG
                        )
                        .setDuration(5000).show()
                }
                TicketDataHandler.TicketState.CHECKED -> {
                    status = statusChecked
                    scanning_ticket_layout.checked_status?.visibility = View.VISIBLE
                    scanning_ticket_layout.purchased_status?.visibility = View.GONE
                    scanning_ticket_layout.complete_check_in?.visibility = View.GONE
                    scanning_ticket_layout.duplicate_status?.visibility = View.GONE

                    Snackbar
                        .make(
                            scanning_ticket_layout,
                            "Checked in ${"$lastName, $firstName"}",
                            Snackbar.LENGTH_LONG
                        )
                        .setDuration(5000).show()
                }
                TicketDataHandler.TicketState.DUPLICATED -> {
                    status = statusDuplicate
                    scanning_ticket_layout.checked_status?.visibility = View.GONE
                    scanning_ticket_layout.purchased_status?.visibility = View.GONE
                    scanning_ticket_layout.complete_check_in?.visibility = View.GONE
                    scanning_ticket_layout.duplicate_status?.visibility = View.VISIBLE

                    val intent = Intent(getContext(), DuplicateTicketCheckinActivity::class.java)
                    intent.putExtra("ticketId", ticketId)
                    intent.putExtra("lastAndFirstName", "${ticket.lastName!!}, ${ticket.firstName!!}")
                    intent.putExtra("redeemedBy", ticket.redeemedBy)
                    intent.putExtra("redeemedAt", ticket.redeemedAt)
                    startActivity(intent)

                    Snackbar
                        .make(
                            scanning_ticket_layout,
                            "Warning: Ticket redeemed by $redeemedBy ${AppUtils.getTimeAgo(redeemedAt!!)}",
                            Snackbar.LENGTH_LONG
                        )
                        .setDuration(5000).show()
                }
                TicketDataHandler.TicketState.ERROR -> {
                    object : ConnectionDialog() {
                        override fun positiveButtonAction(context: Context) {
                            enableOfflineMode()
                            completeCheckIn()
                        }

                        override fun negativeButtonAction(context: Context) {
                            setWiFiEnabled(getContext())
                            while (!NetworkUtils.isNetworkAvailable(context)) Thread.sleep(1000)
                            completeCheckIn()
                        }
                    }.showDialog(getContext())
                }
            }
            SharedPrefs.setProperty(AppConstants.LAST_CHECKED_TICKET_ID + eventId, ticketId)
        }

        complete_check_in.setOnClickListener {
            completeCheckIn()
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
        val intent = Intent(getContext(), TicketListActivity::class.java)
        intent.putExtra("eventId", eventId)
        intent.putExtra("ticketId", ticketId)
        intent.putExtra("status", status)
        intent.putExtra("searchGuestText", searchGuestText)
        startActivity(intent)
        finish()
    }
}