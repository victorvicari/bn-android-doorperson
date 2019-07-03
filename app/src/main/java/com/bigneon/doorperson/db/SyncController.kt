package com.bigneon.doorperson.db

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SyncController {
    companion object {
        private val TAG = SyncController::class.java.simpleName

//        @SuppressLint("StaticFieldLeak")
//        private lateinit var context: Context

//        var syncInProgress: Boolean = false
//        var refreshEventListeners: MutableList<RefreshEventListener> = ArrayList()
//        var refreshTicketListeners: MutableList<RefreshTicketListener> = ArrayList()
//        var loadingTicketListeners: MutableList<LoadingTicketListener> = ArrayList()

//        fun setContext(con: Context) {
//            context = con
//        }

//        private fun getContext(): Context {
//            return context
//        }

//        private fun uploadSynchronization(accessToken: String) {
//            val checkedTickets = ticketsDS.getAllCheckedTickets()
//            checkedTickets?.forEach { t ->
//                Log.d(TAG, "Ticket ID: ${t.ticketId} - UPLOADING on server")
//
//                fun redeemTicketResult(isDuplicateTicket: Boolean, redeemedTicket: TicketModel?) {
//                    if (isDuplicateTicket) {
//                        ticketsDS.setDuplicateTicket(t.ticketId!!)
//                        Log.d(TAG, "Ticket ID: ${t.ticketId} - DUPLICATE in local ")
//                    } else {
//                        if (redeemedTicket?.status?.toLowerCase() == getContext().getString(R.string.redeemed).toLowerCase()) {
//                            ticketsDS.updateTicket(redeemedTicket)
//
//                            ticketsDS.setRedeemedTicket(t.ticketId!!)
//                            Log.d(TAG, "Ticket ID: ${t.ticketId!!} - REDEEMED in local ")
//                        }
////                        for (listener in refreshTicketListeners)
////                            listener.refreshTicketList(t.eventId!!)
//                    }
//                }
//
//                RestAPI.redeemTicketForEvent(
//                    accessToken,
//                    t.eventId!!,
//                    t.ticketId!!,
//                    t.firstName!!,
//                    t.lastName!!,
//                    t.redeemKey!!,
//                    ::redeemTicketResult
//                )
//            }
//
//            syncInProgress = false
//        }

//        private fun downloadSynchronization(accessToken: String, fullRefresh: Boolean) {
//            fun setEvents(events: ArrayList<EventModel>?) {
//                if (events != null) {
//                    var j = 0
//                    fun updateTicketsForEvent(event: EventModel) {
//                        val eventForSync = SharedPrefs.getProperty(AppConstants.EVENT_FOR_SYNC + event.id!!)
//                        if (!eventForSync.isNullOrEmpty()) {
//                            var page = 0
//                            fun loadPageOfTickets() {
//                                fun setTickets(tickets: ArrayList<TicketModel>?) {
//                                    if (!tickets.isNullOrEmpty()) {
//                                        ticketsDS.createOrUpdateTicketList(tickets)
//                                    }
//
//                                    page++
//
////                                    for (listener in refreshTicketListeners)
////                                        listener.refreshTicketList(event.id!!)
//
//                                    // If the loaded ticket list isn't empty, proceed with loading of another page
//                                    if (!tickets.isNullOrEmpty())
//                                        loadPageOfTickets()
//                                }
//
//                                val changesSince =
//                                    if (fullRefresh) AppConstants.MIN_TIMESTAMP else SyncDS().getLastSyncTime(
//                                        AppConstants.SyncTableName.TICKETS, event.id!!,
//                                        false
//                                    )
//                                RestAPI.getTicketsForEvent(
//                                    accessToken,
//                                    event.id!!,
//                                    changesSince,
//                                    AppConstants.SYNC_PAGE_LIMIT,
//                                    page,
//                                    ::setTickets
//                                )
//                            }
//                            loadPageOfTickets() // First call of recursive load
//                        }
//
//                        if (++j < events.size) {
//                            updateTicketsForEvent(events[j])
//                        } else {
//                            // Upload synchronization
//                            uploadSynchronization(accessToken)
//                        }
//                    }
//                    updateTicketsForEvent(events[j])
//                } else {
//                    // Upload synchronization
//                    uploadSynchronization(accessToken)
//                }
//            }
//            RestAPI.getScannableEvents(accessToken, ::setEvents)
//        }

//        fun synchronizeAllTables(fullRefresh: Boolean): Boolean {
//            if (!NetworkUtils.instance().isNetworkAvailable(context))
//                return false
//
//            if (!syncInProgress) {
//                Thread(Runnable {
//                    Log.d(TAG, "Synchronization started")
//
//                    fun setAccessTokenForEvent(accessToken: String?) {
//                        if (accessToken != null) {
//                            syncInProgress = true
//
//                            // Download synchronization
//                            downloadSynchronization(accessToken, fullRefresh)
//                        }
//                    }
//
//                    RestAPI.accessToken(::setAccessTokenForEvent)
//                }).start()
//            } else {
//                Log.d(TAG, "Synchronization already in progress - SKIP")
//            }
//
//            return true
//        }



//        fun loadTicketsForEvent(eventId: String) {
//            fun setAccessTokenForEvent(accessToken: String?) {
//                if (accessToken != null) {
//                    val ticketsDS = TicketsDS()
//                    var page = 0
//                    fun loadPageOfTickets() {
//                        fun setTickets(tickets: ArrayList<TicketModel>?) {
//                            if (!tickets.isNullOrEmpty()) {
//                                ticketsDS.createTicketList(tickets)
//                            }
//
//                            page++
//
//                            for (listener in refreshTicketListeners)
//                                listener.refreshTicketList(eventId)
//
//                            // If the loaded ticket list isn't empty, proceed with loading of another page
//                            if (!tickets.isNullOrEmpty())
//                                loadPageOfTickets()
//                            /*else {
//                                for (listener in loadingTicketListeners)
//                                    listener.finish()
//                            }*/
//                        }
//
//                        RestAPI.getTicketsForEvent(
//                            accessToken,
//                            eventId,
//                            AppConstants.MIN_TIMESTAMP,
//                            AppConstants.SYNC_PAGE_LIMIT,
//                            page,
//                            ::setTickets
//                        )
//                    }
//                    loadPageOfTickets() // First call of recursive load
//                }
//            }
//
//            RestAPI.accessToken(::setAccessTokenForEvent)
//        }

//        fun addRefreshEventListener(listener: RefreshEventListener) {
//            refreshEventListeners.add(listener)
//        }
//
//        fun removeRefreshEventListener(listener: RefreshEventListener) {
//            refreshEventListeners.remove(listener)
//        }

//        fun addRefreshTicketListener(listener: RefreshTicketListener) {
//            refreshTicketListeners.add(listener)
//        }
//
//        fun removeRefreshTicketListener(listener: RefreshTicketListener) {
//            refreshTicketListeners.remove(listener)
//        }

//        fun addLoadingTicketListener(listener: LoadingTicketListener) {
//            loadingTicketListeners.add(listener)
//        }
//
//        fun removeLoadingTicketListener(listener: LoadingTicketListener) {
//            loadingTicketListeners.remove(listener)
//        }
    }

//    interface RefreshEventListener {
//        fun refreshEventList()
//    }

//    interface RefreshTicketListener {
//        fun refreshTicketList(eventId: String)
//    }

//    interface LoadingTicketListener {
//        fun finish()
//    }
}