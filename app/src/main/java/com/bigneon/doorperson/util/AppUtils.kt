package com.bigneon.doorperson.util

import java.text.SimpleDateFormat
import java.util.*

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class AppUtils {
    companion object {
        fun getCurrentTimestamp(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)
            dateFormat.timeZone = TimeZone.getTimeZone("CET")
            val date = Date()
            return dateFormat.format(date)
        }
    }
}