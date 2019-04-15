package com.bigneon.doorperson.db.sync

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.widget.Toast
import com.bigneon.doorperson.R
import com.bigneon.doorperson.activity.LoginActivity
import com.bigneon.doorperson.db.ds.EventsDS
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.EventModel
import com.bigneon.doorperson.rest.model.TicketModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SyncController {
    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun setContext(con: Context) {
            context = con
        }
    }

    fun synchronizeAllTables() {
        SynchronizeAllTablesTask(context).execute()
    }
}

class SynchronizeAllTablesTask(@SuppressLint("StaticFieldLeak") private val context: Context) :
    AsyncTask<Unit, Unit, Unit>() {
    private val eventsDS: EventsDS = EventsDS()
    private val ticketsDS: TicketsDS = TicketsDS()

    override fun doInBackground(vararg params: Unit?) {
        // Upload synchronization
        ticketUploadSynchronization()

        // Download synchronization
        eventDownloadSynchronization()
        ticketDownloadSynchronization()
    }

    private fun ticketUploadSynchronization() {
        fun setAccessTokenForEvent(accessToken: String?) {
            val checkedTickets = ticketsDS.getAllCheckedTickets()
            checkedTickets?.forEach() { t ->
                RestAPI.redeemTicketForEvent(accessToken!!, t.eventId!!, t.ticketId!!, t.redeemKey!!)
            }
        }
        RestAPI.accessToken(::setAccessTokenForEvent)
    }

    private fun eventDownloadSynchronization() {
        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken == null)
                context.startActivity(
                    Intent(
                        context,
                        LoginActivity::class.java
                    )
                )
            else {
                fun setEvents(events: ArrayList<EventModel>?) {
                    // for all events
                    events?.forEach {
                        if (eventsDS.eventExists(it.id!!)) {
                            eventsDS.updateEvent(it.id!!, it.name!!, it.promoImageURL!!)
                        } else {
                            eventsDS.createEvent(it.id!!, it.name!!, it.promoImageURL!!)
                        }
                    }

                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.events_synchronized),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                RestAPI.getScannableEvents(accessToken, ::setEvents)
            }
        }
        RestAPI.accessToken(::setAccessTokenForEvent)
    }

    private fun ticketDownloadSynchronization() {
        fun setAccessTokenForTicket(accessToken: String?) {
            if (accessToken == null)
                context.startActivity(
                    Intent(
                        context,
                        LoginActivity::class.java
                    )
                )
            else {
                fun setEvents(events: ArrayList<EventModel>?) {
                    // for all events
                    events?.forEach { e ->
                        fun setTickets(tickets: ArrayList<TicketModel>?) {
                            // for all tickets in the event
                            tickets?.forEach { t ->
                                if (ticketsDS.ticketExists(t.ticketId!!)) {
                                    ticketsDS.updateTicket(
                                        t.ticketId!!,
                                        t.eventId!!,
                                        t.firstName!!,
                                        t.lastName!!,
                                        t.priceInCents!!,
                                        t.ticketType!!,
                                        t.redeemKey!!,
                                        t.status?.toUpperCase()!!
                                    )
                                } else {
                                    ticketsDS.createTicket(
                                        t.ticketId!!,
                                        t.eventId!!,
                                        t.firstName!!,
                                        t.lastName!!,
                                        t.priceInCents!!,
                                        t.ticketType!!,
                                        t.redeemKey!!,
                                        t.status?.toUpperCase()!!
                                    )
                                }
                            }
                        }
                        RestAPI.getTicketsForEvent(accessToken, e.id!!, ::setTickets)
                    }
                }
                RestAPI.getScannableEvents(accessToken, ::setEvents)
            }
        }
        RestAPI.accessToken(::setAccessTokenForTicket)
    }

}
