//package com.bigneon.doorperson
//
//import android.app.Application
//import android.content.Context
//import com.bigneon.doorperson.config.AppConstants
//import com.bigneon.doorperson.config.SharedPrefs
//
///****************************************************
// * Copyright (c) 2016 - 2019.
// * All right reserved!
// * Created by SRKI-ST on 09.04.2019..
// ****************************************************/
//class DoorpersonApplication : Application() {
//    init {
//        instance = this
//    }
//
//    companion object {
//        private var instance: DoorpersonApplication? = null
//
//        fun applicationContext(): Context {
//            return instance!!.applicationContext
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        SharedPrefs.setProperty(AppConstants.REFRESH_TOKEN, null)
//        val context: Context = DoorpersonApplication.applicationContext()
//
//
//    }
//}