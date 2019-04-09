package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.bigneon.doorperson.config.AppConstants.Companion.REFRESH_TOKEN
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.rest.RestAPI
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.content_events.*

class EventsActivity : AppCompatActivity() {
    private var networkStateReceiver: NetworkStateReceiver = NetworkStateReceiver()
    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
                Toast.makeText(getContext(), "Network is available!", Toast.LENGTH_LONG).show()
            }

            override fun networkUnavailable() {
                Toast.makeText(getContext(), "Network is unavailable!", Toast.LENGTH_LONG).show()
            }
        }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_events)

        networkStateReceiver.addListener(networkStateReceiverListener)
        registerReceiver(networkStateReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        val refreshToken = SharedPrefs.getProperty(REFRESH_TOKEN)

        setSupportActionBar(events_toolbar)
        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        events_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), com.bigneon.doorperson.R.color.colorAccent),
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

    override fun onDestroy() {
        networkStateReceiver.removeListener(this.networkStateReceiverListener)
        unregisterReceiver(networkStateReceiver)
        super.onDestroy()
    }
}
