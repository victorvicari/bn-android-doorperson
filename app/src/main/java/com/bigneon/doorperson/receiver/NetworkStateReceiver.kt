package com.bigneon.doorperson.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 08.04.2019..
 ****************************************************/
class NetworkStateReceiver : BroadcastReceiver() {
    private var listeners: MutableList<NetworkStateReceiverListener> = ArrayList()

    companion object {
        private var connected: Boolean = false
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || intent.extras == null)
            return

        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connected = if (manager.activeNetworkInfo != null)
            manager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI ||
                    manager.activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
        else
            false

        for (listener in this.listeners)
            if (connected)
                listener.networkAvailable()
            else
                listener.networkUnavailable()
    }

    fun addListener(listener: NetworkStateReceiverListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: NetworkStateReceiverListener) {
        listeners.remove(listener)
    }

    interface NetworkStateReceiverListener {
        fun networkAvailable()
        fun networkUnavailable()
    }
}