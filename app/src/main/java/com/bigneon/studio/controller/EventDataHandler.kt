package com.bigneon.studio.controller

import android.content.Context
import android.util.Log
import com.bigneon.studio.db.ds.EventsDS
import com.bigneon.studio.rest.RestAPI
import com.bigneon.studio.rest.model.EventModel
import com.bigneon.studio.util.AppUtils.Companion.isOfflineModeEnabled
import com.bigneon.studio.util.NetworkUtils.Companion.isNetworkAvailable
import org.jetbrains.anko.doAsync

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 28.06.2019..
 ****************************************************/
class EventDataHandler {
    private val TAG = EventDataHandler::class.java.simpleName
    private var eventsDS: EventsDS = EventsDS()

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

    fun loadAllEvents(context: Context): ArrayList<EventModel>? {
        when {
            isNetworkAvailable(context) -> {
                val eventList: ArrayList<EventModel> = ArrayList()
                doAsync {
                    val accessToken: String? = RestAPI.accessToken()
                    RestAPI.getScannableEvents(accessToken!!)?.let { eventList.addAll(it) }
                }.get() // get() is important to wait until doAsync is finished
                return eventList
            }
            isOfflineModeEnabled() -> // Return events from local DB
                return eventsDS.getAllEvents()
            else -> {
                Log.e(TAG, "Getting scannable events failed")
                return null
            }
        }
    }

    fun getEventByID(context: Context, eventId: String): EventModel? {
        when {
            isNetworkAvailable(context) -> {
                var event: EventModel? = null
                doAsync {
                    val accessToken: String? = RestAPI.accessToken()
                    RestAPI.getEvent(accessToken!!, eventId)?.let { event = it }
                }.get() // get() is important to wait until doAsync is finished
                return event
            }
            isOfflineModeEnabled() -> // Return events from local DB
                return eventsDS.getEvent(eventId)
            else -> {
                Log.e(TAG, "Getting scannable events failed")
                return null
            }
        }
    }

    fun getEventByPosition(context: Context, pos: Int): EventModel? {
        when {
            isNetworkAvailable(context) -> {
                val eventList: ArrayList<EventModel> = ArrayList()
                doAsync {
                    val accessToken: String? = RestAPI.accessToken()
                    RestAPI.getScannableEvents(accessToken!!)?.let { eventList.addAll(it) }
                }.get() // get() is important to wait until doAsync is finished
                return eventList[pos]
            }
            isOfflineModeEnabled() ->  // Return events from local DB
                return eventsDS.getAllEvents()?.get(pos)
            else -> {
                Log.e(TAG, "Getting scannable events failed")
                return null
            }
        }
    }
}