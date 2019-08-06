package com.bigneon.doorperson.service

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
    override fun onHandleIntent(intent: Intent?) {
        val eventId = intent?.getStringExtra("eventId") ?: return

        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken != null) {
                val ticketsDS = TicketsDS()
                val syncDS = SyncDS()

                fun loadPageOfTickets(page: Int) {
                    fun setTickets(tickets: ArrayList<TicketModel>?) {
                        // If loading failed, try again after 3s
                        if (tickets == null) {
                            SharedPrefs.setProperty("loadingStatus$eventId", LoadingStatus.STOPPED.name)
                            Handler().postDelayed(
                                {
                                    loadPageOfTickets(page)
                                }, 3000
                            )
                        } else {
                            val localBroadcastManagerIntent = Intent("loading_tickets_process")
                            // If the loaded ticket list isn't empty, proceed with loading of another page
                            if (tickets.isNotEmpty()) {
                                ticketsDS.createOrUpdateTicketList(tickets)

                                loadPageOfTickets(page + 1)
                                val bundle = Bundle()
                                bundle.putInt("page", page + 1)
                                SharedPrefs.setProperty("loadingStatus$eventId", LoadingStatus.LOADING.name)

                                // Sending broadcast to update loader
                                localBroadcastManagerIntent.putExtra("eventId", eventId)
                                localBroadcastManagerIntent.putExtra("page", page + 1)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(localBroadcastManagerIntent)
                            } else {
                                syncDS.setLastSyncTime(AppConstants.SyncTableName.TICKETS, eventId, false)
                                val bundle = Bundle()
                                bundle.putInt("page", page + 1)
                                SharedPrefs.setProperty("loadingStatus$eventId", LoadingStatus.FINISHED.name)

                                // Sending broadcast to finish loader
                                localBroadcastManagerIntent.putExtra("eventId", eventId)
                                localBroadcastManagerIntent.putExtra("page", 0)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(localBroadcastManagerIntent)
                            }
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
}

enum class LoadingStatus {
    LOADING, STOPPED, FINISHED
}