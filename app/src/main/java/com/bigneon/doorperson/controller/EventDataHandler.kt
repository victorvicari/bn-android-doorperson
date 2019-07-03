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

        //var eventList: ArrayList<EventModel> = ArrayList()
        var eventsDS: EventsDS = EventsDS()
        private var context: Context? = null


        fun setContext(con: Context) {
            context = con
        }

        fun storeEvents(events: ArrayList<EventModel>?) {
            if (events != null) {
                for (event in events) {
                    if (eventsDS.eventExists(event.id!!)) {
                        eventsDS.updateEvent(event.id!!, event.name!!, event.promoImageURL!!)
                    } else {
                        eventsDS.createEvent(event.id!!, event.name!!, event.promoImageURL!!)
                    }
                }
            }
        }

        fun loadAllEvents(): ArrayList<EventModel>? {
            when {
                context?.let { NetworkUtils.instance().isNetworkAvailable(it) }!! -> {
                    val eventList: ArrayList<EventModel> = ArrayList()
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getScannableEvents(accessToken!!)?.let { eventList.addAll(it) }
                    }.get() // get() is important to wait until doAsync is finished
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

        fun getEventByID(eventId: String): EventModel? {
            when {
                context?.let { NetworkUtils.instance().isNetworkAvailable(it) }!! -> {
                    var event: EventModel? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getEvent(accessToken!!, eventId)?.let { event = it }
                    }.get() // get() is important to wait until doAsync is finished
                    return event
                }
                isOfflineModeEnabled -> // Return events from local DB
                    return eventsDS.getEvent(eventId)
                else -> {
                    Log.e(TAG, "Getting scannable events failed")
                }
            }
            return null
        }

        fun getEventByPosition(pos: Int): EventModel? {
            when {
                context?.let { NetworkUtils.instance().isNetworkAvailable(it) }!! -> {
                    val eventList: ArrayList<EventModel> = ArrayList()
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getScannableEvents(accessToken!!)?.let { eventList.addAll(it) }
                    }.get() // get() is important to wait until doAsync is finished
                    return eventList[pos]
                }
                isOfflineModeEnabled ->  // Return events from local DB
                    return eventsDS.getAllEvents()?.get(pos)
                else -> {
                    Log.e(TAG, "Getting scannable events failed")
                }
            }
            return null
        }
    }
}