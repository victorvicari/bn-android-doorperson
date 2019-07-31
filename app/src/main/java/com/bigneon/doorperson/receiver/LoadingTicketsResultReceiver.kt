package com.bigneon.doorperson.receiver

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

import android.widget.ProgressBar
import android.widget.TextView
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.service.StoreTicketsService

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 30.07.2019..
 ****************************************************/
class LoadingTicketsResultReceiver(
    handler: Handler,
    private val allTicketNumberForEvent: Int,
    private val loading_progress_bar: ProgressBar,
    private val loading_text: TextView
) :
    ResultReceiver(handler) {

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        when (resultCode) {
            StoreTicketsService.LOADING_IN_PROGRESS -> {
                val page = resultData.getInt("page")
                loading_progress_bar.progress = (page * AppConstants.SYNC_PAGE_LIMIT * 100) / allTicketNumberForEvent
                loading_text.text =
                    "${page * AppConstants.SYNC_PAGE_LIMIT} tickets loaded. (${loading_progress_bar.progress}%)"
            }

            StoreTicketsService.LOADING_COMPLETE -> {
                loading_progress_bar.progress = 100
                loading_text.text = "All $allTicketNumberForEvent has been loaded."
            }
        }
        super.onReceiveResult(resultCode, resultData)
    }
}