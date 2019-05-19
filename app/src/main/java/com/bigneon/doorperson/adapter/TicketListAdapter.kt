package com.bigneon.doorperson.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.viewholder.TicketViewHolder

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 21.03.2019..
 ****************************************************/
class TicketListAdapter(
    private val list: ArrayList<TicketModel>
) : RecyclerView.Adapter<TicketViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TicketViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket: TicketModel = list[position]
        holder.bind(ticket)
    }

    override fun getItemCount(): Int = list.size
}