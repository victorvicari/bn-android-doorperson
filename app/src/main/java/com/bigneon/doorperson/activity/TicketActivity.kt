package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bigneon.doorperson.R
import com.bigneon.doorperson.db.SyncController.Companion.isOfflineModeEnabled
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.NetworkUtils
import kotlinx.android.synthetic.main.activity_ticket.*
import kotlinx.android.synthetic.main.content_ticket.*
import kotlinx.android.synthetic.main.content_ticket.view.*


class TicketActivity : AppCompatActivity() {

    private var ticketsDS: TicketsDS? = null

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket)

        ticketsDS = TicketsDS()

        setSupportActionBar(ticket_toolbar)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val ticketId = intent.getStringExtra("ticketId")
        val eventId = intent.getStringExtra("eventId")
        val redeemKey = intent.getStringExtra("redeemKey")
        val searchGuestText = intent.getStringExtra("searchGuestText")
        val firstName = intent.getStringExtra("firstName")
        val lastName = intent.getStringExtra("lastName")
        val priceInCents = intent.getIntExtra("priceInCents", 0)
        val ticketTypeName = intent.getStringExtra("ticketTypeName")
        val status = intent.getStringExtra("status")
        val position = intent.getIntExtra("position", -1)
        val offset = intent.getIntExtra("offset", 0)

        last_name_and_first_name?.text =
            getContext().getString(R.string.last_name_first_name, lastName, firstName)
        price_and_ticket_type?.text =
            getContext().getString(R.string.price_ticket_type, priceInCents.div(100), ticketTypeName)

        val ticketStatus = status?.toLowerCase()
        val statusRedeemed = getString(R.string.redeemed).toLowerCase()
        val statusChecked = getString(R.string.checked).toLowerCase()

        when (ticketStatus) {
            statusRedeemed -> {
                redeemed_status?.visibility = View.VISIBLE
                checked_status?.visibility = View.GONE
                purchased_status?.visibility = View.GONE
                complete_check_in?.visibility = View.GONE
            }
            statusChecked -> {
                redeemed_status?.visibility = View.GONE
                checked_status?.visibility = View.VISIBLE
                purchased_status?.visibility = View.GONE
                complete_check_in?.visibility = View.GONE
            }
            else -> {
                redeemed_status?.visibility = View.GONE
                checked_status?.visibility = View.GONE
                purchased_status?.visibility = View.VISIBLE
                complete_check_in?.visibility = View.VISIBLE
            }
        }

        ticket_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), com.bigneon.doorperson.R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        ticket_toolbar.setNavigationOnClickListener {
            val intent = Intent(getContext(), TicketListActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("searchGuestText", searchGuestText)
            intent.putExtra("position", position)
            intent.putExtra("offset", offset)
            startActivity(intent)
        }

        back_to_list.setOnClickListener {
            val intent = Intent(getContext(), TicketListActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("searchGuestText", searchGuestText)
            intent.putExtra("position", position)
            intent.putExtra("offset", offset)
            startActivity(intent)
        }

        fun checkInTicket() {
            val ticket = ticketsDS!!.setCheckedTicket(ticketId)
            if (ticket != null) {
                scanning_ticket_layout.checked_status?.visibility = View.VISIBLE
                scanning_ticket_layout.purchased_status?.visibility = View.GONE
                scanning_ticket_layout.complete_check_in?.visibility = View.GONE

                Snackbar
                    .make(
                        scanning_ticket_layout,
                        "Checked in ${ticket.lastName + ", " + ticket.firstName}",
                        Snackbar.LENGTH_LONG
                    )
                    .setDuration(5000).show()
            } else {
                Snackbar
                    .make(
                        scanning_ticket_layout,
                        "User ticket already redeemed! Redeem key: $redeemKey",
                        Snackbar.LENGTH_LONG
                    )
                    .setDuration(5000).show()
            }
        }

        fun redeemTicket() {
            fun setAccessToken(accessToken: String?) {
                if (accessToken == null) {
                    Snackbar
                        .make(
                            scanning_ticket_layout,
                            "Ticket is NOT redeemed. Error in connection",
                            Snackbar.LENGTH_LONG
                        )
                        .setDuration(5000).show()
                    startActivity(
                        Intent(
                            getContext(),
                            LoginActivity::class.java
                        )
                    )
                } else {
                    fun redeemTicketResult() {
                        fun getTicketResult(isRedeemed: Boolean, ticket: TicketModel?) {
                            if (isRedeemed) {
                                ticketsDS!!.setRedeemedTicket(ticketId)

                                scanning_ticket_layout.redeemed_status?.visibility = View.VISIBLE
                                scanning_ticket_layout.purchased_status?.visibility = View.GONE
                                scanning_ticket_layout.complete_check_in?.visibility = View.GONE

                                Snackbar
                                    .make(
                                        scanning_ticket_layout,
                                        "Redeemed ${ticket?.lastName + ", " + ticket?.firstName}",
                                        Snackbar.LENGTH_LONG
                                    )
                                    .setDuration(5000).show()
                            } else {
                                // build alert dialog
                                val dialogBuilder = AlertDialog.Builder(getContext())

                                // set message of alert dialog
                                dialogBuilder.setMessage("User ticket is NOT redeemed because offline mode has been disabled and there is no internet connection")
                                    .setCancelable(false)
                                    .setPositiveButton("Turn on the offline mode") { _, _ ->
                                        run {
                                            isOfflineModeEnabled = true
                                            checkInTicket()
                                        }
                                    }
                                    .setNegativeButton("Turn on the WiFi") { _, _ ->
                                        run {
                                            NetworkUtils.instance().setWiFiEnabled(getContext(), true)
                                            redeemTicket()
                                        }
                                    }
                                val alert = dialogBuilder.create()
                                alert.setTitle("Error in connection!")
                                alert.show()
                            }
                        }
                        RestAPI.getTicket(accessToken, ticketId, ::getTicketResult)
                    }

                    RestAPI.redeemTicketForEvent(
                        accessToken,
                        eventId!!,
                        ticketId!!,
                        redeemKey!!,
                        ::redeemTicketResult
                    )
                }
            }
            RestAPI.accessToken(::setAccessToken)
        }

        complete_check_in.setOnClickListener {
            if (isOfflineModeEnabled) {
                checkInTicket()
            } else {
                redeemTicket()
            }
        }
    }
}