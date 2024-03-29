package com.bigneon.studio.util

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.bigneon.studio.receiver.NetworkStateReceiver


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 11.04.2019..
 ****************************************************/
class NetworkUtils {
    companion object {
        private const val CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
        private const val WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED"


        private var networkStateReceiver = NetworkStateReceiver()
        private var registeredListeners: ArrayList<NetworkStateReceiver.NetworkStateReceiverListener> = ArrayList()

        fun addNetworkStateListener(
            context: Context,
            networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener
        ) {
            if (!registeredListeners.contains(networkStateReceiverListener)) {
                networkStateReceiver.addListener(networkStateReceiverListener)
                val intentFilter = IntentFilter()
                intentFilter.addAction(CONNECTIVITY_ACTION)
                intentFilter.addAction(WIFI_STATE_CHANGED)
                context.registerReceiver(networkStateReceiver, intentFilter)
                registeredListeners.add(networkStateReceiverListener)
            }
        }

        fun removeNetworkStateListener(
            context: Context,
            networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener
        ) {
            if (registeredListeners.contains(networkStateReceiverListener)) {
                context.unregisterReceiver(networkStateReceiver)
                networkStateReceiver.removeListener(networkStateReceiverListener)
                registeredListeners.remove(networkStateReceiverListener)
            }
        }

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        @Throws(Exception::class)
        fun setWiFiEnabled(context: Context) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled = true
        }

        @Throws(Exception::class)
        fun setWiFiDisabled(context: Context) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled = false
        }
    }
}