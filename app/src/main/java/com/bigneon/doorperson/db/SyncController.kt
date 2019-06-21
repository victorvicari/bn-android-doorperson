package com.bigneon.doorperson.db

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.bigneon.doorperson.R
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs
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

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        var isOfflineModeEnabled: Boolean = true
        var syncInProgress: Boolean = false
        var refreshEventListeners: MutableList<RefreshEventListener> = ArrayList()
        var refreshTicketListeners: MutableList<RefreshTicketListener> = ArrayList()

        fun setContext(con: Context) {
            context = con
        }

        private fun getContext(): Context {
            return context
        }

        fun synchronizeAllTables(fullRefresh: Boolean): Boolean {
            if (!NetworkUtils.instance().isNetworkAvailable(context))
                return false

            if (!syncInProgress) {
                Log.d(TAG, "Synchronization started")

                val eventsDS = EventsDS()
                val ticketsDS = TicketsDS()
                val syncDS = SyncDS()

                fun uploadSynchronization(accessToken: String) {
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
                                for (listener in refreshTicketListeners)
                                    listener.refreshTicketList(t.eventId!!, 0)
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

                    syncInProgress = false
                }

                fun downloadSynchronization(accessToken: String) {
                    fun setEvents(events: ArrayList<EventModel>?) {
                        if (events != null) {
                            fun updateTicketsForEvents() {
                                var j = 0
                                fun updateTicketsForEvent(e: EventModel) {
                                    val eventForSync = SharedPrefs.getProperty(AppConstants.EVENT_FOR_SYNC + e.id!!)
                                    if (!eventForSync.isNullOrEmpty()) {
                                        val initialLoading = ticketsDS.getAllTicketNumberForEvent(e.id!!) == 0

                                        var page = 0
                                        fun loadPageOfTickets() {
                                            fun setTickets(tickets: ArrayList<TicketModel>?) {
                                                if (!tickets.isNullOrEmpty()) {
                                                    if (initialLoading)
                                                        ticketsDS.createTicketList(tickets)
                                                    else
                                                        ticketsDS.createOrUpdateTicketList(tickets)
                                                }

                                                page++

                                                for (listener in refreshTicketListeners)
                                                    listener.refreshTicketList(e.id!!, page)

                                                // If the loaded ticket list isn't empty, proceed with loading of another page
                                                if (!tickets.isNullOrEmpty())
                                                    loadPageOfTickets()
                                            }

                                            val changesSince =
                                                if (fullRefresh) AppConstants.MIN_TIMESTAMP else syncDS.getLastSyncTime(
                                                    AppConstants.SyncTableName.TICKETS,
                                                    false
                                                )
                                            RestAPI.getTicketsForEvent(
                                                accessToken,
                                                e.id!!,
                                                changesSince,
                                                AppConstants.SYNC_PAGE_LIMIT,
                                                page,
                                                ::setTickets
                                            )
                                        }
                                        loadPageOfTickets() // First call of recursive load
                                    }

                                    if (++j < events.size) {
                                        updateTicketsForEvent(events[j])
                                    } else {
                                        // Upload synchronization
                                        uploadSynchronization(accessToken)
                                    }
                                }
                                updateTicketsForEvent(events[j])
                            }

                            var i = 0
                            fun updateEvent(e: EventModel) {
                                fun setTotalNumberOfTickets(total: Int?) {
                                    if (eventsDS.eventExists(e.id!!)) {
                                        eventsDS.updateEvent(e.id!!, e.name!!, e.promoImageURL!!, total ?: 0)
                                        Log.d(TAG, "updateEvent - ${e.id}")
                                    } else {
                                        eventsDS.createEvent(e.id!!, e.name!!, e.promoImageURL!!, total ?: 0)
                                        Log.d(TAG, "createEvent - ${e.id}")
                                    }

                                    if (++i < events.size) {
                                        updateEvent(events[i])
                                    } else {
                                        // Refresh event list
                                        for (listener in refreshEventListeners)
                                            listener.refreshEventList()

                                        updateTicketsForEvents()
                                    }
                                }
                                RestAPI.getTotalNumberOfTicketsForEvent(accessToken, e.id!!, ::setTotalNumberOfTickets)
                            }

                            updateEvent(events[i])
                        } else {
                            // Upload synchronization
                            uploadSynchronization(accessToken)
                        }
                    }
                    RestAPI.getScannableEvents(accessToken, ::setEvents)
                }

                fun setAccessTokenForEvent(accessToken: String?) {
                    if (accessToken != null) {
                        syncInProgress = true

                        // Download synchronization
                        downloadSynchronization(accessToken)
                    }
                }

                RestAPI.accessToken(::setAccessTokenForEvent)
            } else {
                Log.d(TAG, "Synchronization already in progress - SKIP")
            }

            return true
        }

        fun addRefreshEventListener(listener: RefreshEventListener) {
            refreshEventListeners.add(listener)
        }

        fun removeRefreshEventListener(listener: RefreshEventListener) {
            refreshEventListeners.remove(listener)
        }

        fun addRefreshTicketListener(listener: RefreshTicketListener) {
            refreshTicketListeners.add(listener)
        }

        fun removeRefreshTicketListener(listener: RefreshTicketListener) {
            refreshTicketListeners.remove(listener)
        }
    }

    interface RefreshEventListener {
        fun refreshEventList()
    }

    interface RefreshTicketListener {
        fun refreshTicketList(eventId: String, page: Int)
    }
}