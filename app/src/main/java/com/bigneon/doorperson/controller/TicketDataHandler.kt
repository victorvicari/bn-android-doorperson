package com.bigneon.doorperson.controller

import android.content.Intent
import android.util.Log
import com.bigneon.doorperson.BigNeonApplication.Companion.context
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.EventDashboardModel
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.service.StoreTicketsService
import com.bigneon.doorperson.util.AppUtils.Companion.isOfflineModeEnabled
import com.bigneon.doorperson.util.NetworkUtils.Companion.isNetworkAvailable
import org.jetbrains.anko.doAsync


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 28.06.2019..
 ****************************************************/
class TicketDataHandler {
    companion object {
        private val TAG = TicketDataHandler::class.java.simpleName

        private var ticketsDS: TicketsDS = TicketsDS()

        fun getTicket(ticketId: String): TicketModel? {
            when {
                isNetworkAvailable() -> {
                    var ticket: TicketModel? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getTicket(accessToken!!, ticketId)?.let { ticket = it }
                    }.get() // get() is important to wait until doAsync is finished
                    return ticket
                }
                isOfflineModeEnabled() -> // Return events from local DB
                    return ticketsDS.getTicket(ticketId)
                else -> {
                    Log.e(TAG, "Getting ticket failed")
                }
            }
            return null
        }

        fun loadPageOfTickets(eventId: String, page: Int): List<TicketModel>? {
            when {
                isNetworkAvailable() -> {
                    var tickets: ArrayList<TicketModel>? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getTicketsForEvent(
                            accessToken!!,
                            eventId,
                            AppConstants.MIN_TIMESTAMP,
                            AppConstants.PAGE_LIMIT,
                            page
                        )?.let { tickets = it }
                    }.get() // get() is important to wait until doAsync is finished
                    return tickets
                }
                isOfflineModeEnabled() -> // Return events from local DB
                    return ticketsDS.getAllTicketsForEvent(eventId) // TODO - implement paging!!!
                else -> {
                    Log.e(TAG, "Getting a number of all ticket for event failed")
                }
            }
            return null
        }

        fun getRedeemedTicketNumberForEvent(eventId: String): Int {
            when {
                isNetworkAvailable() -> {
                    var eventDashboardModel: EventDashboardModel? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getEventDashboard(accessToken!!, eventId)?.let { eventDashboardModel = it }
                    }.get() // get() is important to wait until doAsync is finished
                    return eventDashboardModel?.ticketsRedeemed ?: 0
                }
                isOfflineModeEnabled() -> // Return events from local DB
                    return ticketsDS.getRedeemedTicketNumberForEvent(eventId)
                else -> {
                    Log.e(TAG, "Getting a number of all ticket for event failed")
                }
            }
            return 0
        }

        fun getAllTicketNumberForEvent(eventId: String): Int {
            when {
                isNetworkAvailable() -> {
                    var eventDashboardModel: EventDashboardModel? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getEventDashboard(accessToken!!, eventId)?.let { eventDashboardModel = it }
                    }.get() // get() is important to wait until doAsync is finished
                    return (eventDashboardModel?.soldUnreserved ?: 0) + (eventDashboardModel?.soldHeld ?: 0)
                }
                isOfflineModeEnabled() -> // Return events from local DB
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

        fun storeTickets(eventId: String) {
            val i = Intent(context, StoreTicketsService::class.java)
            i.putExtra("eventId", eventId)
            context?.startService(i)
        }

        fun completeCheckIn(
            eventId: String,
            ticketId: String,
            redeemKey: String,
            firstName: String,
            lastName: String
        ): TicketState? {
            var ticketState: TicketState? = null
            when {
                isNetworkAvailable() -> {
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        val isDuplicateTicket = accessToken?.let {
                            RestAPI.redeemTicketForEvent(
                                it,
                                eventId,
                                ticketId,
                                firstName,
                                lastName,
                                redeemKey
                            )
                        }
                        ticketState = if (isDuplicateTicket == true) {
                            Log.d(
                                TAG,
                                "Warning: DUPLICATE TICKET! - Ticket ID: $ticketId has already been redeemed! "
                            )
                            TicketState.DUPLICATED
                        } else {
                            Log.d(TAG, "Ticket ID: $ticketId has been redeemed! ")
                            TicketState.REDEEMED
                        }
                    }.get() // get() is important to wait until doAsync is finished
                }
                isOfflineModeEnabled() -> {
                    ticketsDS.setCheckedTicket(ticketId)
                    ticketState = TicketState.CHECKED
                }
                else -> {
                    ticketState = TicketState.ERROR
                    Log.e(TAG, "Error in connection! Redeem ticket failed")
                }
            }
            return ticketState
        }
    }

    enum class TicketState {
        REDEEMED, CHECKED, DUPLICATED, ERROR
    }
}