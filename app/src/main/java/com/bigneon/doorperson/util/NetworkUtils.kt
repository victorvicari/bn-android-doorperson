package com.bigneon.doorperson.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.bigneon.doorperson.receiver.NetworkStateReceiver


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 11.04.2019..
 ****************************************************/
class NetworkUtils {
    private object Loader {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        internal var INSTANCE = NetworkUtils()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun setContext(con: Context) {
            context = con
        }

        fun instance(): NetworkUtils {
            return Loader.INSTANCE
        }

        private const val CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
        private const val WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED"
    }

    private var networkStateReceiver: NetworkStateReceiver = NetworkStateReceiver()
    private var registeredListenersMap: ArrayList<NetworkStateReceiver.NetworkStateReceiverListener> = ArrayList()

    fun addNetworkStateListener(
        networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener
    ) {
        if (!registeredListenersMap.contains(networkStateReceiverListener)) {
            networkStateReceiver.addListener(networkStateReceiverListener)
            val intentFilter = IntentFilter()
            intentFilter.addAction(CONNECTIVITY_ACTION)
            intentFilter.addAction(WIFI_STATE_CHANGED)
            context.registerReceiver(networkStateReceiver, intentFilter)
            registeredListenersMap.add(networkStateReceiverListener)
        }
    }

    fun removeNetworkStateListener(
        networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener
    ) {
        if (registeredListenersMap.contains(networkStateReceiverListener)) {
            networkStateReceiver.removeListener(networkStateReceiverListener)
            context.unregisterReceiver(networkStateReceiver)
            registeredListenersMap.remove(networkStateReceiverListener)
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    @Throws(Exception::class)
    fun setWiFiEnabled(enabled: Boolean) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = enabled
    }
}