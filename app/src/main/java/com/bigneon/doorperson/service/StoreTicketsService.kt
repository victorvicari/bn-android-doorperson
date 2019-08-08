package com.bigneon.doorperson.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.db.ds.SyncDS
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.NetworkUtils.Companion.isNetworkAvailable
import org.jetbrains.anko.doAsync

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 04.07.2019..
 ****************************************************/
class StoreTicketsService : IntentService("StoreTicketsService") {
    private fun getContext(): Context {
        return this
    }

    override fun onHandleIntent(intent: Intent?) {
        val eventId = intent?.getStringExtra("eventId") ?: return
        doAsync {
            val accessToken: String? = RestAPI.accessToken()
            val ticketsDS = TicketsDS()
            val syncDS = SyncDS()

            fun loadPageOfTickets(page: Int) {
                var tickets: ArrayList<TicketModel>? = null

                if (!isNetworkAvailable(getContext())) {
                    Log.d(Companion.TAG, "Network is not available!")
//                    SharedPrefs.setProperty("loadingStatus$eventId", LoadingStatus.STOPPED.name)
                    Thread.sleep(3000)
                    loadPageOfTickets(page)
                }

                RestAPI.getTicketsForEvent(
                    accessToken!!,
                    eventId,
                    //syncDS.getLastSyncTime(AppConstants.SyncTableName.TICKETS, eventId, false),
                    AppConstants.MIN_TIMESTAMP,
                    AppConstants.SYNC_PAGE_LIMIT,
                    "",
                    page
                )?.let { tickets = it }

                val localBroadcastManagerIntent = Intent("loading_tickets_process")
                // If the loaded ticket list isn't empty, proceed with loading of another page
                if (tickets!!.isNotEmpty()) {
                    ticketsDS.createOrUpdateTicketList(tickets!!)

                    val bundle = Bundle()
                    bundle.putInt("page", page + 1)
//                        SharedPrefs.setProperty("loadingStatus$eventId", LoadingStatus.LOADING.name)

                    // Sending broadcast to update loader
                    localBroadcastManagerIntent.putExtra("eventId", eventId)
                    localBroadcastManagerIntent.putExtra("page", page + 1)
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(localBroadcastManagerIntent)

                    // Load next page
                    loadPageOfTickets(page + 1)
                } else {
                    syncDS.setLastSyncTime(AppConstants.SyncTableName.TICKETS, eventId, false)

                    val bundle = Bundle()
                    bundle.putInt("page", page + 1)
//                        SharedPrefs.setProperty("loadingStatus$eventId", LoadingStatus.FINISHED.name)

                    // Sending broadcast to finish loader
                    localBroadcastManagerIntent.putExtra("eventId", eventId)
                    localBroadcastManagerIntent.putExtra("page", 0)
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(localBroadcastManagerIntent)
                }

            }
            loadPageOfTickets(0) // First call of recursive load
        }.get() // get() is important to wait until doAsync is finished
    }

    companion object {
        private const val TAG = "StoreTicketsService"
    }
}

//enum class LoadingStatus {
//    LOADING, STOPPED, FINISHED
//}