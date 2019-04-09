package com.bigneon.doorperson

import android.app.Application
import android.content.Context
import android.content.Intent
import com.bigneon.doorperson.activity.LoginActivity
import com.bigneon.doorperson.rest.RestAPISync

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class DoorpersonApplication : Application() {
    init {
        instance = this
    }

    companion object {
        private var instance: DoorpersonApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        val context: Context = DoorpersonApplication.applicationContext()

        if(RestAPISync.accessToken() == null) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }
}