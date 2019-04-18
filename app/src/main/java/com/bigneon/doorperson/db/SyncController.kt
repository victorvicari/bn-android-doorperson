package com.bigneon.doorperson.db

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.bigneon.doorperson.R
import com.bigneon.doorperson.activity.IEventListRefresher
import com.bigneon.doorperson.activity.ITicketListRefresher
import com.bigneon.doorperson.activity.LoginActivity
import com.bigneon.doorperson.db.SyncController.Companion.isSyncActive
import com.bigneon.doorperson.db.SyncController.Companion.ticketListItemOffset
import com.bigneon.doorperson.db.SyncController.Companion.ticketListItemPosition
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
        var eventListRefresher: IEventListRefresher? = null
        var ticketListRefresher: ITicketListRefresher? = null
        var ticketListItemPosition = -1
        var ticketListItemOffset = 0

        var isNetworkAvailable: Boolean = false
        var isSyncActive: Boolean = false
        var isOfflineModeEnabled: Boolean = true

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun setContext(con: Context) {
            context = con
        }
    }

    fun synchronizeAllTables() {
        SynchronizeAllTablesTask(
            context,
            eventListRefresher,
            ticketListRefresher
        ).execute()
    }
}

class SynchronizeAllTablesTask(
    @SuppressLint("StaticFieldLeak") private val context: Context, var eventListRefresher: IEventListRefresher?,
    var ticketListRefresher: ITicketListRefresher?
) :
    AsyncTask<Unit, Unit, Unit>() {
    private val TAG = SynchronizeAllTablesTask::class.java.simpleName

    private val eventsDS: EventsDS = EventsDS()
    private val ticketsDS: TicketsDS = TicketsDS()

    override fun doInBackground(vararg params: Unit?) {
        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken == null) {
                if (isSyncActive) {
                    context.startActivity(
                        Intent(
                            context,
                            LoginActivity::class.java
                        )
                    )
                }
                isSyncActive = false
            } else {
                isSyncActive = true

                // Upload synchronization
                ticketUploadSynchronization(accessToken)

                // Download synchronization
                eventDownloadSynchronization(accessToken)
                ticketDownloadSynchronization(accessToken)
            }
        }
        RestAPI.accessToken(::setAccessTokenForEvent)
    }

    private fun ticketUploadSynchronization(accessToken: String) {
        val checkedTickets = ticketsDS.getAllCheckedTickets()
        checkedTickets?.forEach { t ->
            RestAPI.redeemTicketForEvent(accessToken, t.eventId!!, t.ticketId!!, t.redeemKey!!, null)
        }
    }

    private fun eventDownloadSynchronization(accessToken: String) {

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

            // Refresh event list
            eventListRefresher?.refreshEventList()
        }
        RestAPI.getScannableEvents(accessToken, ::setEvents)
    }

    private fun ticketDownloadSynchronization(accessToken: String) {
        fun setEvents(events: ArrayList<EventModel>?) {
            // for all events
            events?.forEach { e ->
                fun setTickets(tickets: ArrayList<TicketModel>?) {
                    // for all tickets in the event
                    tickets?.forEach { t ->
                        if (ticketsDS.ticketExists(t.ticketId!!)) {
                            // Prevent download sync if checked ticket wasn't uploaded
                            val ticket = ticketsDS.getTicket(t.ticketId!!)
                            if (ticket?.status == context.getString(R.string.checked) &&
                                t.status == context.getString(R.string.purchased)
                            ) {
                                Log.d(TAG, "Prevented download sync for checked ticket that wasn't uploaded")
                            } else {
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
                            }
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
                ticketListRefresher?.refreshTicketList(e.id!!, ticketListItemPosition, ticketListItemOffset)
            }
        }
        RestAPI.getScannableEvents(accessToken, ::setEvents)
    }
}
