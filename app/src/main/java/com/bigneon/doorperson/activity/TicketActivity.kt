package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bigneon.doorperson.R
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.AppUtils.Companion.isOfflineModeEnabled
import com.bigneon.doorperson.util.NetworkUtils
import kotlinx.android.synthetic.main.activity_ticket.*
import kotlinx.android.synthetic.main.content_ticket.*
import kotlinx.android.synthetic.main.content_ticket.view.*


class TicketActivity : AppCompatActivity() {
    private val TAG = TicketActivity::class.java.simpleName
    private var ticketsDS: TicketsDS? = null
    private var eventId: String? = null
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

    private fun checkInTicket(ticketId: String, redeemKey: String, firstName: String, lastName: String) {
        val ticket = ticketsDS!!.setCheckedTicket(ticketId)
        if (ticket != null) {
            scanning_ticket_layout.checked_status?.visibility = View.VISIBLE
            scanning_ticket_layout.purchased_status?.visibility = View.GONE
            scanning_ticket_layout.complete_check_in?.visibility = View.GONE

            Snackbar
                .make(
                    scanning_ticket_layout,
                    "Checked in ${"$lastName, $firstName"}",
                    Snackbar.LENGTH_LONG
                )
                .setDuration(5000).show()
            Log.d(TAG, "Ticket ID: ${ticket.ticketId} - CHECKED in local ")
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

    private fun redeemTicket(ticketId: String, redeemKey: String, firstName: String, lastName: String) {
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
                fun redeemTicketResult(isDuplicateTicket: Boolean, redeemedTicket: TicketModel?) {
                    if (isDuplicateTicket) {
                        ticketsDS!!.setDuplicateTicket(ticketId)
                        Log.d(TAG, "Ticket ID: $ticketId - DUPLICATE in local ")
                    } else {
                        if (redeemedTicket?.status?.toLowerCase() == getContext().getString(R.string.redeemed).toLowerCase()) {
                            ticketsDS!!.updateTicket(redeemedTicket)

                            ticketsDS!!.setRedeemedTicket(ticketId)
                            Log.d(TAG, "Ticket ID: $ticketId - REDEEMED in local ")

                            scanning_ticket_layout.redeemed_status?.visibility = View.VISIBLE
                            scanning_ticket_layout.purchased_status?.visibility = View.GONE
                            scanning_ticket_layout.complete_check_in?.visibility = View.GONE

                            Snackbar
                                .make(
                                    scanning_ticket_layout,
                                    "Redeemed ${redeemedTicket.lastName + ", " + redeemedTicket.firstName}",
                                    Snackbar.LENGTH_LONG
                                )
                                .setDuration(5000).show()
                        } else {
                            if (!isOfflineModeEnabled && !NetworkUtils.instance().isNetworkAvailable(this)) {
                                showDialog(getContext(), ticketId, redeemKey,
                                    redeemedTicket?.firstName!!, redeemedTicket.lastName!!)
                            } else {
                                Log.e(TAG, "ERROR: redeemTicketForEvent")
                            }
                        }
                    }
                }

                RestAPI.redeemTicketForEvent(
                    accessToken,
                    eventId!!,
                    ticketId,
                    firstName,
                    lastName,
                    redeemKey,
                    ::redeemTicketResult
                )
            }
        }
        RestAPI.accessToken(::setAccessToken)
    }

    private fun showDialog(context: Context, ticketId: String, redeemKey: String, firstName: String, lastName: String
    ) {
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(context)

        // set message of alert dialog
        dialogBuilder.setMessage("User ticket is NOT redeemed because offline mode has been disabled and there is no internet connection")
            .setCancelable(false)
            .setPositiveButton("Turn on the offline mode") { _, _ ->
                run {
                    isOfflineModeEnabled = true
                    checkInTicket(ticketId, redeemKey, firstName, lastName)
                }
            }
            .setNegativeButton("Turn on the WiFi") { _, _ ->
                run {
                    NetworkUtils.instance().setWiFiEnabled(this, true)
                    redeemTicket(ticketId, redeemKey, firstName, lastName)
                }
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Error in connection!")
        alert.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket)

        AppUtils.checkLogged(getContext())

        ticketsDS = TicketsDS()

        setSupportActionBar(ticket_toolbar)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val ticketId = intent.getStringExtra("ticketId")
        eventId = intent.getStringExtra("eventId")
        val redeemKey = intent.getStringExtra("redeemKey")
        searchGuestText = intent.getStringExtra("searchGuestText")
        val firstName = intent.getStringExtra("firstName")
        val lastName = intent.getStringExtra("lastName")
        val priceInCents = intent.getIntExtra("priceInCents", 0)
        val ticketType = intent.getStringExtra("ticketType")
        val status = intent.getStringExtra("status")

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
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }

        back_to_list.setOnClickListener {
            val intent = Intent(getContext(), TicketListActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }

        complete_check_in.setOnClickListener {
            when {
                NetworkUtils.instance().isNetworkAvailable(getContext()) -> redeemTicket(ticketId, redeemKey, firstName, lastName)
                isOfflineModeEnabled -> checkInTicket(ticketId, redeemKey, firstName, lastName)
                else -> {
                    showDialog(getContext(), ticketId, redeemKey, firstName, lastName)
                    Log.e(TAG, "ERROR: Internet is not available and offline mode is disabled!")
                }
            }
            SharedPrefs.setProperty(AppConstants.LAST_CHECKED_TICKET_ID + eventId, ticketId)
        }
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
        val intent = Intent(getContext(), TicketListActivity::class.java)
        intent.putExtra("eventId", eventId)
        intent.putExtra("searchGuestText", searchGuestText)
        startActivity(intent)
        finish()
    }
}