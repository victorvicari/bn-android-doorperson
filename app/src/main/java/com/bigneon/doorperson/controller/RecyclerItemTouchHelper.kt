package com.bigneon.doorperson.controller

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Canvas
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE
import android.support.v7.widget.helper.ItemTouchHelper.RIGHT
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import com.bigneon.doorperson.R
import com.bigneon.doorperson.adapter.TicketListAdapter
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.controller.TicketDataHandler.Companion.completeCheckIn
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.AppUtils.Companion.enableOfflineMode
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

                        fun completeCheckIn() {
                            when (completeCheckIn(
                                ticketModel.eventId!!,
                                ticketModel.ticketId!!,
                                ticketModel.redeemKey!!,
                                ticketModel.firstName!!,
                                ticketModel.lastName!!
                            )) {
                                TicketDataHandler.TicketState.REDEEMED -> {
                                    ticketModel.status =
                                        viewHolder.itemView.context!!.getString(R.string.redeemed).toLowerCase()
                                }
                                TicketDataHandler.TicketState.CHECKED -> {
                                    ticketModel.status =
                                        viewHolder.itemView.context!!.getString(R.string.checked).toLowerCase()
                                }
                                TicketDataHandler.TicketState.DUPLICATED -> {
                                    ticketModel.status =
                                        viewHolder.itemView.context!!.getString(R.string.duplicate).toLowerCase()
                                }
                                TicketDataHandler.TicketState.ERROR -> {
                                    // build alert dialog
                                    val dialogBuilder = AlertDialog.Builder(viewHolder.itemView.context)

                                    // set message of alert dialog
                                    dialogBuilder.setMessage("User ticket is NOT redeemed because offline mode has been disabled and there is no internet connection")
                                        .setCancelable(false)
                                        .setPositiveButton("Turn on the offline mode") { _, _ ->
                                            run {
                                                enableOfflineMode()
                                                completeCheckIn()
                                            }
                                        }
                                        .setNegativeButton("Turn on the WiFi") { _, _ ->
                                            run {
                                                setWiFiEnabled(true)
                                                completeCheckIn()
                                            }
                                        }
                                    val alert = dialogBuilder.create()
                                    alert.setTitle("Error in connection!")
                                    alert.show()
                                }
                            }
                            SharedPrefs.setProperty(
                                AppConstants.LAST_CHECKED_TICKET_ID + ticketModel.eventId!!,
                                ticketModel.ticketId!!
                            )
                        }
                        completeCheckIn()
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