package com.bigneon.doorperson.viewholder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    var redeemedStatusTextView: TextView? = null
    var purchasedStatusTextView: TextView? = null
    private var ticketItemBackgroundRedeemed: TextView? = null
    private var ticketItemBackgroundPurchased: TextView? = null
    private var context: Context? = null
    var checkedIn: Boolean = false

    init {
        context = parent.context
        lastNameAndFirstNameTextView = itemView.findViewById(R.id.last_name_and_first_name)
        priceAndTicketTypeTextView = itemView.findViewById(R.id.price_and_ticket_type)
        redeemedStatusTextView = itemView.findViewById(R.id.redeemed_status)
        purchasedStatusTextView = itemView.findViewById(R.id.purchased_status)
        ticketItemBackgroundRedeemed = itemView.findViewById(R.id.ticket_item_background_redeemed)
        ticketItemBackgroundPurchased = itemView.findViewById(R.id.ticket_item_background_purchased)
    }

    fun bind(ticket: TicketModel) {
        lastNameAndFirstNameTextView?.text =
            context!!.getString(R.string.last_name_first_name, ticket.lastName, ticket.firstName)
        // TODO - Uncomment after test!
//        priceAndTicketTypeTextView?.text =
//            context!!.getString(R.string.price_ticket_type, ticket.priceInCents?.div(100), ticket.ticketType)
        priceAndTicketTypeTextView?.text = ticket.redeemKey
        if (ticket.status?.toLowerCase() == context!!.getString(R.string.redeemed).toLowerCase()) {
            redeemedStatusTextView?.visibility = View.VISIBLE
            purchasedStatusTextView?.visibility = View.GONE
            ticketItemBackgroundRedeemed?.visibility = View.VISIBLE
            ticketItemBackgroundPurchased?.visibility = View.GONE
            checkedIn = true
        } else {
            redeemedStatusTextView?.visibility = View.GONE
            purchasedStatusTextView?.visibility = View.VISIBLE
            ticketItemBackgroundRedeemed?.visibility = View.GONE
            ticketItemBackgroundPurchased?.visibility = View.VISIBLE
            checkedIn = false
        }
    }
}