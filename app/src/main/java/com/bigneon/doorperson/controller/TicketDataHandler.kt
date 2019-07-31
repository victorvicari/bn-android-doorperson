package com.bigneon.doorperson.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import com.bigneon.doorperson.BigNeonApplication.Companion.context
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.receiver.LoadingTicketsResultReceiver
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.EventDashboardModel
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.service.StoreTicketsService
import com.bigneon.doorperson.util.AppUtils
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

        private var listeners: MutableList<RefreshTickets> = ArrayList()

        private var ticketsDS: TicketsDS = TicketsDS()

        fun getTicket(context: Context, ticketId: String): TicketModel? {
            when {
                isNetworkAvailable(context) -> {
                    var ticket: TicketModel? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getTicket(accessToken!!, ticketId)?.let { ticket = it }
                    }.get() // get() is important to wait until doAsync is finished
                    return ticket ?: TicketModel()
                }
                isOfflineModeEnabled() -> // Return events from local DB
                    return ticketsDS.getTicket(ticketId) ?: TicketModel()
                else -> {
                    Log.e(TAG, "Getting ticket failed")
                    return null
                }
            }
        }

        fun loadPageOfTickets(context: Context, eventId: String, filter: String, page: Int): List<TicketModel>? {
            when {
                isNetworkAvailable(context) -> {
                    var tickets: ArrayList<TicketModel>? = null
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        RestAPI.getTicketsForEvent(
                            accessToken!!,
                            eventId,
                            AppConstants.MIN_TIMESTAMP,
                            AppConstants.PAGE_LIMIT,
                            filter,
                            page
                        )?.let { tickets = it }
                    }.get() // get() is important to wait until doAsync is finished
                    return tickets ?: ArrayList()
                }
                isOfflineModeEnabled() -> // Return events from local DB
                    return ticketsDS.getTicketsForEvent(eventId, filter, page) ?: ArrayList()
                else -> {
                    Log.e(TAG, "Getting a number of all ticket for event failed")
                    return null
                }
            }
        }

        fun getRedeemedTicketNumberForEvent(context: Context, eventId: String): Int? {
            when {
                isNetworkAvailable(context) -> {
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
                    return null
                }
            }
        }

        fun getAllTicketNumberForEvent(context: Context, eventId: String): Int? {
            when {
                isNetworkAvailable(context) -> {
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
                    return null
                }
            }
        }

        fun getCheckedTicketNumberForEvent(eventId: String): Int {
            return ticketsDS.getCheckedTicketNumberForEvent(eventId)
        }

        fun storeTickets(eventId: String, loadingTicketsResultReceiver: LoadingTicketsResultReceiver) {
            val intent = Intent(context, StoreTicketsService::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("receiver", loadingTicketsResultReceiver)
            context?.startService(intent)
        }

        fun completeCheckIn(context: Context, ticketModel: TicketModel): TicketState? {
            var ticketState: TicketState? = null
            when {
                isNetworkAvailable(context) -> {
                    doAsync {
                        val accessToken: String? = RestAPI.accessToken()
                        val localTicketModel = accessToken?.let {
                            RestAPI.redeemTicketForEvent(
                                it,
                                ticketModel.eventId!!,
                                ticketModel.ticketId!!,
                                ticketModel.redeemKey!!
                            )
                        }
                        ticketState = if (localTicketModel == null) {
                            var tickets: ArrayList<TicketModel>? = null
                            doAsync {
                                RestAPI.getTicketsForEvent(
                                    accessToken!!,
                                    ticketModel.eventId!!,
                                    AppConstants.MIN_TIMESTAMP,
                                    AppConstants.PAGE_LIMIT,
                                    ticketModel.ticketId!!,
                                    null
                                )?.let { tickets = it }
                            }.get() // get() is important to wait until doAsync is finished
                            val ticket = tickets?.get(0)
                            Log.d(
                                TAG,
                                "Warning: Ticket redeemed by ${ticket?.redeemedBy} ${AppUtils.getTimeAgo(
                                    ticket?.redeemedAt!!
                                )}"
                            )
                            ticketsDS.setDuplicateTicket(ticketModel.ticketId!!)

                            ticketModel.redeemedBy = ticket.redeemedBy
                            ticketModel.redeemedAt = ticket.redeemedAt

                            TicketState.DUPLICATED
                        } else {
                            Log.d(TAG, "Ticket ID: ${ticketModel.ticketId} has been redeemed! ")
                            TicketState.REDEEMED
                        }
                    }.get() // get() is important to wait until doAsync is finished
                }
                isOfflineModeEnabled() -> {
                    ticketsDS.setCheckedTicket(ticketModel.ticketId!!)

                    ticketState = TicketState.CHECKED
                }
                else -> {
                    ticketState = TicketState.ERROR
                    Log.e(TAG, "Error in connection! Redeem ticket failed")
                }
            }
            return ticketState
        }

        fun updateTicket(ticketId: String, status: String) {
            for (listener in listeners) {
                listener.updateTicket(ticketId, status)
            }
        }

        fun refreshTicketList() {
            for (listener in listeners) {
                listener.refreshTicketList()
            }
        }

        fun addRefreshTicketsListener(listener: RefreshTickets) {
            listeners.add(listener)
        }

        fun removeRefreshTicketsListener(listener: RefreshTickets) {
            listeners.remove(listener)
        }
    }

    enum class TicketState {
        REDEEMED, CHECKED, DUPLICATED, ERROR
    }

    interface RefreshTickets {
        fun updateTicket(ticketId: String, status: String)

        fun refreshTicketList()
    }
}