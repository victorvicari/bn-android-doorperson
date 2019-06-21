package com.bigneon.doorperson.service

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.bigneon.doorperson.db.SyncController

class SyncService : IntentService("SyncService") {
    private val syncAllTablesReceiver = object : BroadcastReceiver() {
        @Synchronized
        override fun onReceive(context: Context, intent: Intent) {
            SyncController.synchronizeAllTables(false)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If we need synchronization every minute
        val filter = IntentFilter()
        // Run every 1 minute!
        filter.addAction("android.intent.action.TIME_TICK")
        registerReceiver(syncAllTablesReceiver, filter)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        //Initial sync
        SyncController.synchronizeAllTables(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(syncAllTablesReceiver)
    }
}
