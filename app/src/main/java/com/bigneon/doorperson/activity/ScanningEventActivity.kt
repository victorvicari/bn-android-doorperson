package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.bigneon.doorperson.R
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.util.AppUtils
import kotlinx.android.synthetic.main.activity_scanning_event.*
import kotlinx.android.synthetic.main.content_scanning_event.*

class ScanningEventActivity : AppCompatActivity() {
    private var eventId: String = ""
    private var ticketsDS: TicketsDS? = null

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

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scanning_events_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        scanning_events_toolbar.setNavigationOnClickListener {
            startActivity(Intent(getContext(), EventsActivity::class.java))
        }

        number_of_redeemed.text = getString(
            R.string._1_d_of_2_d_redeemed,
            ticketsDS!!.getRedeemedTicketNumberForEvent(eventId),
            ticketsDS!!.getAllTicketNumberForEvent(eventId)
        )

        number_of_checked.text = getString(
            R.string._1_d_checked,
            ticketsDS!!.getCheckedTicketNumberForEvent(eventId)
        )

        scanning_events_button.setOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.checkLogged(getContext())
    }
}