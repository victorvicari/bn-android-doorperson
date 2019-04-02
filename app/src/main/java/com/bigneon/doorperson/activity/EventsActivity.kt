package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.bigneon.doorperson.R
import com.bigneon.doorperson.config.AppConstants.Companion.REFRESH_TOKEN
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.rest.RestAPI
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.content_events.*


class EventsActivity : AppCompatActivity() {
    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_events)
        val refreshToken = SharedPrefs.getProperty(getContext(), REFRESH_TOKEN)

        setSupportActionBar(events_toolbar)
        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        events_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        events_toolbar.setNavigationOnClickListener {
            startActivity(Intent(getContext(), LoginActivity::class.java))
        }

        // If there is no refresh token, then user isn't logged in
        if (refreshToken.equals("")) {
            startActivity(Intent(getContext(), LoginActivity::class.java))
        } else {
            RestAPI.getScannableEvents(getContext(), events_layout)
        }
    }
}
