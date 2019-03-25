package com.bigneon.doorperson.auth

import android.content.Context
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.03.2019..
 ****************************************************/
object AppAuth {
    fun getAccessToken(context : Context) : String {
        return "Bearer " + SharedPrefs.getProperty(context, AppConstants.ACCESS_TOKEN).orEmpty()
    }
}