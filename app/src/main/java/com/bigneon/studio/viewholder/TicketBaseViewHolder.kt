package com.bigneon.studio.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 03.07.2019..
 ****************************************************/
abstract class TicketBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var currentPosition: Int = 0

    protected abstract fun bind(position: Int)

    fun onBind(position: Int) {
        currentPosition = position
        bind(position)
    }

}