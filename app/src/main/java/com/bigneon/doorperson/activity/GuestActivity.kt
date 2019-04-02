package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bigneon.doorperson.R
import com.bigneon.doorperson.rest.RestAPI
import kotlinx.android.synthetic.main.activity_guest.*
import kotlinx.android.synthetic.main.content_guest.*

class GuestActivity : AppCompatActivity() {

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest)

        setSupportActionBar(guest_toolbar)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val ticketId = intent.getStringExtra("id")
        val eventId = intent.getStringExtra("eventId")
        val redeemKey = intent.getStringExtra("redeemKey")
        val searchGuestText = intent.getStringExtra("searchGuestText")
        val firstName = intent.getStringExtra("firstName")
        val lastName = intent.getStringExtra("lastName")
        val priceInCents = intent.getIntExtra("priceInCents", 0)
        val ticketType = intent.getStringExtra("ticketType")
        val status = intent.getStringExtra("status")

        last_name_and_first_name?.text =
            getContext().getString(R.string.last_name_first_name, lastName, firstName)
        price_and_ticket_type?.text =
            getContext().getString(R.string.price_ticket_type, priceInCents.div(100), ticketType)

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
            val intent = Intent(getContext(), GuestListActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }

        back_to_list.setOnClickListener {
            val intent = Intent(getContext(), GuestListActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }

        complete_check_in.setOnClickListener {
            RestAPI.redeemTicketForEvent(getContext(), scanning_guest_layout, eventId, ticketId, redeemKey)
        }
    }
}
