package com.bigneon.doorperson.config

import android.annotation.SuppressLint
import android.content.Context
import com.bigneon.doorperson.config.AppConstants.Companion.PREFS_FILENAME

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 22.03.2019..
 ****************************************************/
class SharedPrefs {
    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun setContext(con: Context) {
            context = con
        }

        fun getProperty(key: String): String? {
            val prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
            return prefs.getString(key, "")
        }

        fun setProperty(key: String, value: String?) {
            val prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
            val editor = prefs.edit()
            editor.putString(key, value)
            editor.apply()
        }
    }
}
