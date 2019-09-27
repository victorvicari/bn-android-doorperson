package com.bigneon.studio.util

import android.content.Intent
import com.bigneon.studio.BigNeonApplication.Companion.context
import com.bigneon.studio.activity.LoginActivity
import com.bigneon.studio.config.AppConstants
import com.bigneon.studio.config.AppConstants.Companion.DATE_FORMAT_MS
import com.bigneon.studio.config.SharedPrefs
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

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
            val dateFormat = SimpleDateFormat(DATE_FORMAT_MS, Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("CET")
            val date = Date()
            return dateFormat.format(date)
        }

        fun getTimeAgo(redeemedAt: String): String {
            val formatLocal = SimpleDateFormat(DATE_FORMAT_MS, Locale.ENGLISH)
            val formatUTC = SimpleDateFormat(DATE_FORMAT_MS, Locale.ENGLISH)
            formatUTC.timeZone = TimeZone.getTimeZone("UTC")

            val redeemedDate = formatLocal.parse(redeemedAt)
            val nowDate = formatLocal.parse(formatUTC.format(Date()))
            val diffInMilliseconds = abs(nowDate.time - redeemedDate.time)

            val seconds = diffInMilliseconds / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            var showBegun = false
            val redeemedAtBuilder = StringBuilder()
            if (days > 0) {
                redeemedAtBuilder.append("$days d ")
                showBegun = true
            }
            if (hours % 24 > 0 || showBegun) {
                redeemedAtBuilder.append("${hours % 24}h ")
                showBegun = true
            }
            if (minutes % 60 > 0 || showBegun) {
                redeemedAtBuilder.append("${minutes % 60}m ")
                showBegun = true
            }
            if (seconds % 60 > 0 || showBegun) {
                redeemedAtBuilder.append("${seconds % 60}s ")
            }
            redeemedAtBuilder.append("ago")
            return redeemedAtBuilder.toString()
        }

        fun checkLogged() {
            val refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN)
            if (refreshToken.isNullOrEmpty()) {
                context?.startActivity(Intent(context, LoginActivity::class.java))
            }
        }
    }
}