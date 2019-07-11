package com.bigneon.doorperson.util

import android.content.Context
import android.support.v7.app.AlertDialog


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 10.07.2019..
 ****************************************************/
abstract class ConnectionDialog {

    fun showDialog(context: Context) {
        // build alert dialog
        // set message of alert dialog
        AlertDialog.Builder(context)
            .setTitle("Error in connection!")
            .setMessage("User ticket is NOT redeemed because offline mode has been disabled and there is no internet connection")
            .setCancelable(false)
            .setPositiveButton("Turn on the offline mode") { _, _ ->
                run {
                    positiveButtonAction(context)
                }
            }
            .setNegativeButton("Turn on the WiFi") { _, _ ->
                run {
                    negativeButtonAction(context)
                }
            }
            .create()
            .show()
    }

    abstract fun positiveButtonAction(context: Context)

    abstract fun negativeButtonAction(context: Context)
}