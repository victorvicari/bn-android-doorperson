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
import com.bigneon.doorperson.db.ds.TicketsDS
import kotlinx.android.synthetic.main.activity_guest.*
import kotlinx.android.synthetic.main.content_guest.*
import kotlinx.android.synthetic.main.content_guest.view.*

class TicketActivity : AppCompatActivity() {

    private var ticketsDS: TicketsDS? = null

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest)

        ticketsDS = TicketsDS()

        setSupportActionBar(guest_toolbar)

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

        if (status?.toLowerCase() == "redeemed") {
            redeemed_status?.visibility = View.VISIBLE
            purchased_status?.visibility = View.GONE
            complete_check_in?.visibility = View.GONE
        } else {
            redeemed_status?.visibility = View.GONE
            purchased_status?.visibility = View.VISIBLE
            complete_check_in?.visibility = View.VISIBLE
        }

        guest_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), com.bigneon.doorperson.R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        guest_toolbar.setNavigationOnClickListener {
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

        complete_check_in.setOnClickListener {
            val redeemedTicket = ticketsDS!!.setRedeemTicket(ticketId)
            if (redeemedTicket != null) {
                scanning_guest_layout.redeemed_status?.visibility = View.VISIBLE
                scanning_guest_layout.purchased_status?.visibility = View.GONE
                scanning_guest_layout.complete_check_in?.visibility = View.GONE

                Snackbar
                    .make(
                        scanning_guest_layout,
                        "Checked in ${redeemedTicket.lastName + ", " + redeemedTicket.firstName}",
                        Snackbar.LENGTH_LONG
                    )
                    .setDuration(5000).show()
            } else {
                Snackbar
                    .make(
                        scanning_guest_layout,
                        "User ticket already redeemed! Redeem key: $redeemKey",
                        Snackbar.LENGTH_LONG
                    )
                    .setDuration(5000).show()
            }
        }
    }
}
