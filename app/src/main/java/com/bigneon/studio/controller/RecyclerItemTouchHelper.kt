package com.bigneon.studio.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE
import android.support.v7.widget.helper.ItemTouchHelper.RIGHT
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import com.bigneon.studio.BigNeonApplication.Companion.context
import com.bigneon.studio.R
import com.bigneon.studio.activity.DuplicateTicketCheckinActivity
import com.bigneon.studio.adapter.TicketListAdapter
import com.bigneon.studio.config.AppConstants
import com.bigneon.studio.config.SharedPrefs
import com.bigneon.studio.rest.model.TicketModel
import com.bigneon.studio.util.AppUtils
import com.bigneon.studio.util.AppUtils.Companion.enableOfflineMode
import com.bigneon.studio.util.ConnectionDialog
import com.bigneon.studio.util.NetworkUtils
import com.bigneon.studio.util.NetworkUtils.Companion.setWiFiEnabled
import com.bigneon.studio.viewholder.TicketViewHolder
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
                            when (TicketDataHandler.completeCheckIn(viewHolder.itemView.context, ticketModel)) {
                                TicketDataHandler.TicketState.REDEEMED -> {
                                    Snackbar
                                        .make(
                                            parentLayout!!,
                                            "Redeemed ${"${ticketModel.lastName!!}, ${ticketModel.firstName!!}"}",
                                            Snackbar.LENGTH_LONG
                                        )
                                        .setDuration(5000).show()
                                    ticketModel.status =
                                        viewHolder.itemView.context!!.getString(R.string.redeemed).toLowerCase()
                                }
                                TicketDataHandler.TicketState.CHECKED -> {
                                    Snackbar
                                        .make(
                                            parentLayout!!,
                                            "Checked in ${"${ticketModel.lastName!!}, ${ticketModel.firstName!!}"}",
                                            Snackbar.LENGTH_LONG
                                        )
                                        .setDuration(5000).show()
                                    ticketModel.status =
                                        viewHolder.itemView.context!!.getString(R.string.checked).toLowerCase()
                                }
                                TicketDataHandler.TicketState.DUPLICATED -> {
                                    val intent = Intent(context, DuplicateTicketCheckinActivity::class.java)
                                    intent.putExtra("ticketId", ticketModel.ticketId)
                                    intent.putExtra(
                                        "lastAndFirstName",
                                        "${ticketModel.lastName!!}, ${ticketModel.firstName!!}"
                                    )
                                    intent.putExtra("redeemedBy", ticketModel.redeemedBy)
                                    intent.putExtra("redeemedAt", ticketModel.redeemedAt)
                                    context?.startActivity(intent)

                                    Snackbar
                                        .make(
                                            parentLayout!!,
                                            "Warning: Ticket redeemed by ${ticketModel.redeemedBy} ${AppUtils.getTimeAgo(
                                                ticketModel.redeemedAt!!
                                            )}",
                                            Snackbar.LENGTH_LONG
                                        )
                                        .setDuration(5000).show()
                                    ticketModel.status =
                                        viewHolder.itemView.context!!.getString(R.string.duplicate).toLowerCase()
                                }
                                TicketDataHandler.TicketState.ERROR -> {
                                    object : ConnectionDialog() {
                                        override fun positiveButtonAction(context: Context) {
                                            enableOfflineMode()
                                            completeCheckIn()
                                        }

                                        override fun negativeButtonAction(context: Context) {
                                            setWiFiEnabled(context)
                                            while (!NetworkUtils.isNetworkAvailable(context)) Thread.sleep(1000)
                                            completeCheckIn()
                                        }
                                    }.showDialog(viewHolder.itemView.context)
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