package com.bigneon.doorperson.viewholder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bigneon.doorperson.R
import com.bigneon.doorperson.rest.model.TicketModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 22.03.2019..
 ****************************************************/
class TicketViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_ticket, parent, false)) {
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
    private var ticketTypeName: String? = null


    init {
        context = parent.context
        lastNameAndFirstNameTextView = itemView.findViewById(R.id.last_name_and_first_name)
        priceAndTicketTypeTextView = itemView.findViewById(R.id.price_and_ticket_type)
        ticketIdTextView = itemView.findViewById(R.id.ticket_id)
        redeemedStatusTextView = itemView.findViewById(R.id.redeemed_status_item)
        checkedStatusTextView = itemView.findViewById(R.id.checked_status_item)
        checkedNoInternetImageView = itemView.findViewById(R.id.checked_no_internet_item)
        duplicateStatusTextView = itemView.findViewById(R.id.duplicate_status_item)
        purchasedStatusTextView = itemView.findViewById(R.id.purchased_status_item)
        ticketItemBackgroundRedeemedOrChecked = itemView.findViewById(R.id.ticket_item_background_redeemed_or_checked)
        ticketItemBackgroundPurchased = itemView.findViewById(R.id.ticket_item_background_purchased)
    }

    fun bind(ticket: TicketModel) {
        ticketId = ticket.ticketId
        priceInCents = ticket.priceInCents
        ticketTypeName = ticket.ticketType

        lastNameAndFirstNameTextView?.text =
            context!!.getString(R.string.last_name_first_name, ticket.lastName, ticket.firstName)

        priceAndTicketTypeTextView?.text = context!!.getString(R.string.price_ticket_type, priceInCents?.div(100), ticketTypeName)

        ticketIdTextView?.text = "#" + ticket.ticketId?.takeLast(8)

        val ticketStatus = ticket.status?.toLowerCase()
        val statusRedeemed = context!!.getString(R.string.redeemed).toLowerCase()
        val statusChecked = context!!.getString(R.string.checked).toLowerCase()
        val statusDuplicate = context!!.getString(R.string.duplicate).toLowerCase()
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
