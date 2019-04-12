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
    private var connected: Boolean = false

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || intent.extras == null)
            return

        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = manager.activeNetworkInfo

        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                connected = true
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                connected = true
            }
        } else {
            // not connected to the internet
            connected = false
        }

        for (listener in this.listeners)
            if (connected)
                listener.networkAvailable()
            else
                listener.networkUnavailable()
    }

    fun addListener(l: NetworkStateReceiverListener) {
        listeners.add(l)
    }

    fun removeListener(l: NetworkStateReceiverListener) {
        listeners.remove(l)
    }

    interface NetworkStateReceiverListener {
        fun networkAvailable()
        fun networkUnavailable()
    }
}