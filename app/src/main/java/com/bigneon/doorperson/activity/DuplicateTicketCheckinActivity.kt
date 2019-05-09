package com.bigneon.doorperson.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bigneon.doorperson.R
import kotlinx.android.synthetic.main.content_duplicate_ticket_checkin.*

class DuplicateTicketCheckinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duplicate_ticket_checkin)

        val ticketId = intent.getStringExtra("ticketId") ?: "UNKNOWN"
        duplicate_ticket_msg.text = getString(R.string.duplicate_ticket_msg, ticketId)
        duplicate_ticket_go_back.setOnClickListener {
            finish()
        }
    }

}
