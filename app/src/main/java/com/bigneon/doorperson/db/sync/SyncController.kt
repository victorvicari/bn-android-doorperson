package com.bigneon.doorperson.db.sync

import android.os.AsyncTask
import android.widget.Toast
import com.bigneon.doorperson.DoorpersonApplication
import com.bigneon.doorperson.db.ds.EventsDS
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPISync

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SyncController {
    //    val syncDS: SyncDS = SyncDS()
    val eventsDS: EventsDS = EventsDS()
    val ticketsDS: TicketsDS = TicketsDS()

    fun synchronizeAllTables() {
        // Download synchronization
        EventDownloadSynchronizationTask().execute()
        GuestDownloadSynchronizationTask().execute()

        // Upload synchronization
        GuestUploadSynchronizationTask().execute()
    }

    // Download synchronization
    inner class EventDownloadSynchronizationTask : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            // get all events from remote DB
            val events = RestAPISync.getScannableEvents()

            // for all events
            events?.forEach {
                if (eventsDS.eventExists(it.id!!)) {
                    eventsDS.updateEvent(it.id!!, it.name!!, it.promoImageURL!!)
                } else {
                    eventsDS.createEvent(it.id!!, it.name!!, it.promoImageURL!!)
                }
            }
            Toast.makeText(
                DoorpersonApplication.applicationContext(),
                "Events has been synchronized",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    inner class GuestDownloadSynchronizationTask : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            val events = RestAPISync.getScannableEvents()
            events?.forEach {e ->
                val eventId = e.id ?: ""
                val tickets = RestAPISync.getTicketsForEvent(eventId)

                // for all events
                tickets?.forEach {
                    if (ticketsDS.ticketExists(it.id!!)) {
                        ticketsDS.updateTicket(it.id!!, it.priceInCents!!, it.ticketTypeName!!, it.redeemKey!!, it.status!!)
                    } else {
                        ticketsDS.createTicket(it.id!!, it.priceInCents!!, it.ticketTypeName!!, it.redeemKey!!, it.status!!)
                    }
                }
                Toast.makeText(
                    DoorpersonApplication.applicationContext(),
                    "Tickets for event $eventId has been synchronized",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    // Upload synchronization
    inner class GuestUploadSynchronizationTask : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {

        }
    }


}
