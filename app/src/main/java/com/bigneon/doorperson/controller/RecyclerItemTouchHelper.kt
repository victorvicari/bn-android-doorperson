package com.bigneon.doorperson.controller

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE
import android.support.v7.widget.helper.ItemTouchHelper.RIGHT
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.bigneon.doorperson.R
import com.bigneon.doorperson.activity.LoginActivity
import com.bigneon.doorperson.adapter.TicketListAdapter
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.AppUtils.Companion.enableOfflineMode
import com.bigneon.doorperson.util.AppUtils.Companion.isOfflineModeEnabled
import com.bigneon.doorperson.util.NetworkUtils
import com.bigneon.doorperson.util.NetworkUtils.Companion.isNetworkAvailable
import com.bigneon.doorperson.util.NetworkUtils.Companion.setWiFiEnabled
import com.bigneon.doorperson.viewholder.TicketViewHolder
import kotlinx.android.synthetic.main.list_item_ticket.view.*
import java.util.*

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 29.03.2019..
 ****************************************************/
class RecyclerItemTouchHelper :
    ItemTouchHelper.SimpleCallback(0, RIGHT) {
    private val TAG = RecyclerItemTouchHelper::class.java.simpleName
    var ticketList: ArrayList<TicketModel>? = null
    var adapter: TicketListAdapter? = null
    var parentLayout: LinearLayout? = null
    private var swipeBack = false
    private var ticketsDS: TicketsDS? = null

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        p2: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(viewHolder.itemView.ticket_item_foreground)
        }
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        ItemTouchHelper.Callback.getDefaultUIUtil().onDrawOver(
            c, recyclerView, viewHolder?.itemView?.ticket_item_foreground, dX, dY,
            actionState, isCurrentlyActive
        )
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder.itemView.ticket_item_foreground)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ACTION_STATE_SWIPE) {
            recyclerView.setOnTouchListener { _, event ->
                if (viewHolder is TicketViewHolder) {
                    if (viewHolder.checkedIn) {
                        swipeBack =
                            event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
                    }
                }
                false
            }
            ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                c, recyclerView, viewHolder.itemView.ticket_item_foreground, dX, dY,
                actionState, isCurrentlyActive
            )
        }
    }

    private fun checkInTicket(ticketModel: TicketModel, viewHolder: TicketViewHolder) {
        val ticket = ticketsDS!!.setCheckedTicket(ticketModel.ticketId!!)
        if (ticket != null) {
            viewHolder.checkedStatusTextView?.visibility = View.VISIBLE
            viewHolder.purchasedStatusTextView?.visibility = View.GONE
            Log.d(TAG, "Ticket ID: ${ticket.ticketId} - CHECKED in local ")
        }
    }

    private fun redeemTicket(ticketModel: TicketModel, viewHolder: TicketViewHolder) {
        fun setAccessToken(accessToken: String?) {
            if (accessToken == null) {
                viewHolder.itemView.context.startActivity(
                    Intent(
                        viewHolder.itemView.context,
                        LoginActivity::class.java
                    )
                )
            } else {
                fun redeemTicketResult(isDuplicateTicket: Boolean, redeemedTicket: TicketModel?) {
                    if (isDuplicateTicket) {
                        ticketsDS!!.setDuplicateTicket(ticketModel.ticketId!!)
                        Log.d(TAG, "Ticket ID: ${ticketModel.ticketId!!} - DUPLICATE in local ")
                    } else {
                        if (redeemedTicket?.status?.toLowerCase() == viewHolder.itemView.context.getString(R.string.redeemed).toLowerCase()) {
                            ticketsDS!!.updateTicket(redeemedTicket)

                            ticketsDS!!.setRedeemedTicket(ticketModel.ticketId!!)
                            Log.d(TAG, "Ticket ID: ${ticketModel.ticketId!!} - REDEEMED in local ")

                            viewHolder.redeemedStatusTextView?.visibility = View.VISIBLE
                            viewHolder.purchasedStatusTextView?.visibility = View.GONE
                        } else {
                            if (!isOfflineModeEnabled() && !isNetworkAvailable()
                            ) {
                                // build alert dialog
                                val dialogBuilder = AlertDialog.Builder(viewHolder.itemView.context)

                                // set message of alert dialog
                                dialogBuilder.setMessage("User ticket is NOT redeemed because offline mode has been disabled and there is no internet connection")
                                    .setCancelable(false)
                                    .setPositiveButton("Turn on the offline mode") { _, _ ->
                                        run {
                                            enableOfflineMode()
                                            checkInTicket(ticketModel, viewHolder)
                                        }
                                    }
                                    .setNegativeButton("Turn on the WiFi") { _, _ ->
                                        run {
                                            setWiFiEnabled(true)
                                            redeemTicket(ticketModel, viewHolder)
                                        }
                                    }
                                val alert = dialogBuilder.create()
                                alert.setTitle("Error in connection!")
                                alert.show()
                            } else {
                                Log.e(TAG, "ERROR: redeemTicketForEvent")
                            }
                        }
                    }
                }

                RestAPI.redeemTicketForEvent(
                    accessToken,
                    ticketModel.eventId!!,
                    ticketModel.ticketId!!,
                    ticketModel.firstName!!,
                    ticketModel.lastName!!,
                    ticketModel.redeemKey!!,
                    ::redeemTicketResult
                )
            }
        }
        RestAPI.accessToken(::setAccessToken)
    }

    private fun showDialog(ticketModel: TicketModel, viewHolder: TicketViewHolder) {
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(viewHolder.itemView.context)

        // set message of alert dialog
        dialogBuilder.setMessage("User ticket is NOT redeemed because offline mode has been disabled and there is no internet connection")
            .setCancelable(false)
            .setPositiveButton("Turn on the offline mode") { _, _ ->
                run {
                    enableOfflineMode()
                    checkInTicket(ticketModel, viewHolder)
                }
            }
            .setNegativeButton("Turn on the WiFi") { _, _ ->
                run {
                    setWiFiEnabled(true)
                    redeemTicket(ticketModel, viewHolder)
                }
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Error in connection!")
        alert.show()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is TicketViewHolder) {
            // TODO - Move this dialog on background item click instead of on swiped!
            if (!viewHolder.checkedIn) {
                Snackbar
                    .make(
                        viewHolder.itemView,
                        "Checked in ${viewHolder.lastNameAndFirstNameTextView?.text.toString()}",
                        Snackbar.LENGTH_LONG
                    )
                    .setDuration(5000).show()

                run {
                    Log.d(
                        TAG,
                        "ACTION: CHECK-IN: ${viewHolder.lastNameAndFirstNameTextView?.text.toString()}"
                    )
                    ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder.itemView)
                    if (ticketList != null && adapter != null) {
                        val pos = viewHolder.getAdapterPosition()
                        val ticketModel = ticketList!![pos]

                        adapter!!.notifyItemChanged(pos)

                        ticketsDS = TicketsDS()

                        when {
                            isNetworkAvailable() -> {
                                ticketModel.status =
                                    viewHolder.itemView.context!!.getString(R.string.redeemed).toLowerCase()
                                redeemTicket(ticketModel, viewHolder)
                            }
                            isOfflineModeEnabled() -> {
                                ticketModel.status =
                                    viewHolder.itemView.context!!.getString(R.string.checked).toLowerCase()
                                checkInTicket(ticketModel, viewHolder)
                            }
                            else -> {
                                showDialog(ticketModel, viewHolder)
                                Log.e(TAG, "ERROR: Internet is not available and offline mode is disabled!")
                            }
                        }
                        SharedPrefs.setProperty(
                            AppConstants.LAST_CHECKED_TICKET_ID + ticketModel.eventId,
                            ticketModel.ticketId
                        )
                    }
                }
            }
        }
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }
}