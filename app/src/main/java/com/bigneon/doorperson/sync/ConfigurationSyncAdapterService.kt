package com.bigneon.doorperson.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 18.06.2019..
 ****************************************************/
class ConfigurationSyncAdapterService : Service() {

    override fun onCreate() {
        super.onCreate()

        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized(syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = ConfigurationSyncAdapter(applicationContext, true)
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    override fun onBind(intent: Intent): IBinder {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return syncAdapter!!.syncAdapterBinder
    }

    companion object {

        private var syncAdapter: ConfigurationSyncAdapter? = null
        private val syncAdapterLock = Any()
    }
}