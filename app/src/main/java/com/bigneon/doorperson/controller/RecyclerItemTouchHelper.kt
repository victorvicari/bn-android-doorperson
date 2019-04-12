package com.bigneon.doorperson.controller

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE
import android.support.v7.widget.helper.ItemTouchHelper.RIGHT
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.bigneon.doorperson.R
import com.bigneon.doorperson.adapter.TicketListAdapter
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.viewholder.TicketViewHolder
import kotlinx.android.synthetic.main.content_guest.view.*
import kotlinx.android.synthetic.main.list_item_guest.view.*

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
            ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(viewHolder.itemView.guest_item_foreground)
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
            c, recyclerView, viewHolder?.itemView?.guest_item_foreground, dX, dY,
            actionState, isCurrentlyActive
        )
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder.itemView.guest_item_foreground)
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
            recyclerView.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    if (viewHolder is TicketViewHolder) {
                        if (viewHolder.checkedIn) {
                            swipeBack =
                                event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
                        }
                    }
                    return false
                }
            })
            ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                c, recyclerView, viewHolder.itemView.guest_item_foreground, dX, dY,
                actionState, isCurrentlyActive
            )
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is TicketViewHolder) {
            // TODO - Move this dialog on background item click instead of on swiped!
            if (!viewHolder.checkedIn) {
                // build alert dialog
                val dialogBuilder = AlertDialog.Builder(viewHolder.itemView.context)

                // set message of alert dialog
                dialogBuilder.setMessage("Checked in ${viewHolder.lastNameAndFirstNameTextView?.text.toString()}")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, _ ->
                        run {
                            Log.d(
                                TAG,
                                "ACTION: CHECK-IN: ${viewHolder.lastNameAndFirstNameTextView?.text.toString()}"
                            )
                            dialog.cancel()
                            ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder.itemView)
                            if (ticketList != null && adapter != null) {
                                val pos = viewHolder.getAdapterPosition()
                                val ticket = ticketList!![pos]
                                ticket.status = viewHolder.itemView.context!!.getString(R.string.redeemed).toLowerCase()
                                adapter!!.notifyItemChanged(pos)

                                ticketsDS = TicketsDS()
                                val redeemedTicket = ticketsDS!!.setRedeemTicket(ticket.ticketId!!)
                                if (redeemedTicket != null) {
                                    viewHolder.redeemedStatusTextView?.visibility = View.VISIBLE
                                    viewHolder.purchasedStatusTextView?.visibility = View.GONE
                                    this.parentLayout!!.complete_check_in?.visibility = View.GONE

                                    Snackbar
                                        .make(
                                            this.parentLayout!!,
                                            "Checked in ${redeemedTicket.lastName + ", " + redeemedTicket.firstName}",
                                            Snackbar.LENGTH_LONG
                                        )
                                        .setDuration(5000).show()
                                } else {
                                    Snackbar
                                        .make(
                                            this.parentLayout!!,
                                            "User ticket already redeemed! Redeem key: ${ticket.redeemKey!!}",
                                            Snackbar.LENGTH_LONG
                                        )
                                        .setDuration(5000).show()
                                }
                            }
                        }
                    }
                val alert = dialogBuilder.create()
                alert.setTitle("Alert")
                alert.show()
            }
        }
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }
}