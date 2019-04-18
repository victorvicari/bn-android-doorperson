package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.util.NetworkUtils
import kotlinx.android.synthetic.main.content_login.*


class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.simpleName
    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
                turn_on_wifi.visibility = View.GONE
            }

            override fun networkUnavailable() {
                turn_on_wifi.visibility = View.VISIBLE
            }
        }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_login)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        NetworkUtils.instance().addNetworkStateListener(getContext(), networkStateReceiverListener)

        turn_on_wifi.setOnClickListener {
            NetworkUtils.instance().setWiFiEnabled(getContext(), true)
        }
    }

    fun btnLoginClick(@Suppress("UNUSED_PARAMETER") view: View) {
        try {
            val email = email_address.text.toString()
            val password = password.text.toString()
            fun setAccessToken(accessToken: String?) {
                if (accessToken == null) {
                    Snackbar
                        .make(view, "Username and/or password does not match!", Snackbar.LENGTH_LONG)
                        .setDuration(5000).show()
                    startActivity(Intent(getContext(), LoginActivity::class.java))
                } else {
                    startActivity(Intent(getContext(), EventsActivity::class.java))
                    finish()
                }
            }
            RestAPI.authenticate(email, password, ::setAccessToken)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }
}
