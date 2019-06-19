package com.bigneon.doorperson.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import android.content.Context.ACCOUNT_SERVICE
import android.os.Bundle
import android.util.Log
import com.bigneon.doorperson.R


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 18.06.2019..
 ****************************************************/
internal class SyncAdapterManager(private val context: Context) {
    private val authority: String = context.getString(R.string.authority)
    private val type: String = context.getString(R.string.account_type)

    private var account: Account? = null

    init {
        account = Account(context.getString(R.string.app_name), type)
    }

    fun beginPeriodicSync(updateConfigInterval: Long) {
        Log.d(
            TAG, "beginPeriodicSync() called with: updateConfigInterval = [" +
                    updateConfigInterval + "]"
        )

        val accountManager = context
            .getSystemService(ACCOUNT_SERVICE) as AccountManager

        if (!accountManager.addAccountExplicitly(account, null, null)) {
            account = accountManager.getAccountsByType(type)[0]
        }

        setAccountSyncable()

        ContentResolver.addPeriodicSync(
            account, context.getString(R.string.authority),
            Bundle.EMPTY, updateConfigInterval
        )

        ContentResolver.setSyncAutomatically(account, authority, true)
    }

    fun syncImmediately() {
        val settingsBundle = Bundle()
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)

        ContentResolver.requestSync(account, authority, settingsBundle)
    }

    private fun setAccountSyncable() {
        if (ContentResolver.getIsSyncable(account, authority) == 0) {
            ContentResolver.setIsSyncable(account, authority, 1)
        }
    }

    companion object {
        private val TAG = SyncAdapterManager::class.java.simpleName
    }

}