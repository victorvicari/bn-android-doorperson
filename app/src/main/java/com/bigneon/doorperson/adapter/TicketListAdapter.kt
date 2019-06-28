package com.bigneon.doorperson.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bigneon.doorperson.rest.model.TicketModel


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 21.03.2019..
 ****************************************************/
class TicketListAdapter : BaseAdapter<TicketModel>() {
    private var footerViewHolder: FooterViewHolder? = null

    override fun createHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder? {
        return null
    }

    override fun createItemViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(com.bigneon.doorperson.R.layout.list_item_ticket, parent, false)
        val holder = TicketViewHolder(view, parent)

        holder.itemView.setOnClickListener {
            val adapterPos = holder.adapterPosition
            if (adapterPos != RecyclerView.NO_POSITION) {
                if (onItemClickListener2 != null) {
                    onItemClickListener2!!.onItemClick(adapterPos, holder.itemView)
                }
            }
        }

        return holder
    }

    override fun createFooterViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(com.bigneon.doorperson.R.layout.adapter_footer, parent, false)

        val holder = FooterViewHolder(view)
        holder.reloadButton?.setOnClickListener {
            if (onReloadClickListener2 != null) {
                onReloadClickListener2!!.onReloadClick()
            }
        }

        return holder
    }

    override fun bindHeaderViewHolder(viewHolder: RecyclerView.ViewHolder) {

    }

    override fun bindItemViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as TicketViewHolder

        val ticket = getItem(position)
        if (ticket != null) {
            holder.bind(ticket)
        }
    }

    override fun bindFooterViewHolder(viewHolder: RecyclerView.ViewHolder) {
        val holder = viewHolder as FooterViewHolder
        footerViewHolder = holder

//        holder.loadingImageView!!.setMaskOrientation(LoadingImageView.MaskOrientation.LeftToRight)
    }

    override fun displayLoadMoreFooter() {
        if (footerViewHolder != null) {
            footerViewHolder!!.errorRelativeLayout?.visibility = View.GONE
            footerViewHolder!!.loadingFrameLayout?.visibility = View.VISIBLE
        }
    }

    override fun displayErrorFooter() {
        if (footerViewHolder != null) {
            footerViewHolder!!.loadingFrameLayout?.visibility = View.GONE
            footerViewHolder!!.errorRelativeLayout?.visibility = View.VISIBLE
        }
    }

    override fun addFooter() {
        isFooterAdded = true
        add(TicketModel())
    }
}

class TicketViewHolder(view: View, parent: ViewGroup) : RecyclerView.ViewHolder(view) {

    var lastNameAndFirstNameTextView: TextView? = null
    private var priceAndTicketTypeTextView: TextView? = null
    private var ticketIdTextView: TextView? = null
    var redeemedStatusTextView: TextView? = null
    var checkedStatusTextView: TextView? = null
    private var checkedNoInternetImageView: ImageView? = null
    private var duplicateStatusTextView: TextView? = null
    var purchasedStatusTextView: TextView? = null
    private var ticketItemBackgroundRedeemedOrChecked: TextView? = null
    private var ticketItemBackgroundPurchased: TextView? = null
    private var context: Context? = null
    var checkedIn: Boolean = false
    var ticketId: String? = null
    private var priceInCents: Int? = null
    private var ticketType: String? = null

    init {
        context = parent.context
        lastNameAndFirstNameTextView = itemView.findViewById(com.bigneon.doorperson.R.id.last_name_and_first_name)
        priceAndTicketTypeTextView = itemView.findViewById(com.bigneon.doorperson.R.id.price_and_ticket_type)
        ticketIdTextView = itemView.findViewById(com.bigneon.doorperson.R.id.ticket_id)
        redeemedStatusTextView = itemView.findViewById(com.bigneon.doorperson.R.id.redeemed_status_item)
        checkedStatusTextView = itemView.findViewById(com.bigneon.doorperson.R.id.checked_status_item)
        checkedNoInternetImageView = itemView.findViewById(com.bigneon.doorperson.R.id.checked_no_internet_item)
        duplicateStatusTextView = itemView.findViewById(com.bigneon.doorperson.R.id.duplicate_status_item)
        purchasedStatusTextView = itemView.findViewById(com.bigneon.doorperson.R.id.purchased_status_item)
        ticketItemBackgroundRedeemedOrChecked =
            itemView.findViewById(com.bigneon.doorperson.R.id.ticket_item_background_redeemed_or_checked)
        ticketItemBackgroundPurchased =
            itemView.findViewById(com.bigneon.doorperson.R.id.ticket_item_background_purchased)
    }

    @SuppressLint("SetTextI18n")
    fun bind(ticket: TicketModel) {
        ticketId = ticket.ticketId
        priceInCents = ticket.priceInCents
        ticketType = ticket.ticketType

        lastNameAndFirstNameTextView?.text =
            context!!.getString(com.bigneon.doorperson.R.string.last_name_first_name, ticket.lastName, ticket.firstName)

        priceAndTicketTypeTextView?.text =
            context!!.getString(com.bigneon.doorperson.R.string.price_ticket_type, priceInCents?.div(100), ticketType)

        ticketIdTextView?.text = "#" + ticket.ticketId?.takeLast(8)

        val ticketStatus = ticket.status?.toLowerCase()
        val statusRedeemed = context!!.getString(com.bigneon.doorperson.R.string.redeemed).toLowerCase()
        val statusChecked = context!!.getString(com.bigneon.doorperson.R.string.checked).toLowerCase()
        val statusDuplicate = context!!.getString(com.bigneon.doorperson.R.string.duplicate).toLowerCase()
        when (ticketStatus) {
            statusRedeemed -> {
                redeemedStatusTextView?.visibility = View.VISIBLE
                checkedStatusTextView?.visibility = View.GONE
                checkedNoInternetImageView?.visibility = View.GONE
                duplicateStatusTextView?.visibility = View.GONE
                purchasedStatusTextView?.visibility = View.GONE
                ticketItemBackgroundRedeemedOrChecked?.visibility = View.VISIBLE
                ticketItemBackgroundPurchased?.visibility = View.GONE
                checkedIn = true
            }
            statusChecked -> {
                redeemedStatusTextView?.visibility = View.GONE
                checkedStatusTextView?.visibility = View.VISIBLE
                checkedNoInternetImageView?.visibility = View.VISIBLE
                duplicateStatusTextView?.visibility = View.GONE
                purchasedStatusTextView?.visibility = View.GONE
                ticketItemBackgroundRedeemedOrChecked?.visibility = View.VISIBLE
                ticketItemBackgroundPurchased?.visibility = View.GONE
                checkedIn = true
            }
            statusDuplicate -> {
                redeemedStatusTextView?.visibility = View.GONE
                checkedStatusTextView?.visibility = View.GONE
                checkedNoInternetImageView?.visibility = View.GONE
                duplicateStatusTextView?.visibility = View.VISIBLE
                purchasedStatusTextView?.visibility = View.GONE
                ticketItemBackgroundRedeemedOrChecked?.visibility = View.VISIBLE
                ticketItemBackgroundPurchased?.visibility = View.GONE
                checkedIn = true
            }
            else -> {
                redeemedStatusTextView?.visibility = View.GONE
                checkedStatusTextView?.visibility = View.GONE
                checkedNoInternetImageView?.visibility = View.GONE
                duplicateStatusTextView?.visibility = View.GONE
                purchasedStatusTextView?.visibility = View.VISIBLE
                ticketItemBackgroundRedeemedOrChecked?.visibility = View.GONE
                ticketItemBackgroundPurchased?.visibility = View.VISIBLE
                checkedIn = false
            }
        }
    }
}

class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var loadingFrameLayout: FrameLayout? = null
    var errorRelativeLayout: RelativeLayout? = null
    var loadingImageView: ProgressBar? = null
    var reloadButton: TextView? = null

    init {
        loadingFrameLayout = itemView.findViewById(com.bigneon.doorperson.R.id.loadingFrameLayout)
        errorRelativeLayout = itemView.findViewById(com.bigneon.doorperson.R.id.errorRelativeLayout)
        loadingImageView = itemView.findViewById(com.bigneon.doorperson.R.id.loadingImageView)
        reloadButton = itemView.findViewById(com.bigneon.doorperson.R.id.reloadButton)
    }
}