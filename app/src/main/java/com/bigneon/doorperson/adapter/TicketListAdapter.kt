package com.bigneon.doorperson.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bigneon.doorperson.R
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.viewholder.TicketBaseViewHolder
import com.bigneon.doorperson.viewholder.TicketFooterViewHolder
import com.bigneon.doorperson.viewholder.TicketViewHolder

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 21.03.2019..
 ****************************************************/
class TicketListAdapter(var list: MutableList<TicketModel>?) : RecyclerView.Adapter<TicketBaseViewHolder>() {

    private var isLoaderVisible = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketBaseViewHolder? {
        val normalLayoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ticket, parent, false)
        val loadingLayoutInflater =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_ticket_loader, parent, false)
        return when (viewType) {
            VIEW_TYPE_NORMAL -> TicketViewHolder(
                normalLayoutInflater, list, parent.context
            )
            VIEW_TYPE_LOADING -> TicketFooterViewHolder(loadingLayoutInflater)
            else -> null
        }
    }

    override fun onBindViewHolder(holder: TicketBaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoaderVisible) {
            if (position == list!!.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    private fun add(ticket: TicketModel) {
        list!!.add(ticket)
    }

    private fun remove(postItems: TicketModel?) {
        val position = list!!.indexOf(postItems)
        if (position > -1) {
            list!!.removeAt(position)
        }
    }

    fun addLoading() {
        isLoaderVisible = true
        add(TicketModel())
    }

    fun removeLoading() {
        isLoaderVisible = false
        val position = list!!.size - 1
        val item = getItem(position)
        if (item != null) {
            list!!.removeAt(position)
        }
    }

    fun clear() {
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    private fun getItem(position: Int): TicketModel? {
        return list!![position]
    }

    companion object {
        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
    }
}





