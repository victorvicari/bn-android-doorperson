package com.bigneon.doorperson.config

import android.content.Context
import com.bigneon.doorperson.config.AppConstants.Companion.PREFS_FILENAME

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 22.03.2019..
 ****************************************************/
object SharedPrefs {
    fun getProperty(context: Context, key: String): String? {
        val prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
        return prefs.getString(key, "")
    }

    fun setProperty(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }
}
