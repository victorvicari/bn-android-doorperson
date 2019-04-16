package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.bigneon.doorperson.R
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.content_profile.*

class ProfileActivity : AppCompatActivity() {
    private val TAG = ProfileActivity::class.java.simpleName
    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(profile_settings_toolbar)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        profile_settings_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        profile_settings_toolbar.setNavigationOnClickListener {
            startActivity(Intent(getContext(), EventsActivity::class.java))
        }

        logout_button.setOnClickListener {
            SharedPrefs.setProperty(AppConstants.REFRESH_TOKEN, null)
            startActivity(Intent(getContext(), LoginActivity::class.java))
        }

        offline_sync_button.setOnClickListener {
            Toast.makeText(getContext(), "To be implemented...", Toast.LENGTH_LONG).show()
        }
    }
}
