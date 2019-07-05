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

        var ticketsDS: TicketsDS = TicketsDS()

        fun loadPageOfTickets(eventId: String, page: Int): List<TicketModel>? {
            when {
                isNetworkAvailable() -> {
                    var tickets: ArrayList<TicketModel>? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getTicketsForEvent(accessToken!!,
                            eventId,
                            AppConstants.MIN_TIMESTAMP,
                            AppConstants.PAGE_LIMIT,
                            page)?.let { tickets = it }
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

        fun redeemTicket(eventId: String, ticketId: String, redeemKey: String, firstName: String, lastName: String) {
            fun setAccessToken(accessToken: String?) {
                if (accessToken != null) {
                    fun redeemTicketResult(isDuplicateTicket: Boolean, redeemedTicket: TicketModel?) {

                    }
                    RestAPI.redeemTicketForEvent(
                        accessToken,
                        eventId,
                        ticketId,
                        firstName,
                        lastName,
                        redeemKey,
                        ::redeemTicketResult
                    )
                }
            }
            RestAPI.accessToken(::setAccessToken)
        }
    }
}