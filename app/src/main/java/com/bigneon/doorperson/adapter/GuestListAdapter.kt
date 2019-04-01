package com.bigneon.doorperson.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bigneon.doorperson.rest.model.GuestModel
import com.bigneon.doorperson.viewholder.GuestViewHolder

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 21.03.2019..
 ****************************************************/
class GuestListAdapter(private val list: ArrayList<GuestModel>) : RecyclerView.Adapter<GuestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return GuestViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val guest: GuestModel = list[position]
        holder.bind(guest)
    }

    override fun getItemCount(): Int = list.size

    fun getItem(position: Int): GuestModel = list[position]
}