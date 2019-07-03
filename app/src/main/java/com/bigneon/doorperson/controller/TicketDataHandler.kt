package com.bigneon.doorperson.controller

import android.content.Context
import android.util.Log
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.db.ds.SyncDS
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.EventDashboardModel
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.AppUtils
import com.bigneon.doorperson.util.NetworkUtils
import org.jetbrains.anko.doAsync

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 28.06.2019..
 ****************************************************/
class TicketDataHandler {
    companion object {
        private val TAG = TicketDataHandler::class.java.simpleName

        var ticketsDS: TicketsDS = TicketsDS()
        private var context: Context? = null


        fun setContext(con: Context) {
            context = con
        }

        fun storeTickets(eventId: String) {
            fun setAccessTokenForEvent(accessToken: String?) {
                if (accessToken != null) {
                    val ticketsDS = TicketsDS()
                    val syncDS = SyncDS()

                    var page = 0
                    fun loadPageOfTickets() {
                        fun setTickets(tickets: ArrayList<TicketModel>?) {
                            if (!tickets.isNullOrEmpty()) {
                                ticketsDS.createOrUpdateTicketList(tickets)
                            }

                            page++

//                                for (listener in SyncController.refreshTicketListeners)
//                                    listener.refreshTicketList(eventId)

                            // If the loaded ticket list isn't empty, proceed with loading of another page
                            if (!tickets.isNullOrEmpty()) {
                                loadPageOfTickets()
                            } else {
                                syncDS.setLastSyncTime(AppConstants.SyncTableName.TICKETS, eventId, false)
                            }
                        }

                        RestAPI.getTicketsForEvent(
                            accessToken,
                            eventId,
                            syncDS.getLastSyncTime(AppConstants.SyncTableName.TICKETS, eventId, false),
                            AppConstants.SYNC_PAGE_LIMIT,
                            page,
                            ::setTickets
                        )
                    }
                    loadPageOfTickets() // First call of recursive load
                }
            }
            RestAPI.accessToken(::setAccessTokenForEvent)
        }

        fun getRedeemedTicketNumberForEvent(eventId: String): Int {
            when {
                context?.let { NetworkUtils.instance().isNetworkAvailable(it) }!! -> {
                    var eventDashboardModel: EventDashboardModel? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getEventDashboard(accessToken!!, eventId)?.let { eventDashboardModel = it }
                    }.get() // get() is important to wait until doAsync is finished
                    return eventDashboardModel?.ticketsRedeemed ?: 0
                }
                AppUtils.isOfflineModeEnabled -> // Return events from local DB
                    return ticketsDS.getRedeemedTicketNumberForEvent(eventId)
                else -> {
                    Log.e(TAG, "Getting a number of all ticket for event failed")
                }
            }
            return 0
        }

        fun getAllTicketNumberForEvent(eventId: String): Int {
            when {
                context?.let { NetworkUtils.instance().isNetworkAvailable(it) }!! -> {
                    var eventDashboardModel: EventDashboardModel? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getEventDashboard(accessToken!!, eventId)?.let { eventDashboardModel = it }
                    }.get() // get() is important to wait until doAsync is finished
                    return (eventDashboardModel?.soldUnreserved ?: 0) + (eventDashboardModel?.soldHeld ?: 0)
                }
                AppUtils.isOfflineModeEnabled -> // Return events from local DB
                    return ticketsDS.getAllTicketNumberForEvent(eventId)
                else -> {
                    Log.e(TAG, "Getting a number of all ticket for event failed")
                }
            }
            return 0
        }

        fun getCheckedTicketNumberForEvent(eventId: String): Int {
            return ticketsDS.getCheckedTicketNumberForEvent(eventId)
        }

//        fun loadAllTickets(): ArrayList<TicketModel>? {
//            when {
//                context?.let { NetworkUtils.instance().isNetworkAvailable(it) }!! -> {
//                    doAsync {
//                        ticketList.clear()
//                        val accessToken: String? = RestAPI.accessToken()
//                        RestAPI.getScannableTickets(accessToken!!)?.let { ticketList.addAll(it) }
//                    }.get() // get() is important to wait until doAsync is finished
//                    return ticketList
//                }
//                isOfflineModeEnabled -> // Return tickets from local DB
//                    return ticketsDS.getAllTickets()
//                else -> {
//                    Log.e(TAG, "Getting scannable tickets failed")
//                }
//            }
//            return null
//        }
//
//        fun getTicketByID(ticketId: String): TicketModel? {
//            when {
//                context?.let { NetworkUtils.instance().isNetworkAvailable(it) }!! -> {
//                    var ticket: TicketModel? = null
//                    doAsync {
//                        ticketList.clear()
//                        val accessToken: String? = RestAPI.accessToken()
//                        RestAPI.getTicket(accessToken!!, ticketId)?.let { ticket = it }
//                    }.get() // get() is important to wait until doAsync is finished
//                    return ticket
//                }
//                isOfflineModeEnabled -> // Return tickets from local DB
//                    return ticketsDS.getTicket(ticketId)
//                else -> {
//                    Log.e(TAG, "Getting scannable tickets failed")
//                }
//            }
//            return null
//        }
//
//        fun getTicketByPosition(pos: Int): TicketModel? {
//            return ticketList[pos]
//        }
    }
}