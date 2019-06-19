package com.bigneon.doorperson.sync

import android.accounts.Account
import android.content.*
import android.os.Bundle
import android.util.Log
import com.bigneon.doorperson.db.SyncController


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 18.06.2019..
 ****************************************************/
/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */

internal class ConfigurationSyncAdapter  @JvmOverloads constructor(
    context: Context,
    autoInitialize: Boolean,
    /**
     * Using a default argument along with @JvmOverloads
     * generates constructor for both method signatures to maintain compatibility
     * with Android 3.0 and later platform versions
     */
    allowParallelSyncs: Boolean = false,
    /*
     * If your app uses a content resolver, get an instance of it
     * from the incoming Context
     */
    val mContentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {

    /**
     * Perform a sync for this account. SyncAdapter-specific parameters may be specified in extras,
     * which is guaranteed to not be null. Invocations of this method are guaranteed to be
     * serialized.
     *
     * @param account    the account that should be synced
     * @param extras     SyncAdapter-specific parameters
     * @param authority  the authority of this sync request
     * @param provider   a ContentProviderClient that points to the ContentProvider for this
     * authority
     * @param syncResult SyncAdapter-specific parameters
     */
    override fun onPerformSync(
        account: Account, extras: Bundle, authority: String,
        provider: ContentProviderClient, syncResult: SyncResult
    ) {
        Log.i(TAG, "onPerformSync() was called")

        SyncController.synchronizeAllTables(false)
    }

    companion object {

        private val TAG = ConfigurationSyncAdapter::class.java.simpleName
    }
}