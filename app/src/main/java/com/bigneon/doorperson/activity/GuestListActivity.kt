package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.bigneon.doorperson.R
import kotlinx.android.synthetic.main.activity_guest_list.*

class GuestListActivity : AppCompatActivity() {
    private val TAG = GuestListActivity::class.java.simpleName
    private var eventId: String = ""

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_list)

        setSupportActionBar(guest_list_toolbar)

        eventId = intent.getStringExtra("eventId")

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        guest_list_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        guest_list_toolbar.setNavigationOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }
    }
}
