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
        val lastAndFirstName = intent.getStringExtra("lastAndFirstName") ?: ""
        val redeemedBy = intent.getStringExtra("redeemedBy") ?: ""
        val redeemedAt = intent.getStringExtra("redeemedAt") ?: ""

        duplicate_ticket_msg.text = getString(
            R.string.duplicate_ticket_msg,
            "#" + ticketId.takeLast(8),
            lastAndFirstName,
            redeemedBy,
            redeemedAt
        )
        duplicate_ticket_go_back.setOnClickListener {
            finish()
        }
    }

}
