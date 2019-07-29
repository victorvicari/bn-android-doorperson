package com.bigneon.doorperson.service

import android.app.IntentService
import android.content.Intent
import com.bigneon.doorperson.config.AppConstants
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
                        if (!tickets.isNullOrEmpty()) {
                            ticketsDS.createOrUpdateTicketList(tickets)
                        }

                        // If the loaded ticket list isn't empty, proceed with loading of another page
                        if (!tickets.isNullOrEmpty()) {
                            loadPageOfTickets(page + 1)
                        } else {
                            syncDS.setLastSyncTime(AppConstants.SyncTableName.TICKETS, eventId, false)
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