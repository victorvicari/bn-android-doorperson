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
        fun instance(): NetworkUtils {
            return NetworkUtils.Loader.INSTANCE
        }
    }

    var networkStateReceiver: NetworkStateReceiver = NetworkStateReceiver()

    fun addNetworkStateListener(
        context: Context,
        networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener
    ) {
        networkStateReceiver.addListener(networkStateReceiverListener)
        context.registerReceiver(networkStateReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    fun removeNetworkStateListener(
        context: Context,
        networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener
    ) {
        networkStateReceiver.removeListener(networkStateReceiverListener)
        context.unregisterReceiver(networkStateReceiver)
    }


    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    @Throws(Exception::class)
    fun setWiFiEnabled(context: Context, enabled: Boolean) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = enabled;
    }
}