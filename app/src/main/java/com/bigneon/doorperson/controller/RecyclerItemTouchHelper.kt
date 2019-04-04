package com.bigneon.doorperson.controller

import android.annotation.SuppressLint
import android.graphics.Canvas
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
import com.bigneon.doorperson.adapter.GuestListAdapter
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.GuestModel
import com.bigneon.doorperson.viewholder.GuestViewHolder
import kotlinx.android.synthetic.main.list_item_guest.view.*


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 29.03.2019..
 ****************************************************/
class RecyclerItemTouchHelper :
    ItemTouchHelper.SimpleCallback(0, RIGHT) {
    private val TAG = RecyclerItemTouchHelper::class.java.simpleName
    var guestList: ArrayList<GuestModel>? = null
    var adapter: GuestListAdapter? = null
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
                    if (viewHolder is GuestViewHolder && viewHolder.checkedIn) {
                        swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
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
        if (viewHolder is GuestViewHolder) {
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
                            if (guestList != null && adapter != null) {
                                val pos = viewHolder.getAdapterPosition()
                                val guest = guestList!!.get(pos)
                                guest.status = viewHolder.itemView.context!!.getString(R.string.redeemed).toLowerCase()

                                RestAPI.redeemTicketForEvent(
                                    viewHolder.itemView.context, this.parentLayout!!,
                                    guest.eventId!!, guest.id!!, guest.redeemKey!!
                                )
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