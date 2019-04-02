package com.bigneon.doorperson.activity

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bigneon.doorperson.R
import com.bigneon.doorperson.rest.RestAPI
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*

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
            RestAPI.authenticate(getContext(), login_layout)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }
}
