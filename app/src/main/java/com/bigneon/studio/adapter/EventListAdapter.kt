package com.bigneon.studio.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bigneon.studio.rest.model.EventModel
import com.bigneon.studio.viewholder.EventViewHolder

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 21.03.2019..
 ****************************************************/
class EventListAdapter(private val list: ArrayList<EventModel>) : RecyclerView.Adapter<EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EventViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event: EventModel = list[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = list.size
}