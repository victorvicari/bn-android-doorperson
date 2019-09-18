package com.bigneon.studio

import android.app.Application
import android.content.Context

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.06.2019..
 ****************************************************/
class BigNeonApplication : Application() {

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    companion object {
        var instance: BigNeonApplication? = null
            private set

        // or return instance.getApplicationContext();
        val context: Context?
            get() = instance
    }
}