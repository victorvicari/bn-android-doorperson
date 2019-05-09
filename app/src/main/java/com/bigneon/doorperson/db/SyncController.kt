package com.bigneon.doorperson.db

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import com.bigneon.doorperson.R
import com.bigneon.doorperson.activity.IEventListRefresher
import com.bigneon.doorperson.activity.ITicketListRefresher
import com.bigneon.doorperson.activity.LoginActivity
import com.bigneon.doorperson.db.SyncController.Companion.eventListRefresher
import com.bigneon.doorperson.db.SyncController.Companion.isSyncActive
import com.bigneon.doorperson.db.SyncController.Companion.syncInProgress
import com.bigneon.doorperson.db.SyncController.Companion.ticketListRefresher
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

        var eventListItemPosition = -1
        var eventListItemOffset = 0
        var ticketListItemPosition = -1
        var ticketListItemOffset = 0

        var isSyncActive: Boolean = false
        var isOfflineModeEnabled: Boolean = true
        var syncInProgress: Boolean = false

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun setContext(con: Context) {
            context = con
        }

        fun getContext(): Context {
            return context
        }

        fun synchronizeAllTables() {
            SynchronizeAllTablesTask().execute()
        }
    }
}

class SynchronizeAllTablesTask :
    AsyncTask<Unit, Unit, Unit>() {
    override fun doInBackground(vararg params: Unit?) {
        UploadSyncTask().execute()
    }
}

class UploadSyncTask : AsyncTask<Unit, Unit, Unit>() {
    private val ticketsDS: TicketsDS = TicketsDS()

    override fun onPreExecute() {
        super.onPreExecute()
        syncInProgress = true
    }

    override fun doInBackground(vararg params: Unit?) {
        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken == null) {
                if (isSyncActive) {
                    val intent = Intent(SyncController.getContext(), LoginActivity::class.java)
                    SyncController.getContext().startActivity(intent)
                }
                isSyncActive = false
            } else {
                val checkedTickets = ticketsDS.getAllCheckedTickets()
                checkedTickets?.forEach { t ->
                    RestAPI.redeemTicketForEvent(accessToken, t.eventId!!, t.ticketId!!, t.redeemKey!!, null)
                }
            }
        }
        RestAPI.accessToken(::setAccessTokenForEvent)
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        DownloadSyncTask(
            eventListRefresher,
            ticketListRefresher
        ).execute()
    }
}

class DownloadSyncTask(
    private var eventListRefresher: IEventListRefresher?,
    private var ticketListRefresher: ITicketListRefresher?
) : AsyncTask<Unit, Unit, Unit>() {
    private val TAG = DownloadSyncTask::class.java.simpleName
    private val eventsDS: EventsDS = EventsDS()
    private val ticketsDS: TicketsDS = TicketsDS()

    override fun doInBackground(vararg params: Unit?) {
        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken == null) {
                if (isSyncActive) {
                    val intent = Intent(SyncController.getContext(), LoginActivity::class.java)
                    SyncController.getContext().startActivity(intent)
                }
                isSyncActive = false
            } else {
                fun setEvents(events: ArrayList<EventModel>?) {
                    events?.forEach { e ->
                        if (eventsDS.eventExists(e.id!!)) {
                            eventsDS.updateEvent(e.id!!, e.name!!, e.promoImageURL!!)
                        } else {
                            eventsDS.createEvent(e.id!!, e.name!!, e.promoImageURL!!)
                        }

                        fun setTickets(tickets: ArrayList<TicketModel>?) {
                            tickets?.forEach { t ->
                                if (ticketsDS.ticketExists(t.ticketId!!)) {
                                    // Prevent download sync if checked ticket wasn't uploaded
                                    val ticket = ticketsDS.getTicket(t.ticketId!!)
                                    if (ticket?.status?.toLowerCase() == SyncController.getContext().getString(R.string.checked).toLowerCase() &&
                                        t.status?.toLowerCase() == SyncController.getContext().getString(R.string.purchased).toLowerCase()
                                    ) {
                                        Log.d(
                                            TAG,
                                            "Prevented download sync for checked ticket that wasn't uploaded"
                                        )
                                    } else if (ticket?.status?.toLowerCase() == SyncController.getContext().getString(R.string.checked).toLowerCase() &&
                                        t.status?.toLowerCase() == SyncController.getContext().getString(R.string.redeemed).toLowerCase()
                                    ) {
                                        ticketsDS.setDuplicateTicket(t.ticketId!!)
                                    } else {
                                        if (ticket?.status?.toLowerCase() != SyncController.getContext().getString(R.string.duplicate).toLowerCase())
                                            ticketsDS.updateTicket(
                                                t.ticketId!!,
                                                t.eventId!!,
                                                t.userId!!,
                                                t.firstName,
                                                t.lastName,
                                                t.email,
                                                t.phone,
                                                t.profilePicURL,
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
                                        t.userId!!,
                                        t.firstName,
                                        t.lastName,
                                        t.email,
                                        t.phone,
                                        t.profilePicURL,
                                        t.priceInCents!!,
                                        t.ticketType!!,
                                        t.redeemKey!!,
                                        t.status?.toUpperCase()!!
                                    )
                                }
                            }
                            // Refresh ticket list list
                            ticketListRefresher?.refreshTicketList(e.id!!)
                        }
                        RestAPI.getTicketsForEvent(accessToken, e.id!!, ::setTickets)
                        ticketListRefresher?.refreshTicketList(e.id!!)
                    }
                    // Refresh event list
                    eventListRefresher?.refreshEventList()
                }
                RestAPI.getScannableEvents(accessToken, ::setEvents)
            }
        }
        RestAPI.accessToken(::setAccessTokenForEvent)
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        syncInProgress = false
    }
}
