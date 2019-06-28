package com.bigneon.doorperson.controller

import android.content.Context
import android.util.Log
import com.bigneon.doorperson.db.ds.EventsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.EventModel
import com.bigneon.doorperson.util.AppUtils.Companion.isOfflineModeEnabled
import com.bigneon.doorperson.util.NetworkUtils
import org.jetbrains.anko.doAsync

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 28.06.2019..
 ****************************************************/
class EventDataHandler {
    companion object {
        private val TAG = EventDataHandler::class.java.simpleName

        private var eventList: ArrayList<EventModel> = ArrayList()
        private var eventsDS: EventsDS = EventsDS()
        private var context: Context? = null


        fun setContext(con: Context) {
            context = con
        }

        fun loadAllEvents(): ArrayList<EventModel>? {
            when {
                context?.let { NetworkUtils.instance().isNetworkAvailable(it) }!! -> {
                    doAsync {
                        eventList.clear()
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getScannableEvents(accessToken!!)?.let { eventList.addAll(it) }
                    }.get()
                    return eventList
                }
                isOfflineModeEnabled -> // Return events from local DB
                    return eventsDS.getAllEvents()
                else -> {
                    Log.e(TAG, "Getting scannable events failed")
                }
            }
            return null
        }

        fun getAllEvents(): ArrayList<EventModel>? {
            return eventList
        }
    }
}