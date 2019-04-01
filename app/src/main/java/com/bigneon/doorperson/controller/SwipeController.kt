package com.bigneon.doorperson.controller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.RIGHT
import android.util.Log
import com.bigneon.doorperson.R
import com.bigneon.doorperson.viewholder.GuestViewHolder


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 29.03.2019..
 ****************************************************/
class SwipeController : ItemTouchHelper.SimpleCallback(0, RIGHT) {
    private val TAG = SwipeController::class.java.simpleName
    var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
    var context: Context? = null
    private var swipeBack = false

    override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
        adapter = p0.adapter
        return false
    }

    private var swipedViewHolder: GuestViewHolder? = null

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipedViewHolder = viewHolder as GuestViewHolder
        adapter?.notifyDataSetChanged()

        if (!swipedViewHolder!!.checkedIn) {
            // build alert dialog
            val dialogBuilder = AlertDialog.Builder(this.context!!)

            // set message of alert dialog
            dialogBuilder.setMessage("Checked in ${swipedViewHolder?.lastNameAndFirstNameTextView?.text.toString()}")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    run {
                        Log.d(
                            TAG,
                            "ACTION: CHECK-IN: ${swipedViewHolder?.lastNameAndFirstNameTextView?.text.toString()}"
                        )
                        dialog.cancel()
                        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(swipedViewHolder!!.itemView)
                        adapter?.notifyDataSetChanged()
                    }
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Alert")
            alert.show()
        } else {
            ItemTouchHelper.Callback.getDefaultUIUtil().clearView(swipedViewHolder!!.itemView)
            adapter?.notifyDataSetChanged()
            swipeBack = true
        }
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        context = viewHolder.itemView.context
        swipedViewHolder = viewHolder as GuestViewHolder

        // Get RecyclerView item from the ViewHolder
        val itemView = viewHolder.itemView

        val p = Paint()
        if (dX > 0) {

            if (!swipedViewHolder!!.checkedIn) {
                p.color = viewHolder.itemView.context?.getColor(R.color.colorAccent)!!
            } else {
                p.color = viewHolder.itemView.context?.getColor(R.color.colorGray)!!
            }

            // Draw Rect with varying right side, equal to displacement dX
            c.drawRect(
                itemView.left.toFloat(), itemView.top.toFloat(), dX,
                itemView.bottom.toFloat(), p
            )

            val myViewHolder = viewHolder as GuestViewHolder
            ItemTouchHelper.Callback.getDefaultUIUtil()
                .onDraw(c, recyclerView, myViewHolder.itemView, dX, dY, actionState, isCurrentlyActive)
        }
    }
}