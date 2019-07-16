package com.bigneon.doorperson.util

import android.content.Intent
import com.bigneon.doorperson.BigNeonApplication.Companion.context
import com.bigneon.doorperson.activity.LoginActivity
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.AppConstants.Companion.DATE_FORMAT
import com.bigneon.doorperson.config.SharedPrefs
import java.text.SimpleDateFormat
import java.util.*

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class AppUtils {
    companion object {
        var eventListItemPosition = -1
        var eventListItemOffset = 0
        var ticketListItemPosition = -1
        var ticketListItemOffset = 0

        private var isOfflineModeEnabled: Boolean = true

        fun isOfflineModeEnabled(): Boolean {
            return isOfflineModeEnabled
        }

        fun enableOfflineMode() {
            isOfflineModeEnabled = true
        }

        fun disableOfflineMode() {
            isOfflineModeEnabled = false
        }

        fun getCurrentTimestamp(): String {
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("CET")
            val date = Date()
            return dateFormat.format(date)
        }

        fun checkLogged() {
            val refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN)
             if (refreshToken.isNullOrEmpty()) {
                 context?.startActivity(Intent(context, LoginActivity::class.java))
            }
        }
    }
}