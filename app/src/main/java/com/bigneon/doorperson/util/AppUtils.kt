package com.bigneon.doorperson.util

import android.content.Context
import android.content.Intent
import com.bigneon.doorperson.activity.LoginActivity
import com.bigneon.doorperson.config.AppConstants
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

        const val MIN_TIMESTAMP = "2000-01-01T00:00:00.000000"
        const val MAX_TIMESTAMP = "2100-01-01T00:00:00.000000"

        fun getCurrentTimestamp(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("CET")
            val date = Date()
            return dateFormat.format(date)
        }

        fun checkLogged(context: Context) {
            val refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN)
             if (refreshToken.isNullOrEmpty()) {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        }
    }
}