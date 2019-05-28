package com.bigneon.doorperson.db

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.bigneon.doorperson.R
import com.bigneon.doorperson.activity.IEventListRefresher
import com.bigneon.doorperson.activity.ITicketListRefresher
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.AppConstants.Companion.MIN_TIMESTAMP
import com.bigneon.doorperson.db.SyncController.Companion.eventListRefresher
import com.bigneon.doorperson.db.SyncController.Companion.getContext
import com.bigneon.doorperson.db.SyncController.Companion.syncInProgress
import com.bigneon.doorperson.db.SyncController.Companion.ticketListRefresher
import com.bigneon.doorperson.db.ds.EventsDS
import com.bigneon.doorperson.db.ds.SyncDS
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.EventModel
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.NetworkUtils


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SyncController {
    companion object {
        private val TAG = SyncController::class.java.simpleName
        var eventListRefresher: IEventListRefresher? = null
        var ticketListRefresher: ITicketListRefresher? = null

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

        fun synchronizeAllTables(fullRefresh: Boolean): Boolean {
            if (!NetworkUtils.instance().isNetworkAvailable(context))
                return false

            if (!syncInProgress) {
                Log.d(TAG, "SynchronizeAllTablesTask - STARTED")
                SynchronizeAllTablesTask(fullRefresh).execute()
            } else {
                Log.d(TAG, "Synchronization already in progress - SKIP")
            }

            return true
        }
    }
}

class SynchronizeAllTablesTask(private val fullRefresh: Boolean) :
    AsyncTask<Boolean, Unit, Unit>() {
    override fun doInBackground(vararg params: Boolean?) {
        DownloadSyncTask(
            eventListRefresher,
            ticketListRefresher,
            fullRefresh
        ).execute()
    }
}

class DownloadSyncTask(
    private var eventListRefresher: IEventListRefresher?,
    private var ticketListRefresher: ITicketListRefresher?,
    private var fullRefresh: Boolean
) : AsyncTask<Unit, Unit, Unit>() {
    private val TAG = DownloadSyncTask::class.java.simpleName

    private val eventsDS: EventsDS = EventsDS()
    private val ticketsDS: TicketsDS = TicketsDS()
    private val syncDS: SyncDS = SyncDS()

    override fun onPreExecute() {
        super.onPreExecute()
        syncInProgress = true
        Log.d(TAG, "DownloadSyncTask - STARTED")
    }

    override fun doInBackground(vararg params: Unit?) {
        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken != null) {
                fun setEvents(events: ArrayList<EventModel>?) {
                    events?.forEach { e ->
                        if (eventsDS.eventExists(e.id!!)) {
                            eventsDS.updateEvent(e.id!!, e.name!!, e.promoImageURL!!)
                            Log.d(TAG, "updateEvent - ${e.id}")
                        } else {
                            eventsDS.createEvent(e.id!!, e.name!!, e.promoImageURL!!)
                            Log.d(TAG, "createEvent - ${e.id}")
                        }

                        fun setTickets(tickets: ArrayList<TicketModel>?) {
                            tickets?.forEach { t ->
                                if (ticketsDS.ticketExists(t.ticketId!!)) {
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
                                        t.status?.toUpperCase()!!,
                                        t.redeemedBy,
                                        t.redeemedAt
                                    )
                                    val ticket = ticketsDS.getTicket(t.ticketId!!)
                                    Log.d(
                                        TAG,
                                        "Ticket ID: ${t.ticketId} - Status on server: ${t.status}, status in local ${ticket?.status} "
                                    )

                                    if (fullRefresh) {
                                        if (t.status?.toLowerCase() == SyncController.getContext().getString(R.string.purchased).toLowerCase()) { // Ticket is PURCHASED on server
                                            ticketsDS.setPurchasedTicket(t.ticketId!!)
                                        } else
                                            if (t.status?.toLowerCase() == SyncController.getContext().getString(R.string.redeemed).toLowerCase()) { // Ticket is REDEEMED on server
                                                ticketsDS.setRedeemedTicket(t.ticketId!!)
                                            }
                                    } else {
                                        if (t.status?.toLowerCase() == SyncController.getContext().getString(R.string.purchased).toLowerCase()) { // Ticket is PURCHASED on server
                                            if (ticket?.status?.toLowerCase() == SyncController.getContext().getString(R.string.redeemed).toLowerCase() ||
                                                ticket?.status?.toLowerCase() == SyncController.getContext().getString(R.string.duplicate).toLowerCase()
                                            ) {
                                                Log.e(
                                                    TAG,
                                                    "ERROR: Illegal ticket state! On server: ${t.status}, on device ${ticket.status}"
                                                )
                                            }
                                        } else
                                            if (t.status?.toLowerCase() == SyncController.getContext().getString(R.string.redeemed).toLowerCase()) { // Ticket is REDEEMED on server
                                                if (ticket?.status?.toLowerCase() == SyncController.getContext().getString(
                                                        R.string.purchased
                                                    ).toLowerCase()
                                                ) {
                                                    ticketsDS.setRedeemedTicket(t.ticketId!!)
                                                    Log.d(TAG, "Ticket ID: ${t.ticketId} - REDEEMED in local ")
                                                }
                                            }
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
                                        t.status?.toUpperCase()!!,
                                        t.redeemedBy,
                                        t.redeemedAt
                                    )
                                    Log.d(TAG, "Ticket ID: ${t.ticketId} - CREATED in local ")
                                }
                            }
                            ticketListRefresher?.refreshTicketList(e.id!!)
                        }

                        val changesSince = if (fullRefresh) MIN_TIMESTAMP else syncDS.getLastSyncTime(
                            AppConstants.SyncTableName.TICKETS,
                            false
                        )
                        RestAPI.getTicketsForEvent(accessToken, e.id!!, changesSince, ::setTickets)
                    }
                    // Refresh event list
                    eventListRefresher?.refreshEventList()
                    syncDS.setLastSyncTime(AppConstants.SyncTableName.TICKETS, false)
                }
                RestAPI.getScannableEvents(accessToken, ::setEvents)
            }
        }
        RestAPI.accessToken(::setAccessTokenForEvent)
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        Log.d(TAG, "DownloadSyncTask - FINISHED")
        UploadSyncTask().execute()
    }
}

class UploadSyncTask : AsyncTask<Unit, Unit, Unit>() {
    private val TAG = UploadSyncTask::class.java.simpleName
    private val ticketsDS: TicketsDS = TicketsDS()

    override fun onPreExecute() {
        super.onPreExecute()
        Log.d(TAG, "UploadSyncTask - STARTED")
    }

    override fun doInBackground(vararg params: Unit?) {
        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken != null) {
                val checkedTickets = ticketsDS.getAllCheckedTickets()
                checkedTickets?.forEach { t ->
                    Log.d(TAG, "Ticket ID: ${t.ticketId} - UPLOADING on server")

                    fun redeemTicketResult(isDuplicateTicket: Boolean, redeemedTicket: TicketModel?) {
                        if (isDuplicateTicket) {
                            ticketsDS.setDuplicateTicket(t.ticketId!!)
                            Log.d(TAG, "Ticket ID: ${t.ticketId} - DUPLICATE in local ")
                        } else {
                            if (redeemedTicket?.status?.toLowerCase() == getContext().getString(R.string.redeemed).toLowerCase()) {
                                ticketsDS.updateTicket(redeemedTicket)

                                ticketsDS.setRedeemedTicket(t.ticketId!!)
                                Log.d(TAG, "Ticket ID: ${t.ticketId!!} - REDEEMED in local ")
                            }
                            ticketListRefresher?.refreshTicketList(t.eventId!!)
                        }
                    }

                    RestAPI.redeemTicketForEvent(
                        accessToken,
                        t.eventId!!,
                        t.ticketId!!,
                        t.firstName!!,
                        t.lastName!!,
                        t.redeemKey!!,
                        ::redeemTicketResult
                    )
                }
            }
        }
        RestAPI.accessToken(::setAccessTokenForEvent)
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        syncInProgress = false
        Log.d(TAG, "UploadSyncTask - FINISHED")
    }
}