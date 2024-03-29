package com.bigneon.studio.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bigneon.studio.config.AppConstants
import com.bigneon.studio.config.SharedPrefs
import com.bigneon.studio.db.SQLiteHelper
import com.bigneon.studio.rest.RestAPI
import com.bigneon.studio.rest.response.UserInfoResponse
import com.bigneon.studio.util.AppUtils.Companion.checkLogged
import com.bigneon.studio.util.AppUtils.Companion.disableOfflineMode
import com.bigneon.studio.util.AppUtils.Companion.enableOfflineMode
import com.bigneon.studio.util.AppUtils.Companion.isOfflineModeEnabled
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.content_profile.*
import org.jetbrains.anko.doAsync


class ProfileActivity : AppCompatActivity() {
    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.studio.R.layout.activity_profile)
        setSupportActionBar(profile_settings_toolbar)

        checkLogged()

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        profile_settings_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), com.bigneon.studio.R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        profile_settings_toolbar.setNavigationOnClickListener {
            startActivity(Intent(getContext(), EventListActivity::class.java))
        }

        logout_button.setOnClickListener {
            SharedPrefs.setProperty(AppConstants.REFRESH_TOKEN, null)
            startActivity(Intent(getContext(), LoginActivity::class.java))
        }

        offline_mode_label.text =
            if (isOfflineModeEnabled()) getString(com.bigneon.studio.R.string.offline_mode_enabled) else getString(
                com.bigneon.studio.R.string.offline_mode_disabled
            )
        offline_mode_button_label.text =
            if (isOfflineModeEnabled()) getString(com.bigneon.studio.R.string.disable_offline_mode) else getString(
                com.bigneon.studio.R.string.enable_offline_mode
            )
        offline_mode_button.setOnClickListener {
            if (isOfflineModeEnabled()) {
                disableOfflineMode()
            } else {
                enableOfflineMode()
            }
            offline_mode_label.text =
                if (isOfflineModeEnabled()) getString(com.bigneon.studio.R.string.offline_mode_enabled) else getString(
                    com.bigneon.studio.R.string.offline_mode_disabled
                )
            offline_mode_button_label.text =
                if (isOfflineModeEnabled()) getString(com.bigneon.studio.R.string.disable_offline_mode) else getString(
                    com.bigneon.studio.R.string.enable_offline_mode
                )
        }

        // Show admin panel for admin users
        var userInfo: UserInfoResponse? = null
        doAsync {
            val accessToken: String? = RestAPI.accessToken()
            RestAPI.getUserInfo(accessToken!!)?.let { userInfo = it }
        }.get() // get() is important to wait until doAsync is finished
        if (userInfo != null) {
            if (userInfo!!.roles?.contains("Admin")!!) {
                var baseURL = SharedPrefs.getProperty("BASE_URL")
                if (baseURL.isNullOrBlank()) {
                    baseURL = AppConstants.BASE_URL
                }
                admin_panel_base_url.setText(baseURL)

                // Show admin panel
                admin_panel.visibility = View.VISIBLE
            }

            admin_panel_base_url_button.setOnClickListener {
                SharedPrefs.setProperty("BASE_URL", admin_panel_base_url.text.toString())
                SQLiteHelper.deleteDB()
                SharedPrefs.setProperty(AppConstants.REFRESH_TOKEN, null)
                startActivity(Intent(getContext(), LoginActivity::class.java))
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(getContext(), EventListActivity::class.java))
        finish()
    }
}
