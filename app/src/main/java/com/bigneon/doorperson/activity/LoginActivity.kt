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
import com.bigneon.doorperson.rest.RestAPISync
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.view.*

class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.simpleName

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_login)
        setSupportActionBar(login_toolbar)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        login_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorBlack),
            PorterDuff.Mode.SRC_ATOP
        )

        login_toolbar.setNavigationOnClickListener {
            finishAffinity() // Exit the app
        }
    }

    fun btnLoginClick(@Suppress("UNUSED_PARAMETER") view: View) {
        try {
            val email = if (view.email_address.text != null) view.email_address.text.toString() else ""
            val password = if (view.password.text != null) view.password.text.toString() else ""
            val accessToken = RestAPISync.authenticate(email, password)
            if(accessToken == null) {
                Snackbar
                    .make(view, "Username and/or password does not match!", Snackbar.LENGTH_LONG)
                    .setDuration(5000).show()
                getContext().startActivity(Intent(getContext(), LoginActivity::class.java))
            } else {
                getContext().startActivity(Intent(getContext(), EventsActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }
}
