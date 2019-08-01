package com.bigneon.doorperson.service

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.db.ds.SyncDS
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.TicketModel



/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 04.07.2019..
 ****************************************************/
class StoreTicketsService : IntentService("StoreTicketsService") {
    companion object {
        const val LOADING_IN_PROGRESS = 1
        const val LOADING_COMPLETE = 2
    }

    override fun onHandleIntent(intent: Intent?) {
        val eventId = intent?.getStringExtra("eventId") ?: return
//        val receiver: ResultReceiver = LoadingTicketsResultReceiver(
//            Handler(), TicketDataHandler.getAllTicketNumberForEvent(eventId) ?: 0
//        )

        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken != null) {
                val ticketsDS = TicketsDS()
                val syncDS = SyncDS()

                fun loadPageOfTickets(page: Int) {
                    fun setTickets(tickets: ArrayList<TicketModel>?) {
                        if (!tickets.isNullOrEmpty()) {
                            ticketsDS.createOrUpdateTicketList(tickets)
                        }

                        val localBroadcastManagerIntent = Intent("loading_tickets_process$eventId")
                        // If the loaded ticket list isn't empty, proceed with loading of another page
                        if (!tickets.isNullOrEmpty()) {
                            loadPageOfTickets(page + 1)
                            val bundle = Bundle()
                            bundle.putInt("page", page + 1)
                            SharedPrefs.setProperty("isLoadingInProgress$eventId", "TRUE")
//                            receiver.send(LOADING_IN_PROGRESS, bundle)

                            localBroadcastManagerIntent.putExtra("page", page + 1)
                            LocalBroadcastManager.getInstance(this).sendBroadcast(localBroadcastManagerIntent)
                        } else {
                            syncDS.setLastSyncTime(AppConstants.SyncTableName.TICKETS, eventId, false)
                            val bundle = Bundle()
                            bundle.putInt("page", page + 1)
                            SharedPrefs.setProperty("isLoadingInProgress$eventId", "FALSE")
//                            receiver.send(LOADING_COMPLETE, bundle)
                            localBroadcastManagerIntent.putExtra("page", 0)
                            LocalBroadcastManager.getInstance(this).sendBroadcast(localBroadcastManagerIntent)
                        }
                    }

                    RestAPI.getTicketsForEvent(
                        accessToken,
                        eventId,

//                        syncDS.getLastSyncTime(AppConstants.SyncTableName.TICKETS, eventId, false),
                        AppConstants.MIN_TIMESTAMP, // TODO - Remove this line and uncomment upper one once notification is implemented

                        AppConstants.SYNC_PAGE_LIMIT,
                        page,
                        ::setTickets
                    )
                }
                loadPageOfTickets(0) // First call of recursive load
            }
        }
        RestAPI.accessToken(::setAccessTokenForEvent)
    }
//
//    inner class LoadingTicketsResultReceiver(
//        handler: Handler,
//        private val allTicketNumberForEvent: Int
//    ) :
//        ResultReceiver(handler) {
//
//        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
//            when (resultCode) {
//                LOADING_IN_PROGRESS -> {
//                    val page = resultData.getInt("page")
////                    loadingProgressBar.progress = (page * AppConstants.SYNC_PAGE_LIMIT * 100) / allTicketNumberForEvent
////                    loadingText.text =
////                        "${page * AppConstants.SYNC_PAGE_LIMIT} tickets loaded. (${loading_progress_bar.progress}%)"
//                }
//
//                LOADING_COMPLETE -> {
////                    loadingProgressBar.progress = 100
////                    loadingText.text = "All $allTicketNumberForEvent has been loaded."
//                }
//            }
//            super.onReceiveResult(resultCode, resultData)
//        }
//    }
}