package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bigneon.doorperson.R
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.rest.RestAPI
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*

class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.simpleName

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SharedPrefs.setContext(this)
        super.onCreate(savedInstanceState)

        SharedPrefs.removeProperty(AppConstants.REFRESH_TOKEN)
        println(SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN))
        val refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN) ?: ""

        if (refreshToken != "") {
            startActivity(Intent(getContext(), EventsActivity::class.java))
        } else {
            setContentView(com.bigneon.doorperson.R.layout.activity_login)
        }


//        setSupportActionBar(login_toolbar)
        //this line shows back button
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)


//        login_toolbar.navigationIcon!!.setColorFilter(
//            ContextCompat.getColor(getContext(), R.color.colorBlack),
//            PorterDuff.Mode.SRC_ATOP
//        )
//
//        login_toolbar.setNavigationOnClickListener {
//            finishAffinity() // Exit the app
//        }
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
                }
            }
            RestAPI.authenticate(email, password, ::setAccessToken)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }
}
