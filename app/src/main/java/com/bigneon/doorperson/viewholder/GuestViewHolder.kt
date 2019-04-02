package com.bigneon.doorperson.viewholder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bigneon.doorperson.R
import com.bigneon.doorperson.rest.model.GuestModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 22.03.2019..
 ****************************************************/
class GuestViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_guest, parent, false)) {
    var lastNameAndFirstNameTextView: TextView? = null
    private var priceAndTicketTypeTextView: TextView? = null
    private var redeemedStatusTextView: TextView? = null
    private var purchasedStatusTextView: TextView? = null
    private var context: Context? = null
    var checkedIn : Boolean = false

    init {
        context = parent.context
        lastNameAndFirstNameTextView = itemView.findViewById(R.id.last_name_and_first_name)
        priceAndTicketTypeTextView = itemView.findViewById(R.id.price_and_ticket_type)
        redeemedStatusTextView = itemView.findViewById(R.id.redeemed_status)
        purchasedStatusTextView = itemView.findViewById(R.id.purchased_status)
    }

    fun bind(guest: GuestModel) {
        lastNameAndFirstNameTextView?.text =
            context!!.getString(R.string.last_name_first_name, guest.lastName, guest.firstName)
        priceAndTicketTypeTextView?.text =
            context!!.getString(R.string.price_ticket_type, guest.priceInCents?.div(100), guest.ticketType)
        if (guest.status?.toLowerCase() == "redeemed") {
            redeemedStatusTextView?.visibility = View.VISIBLE
            purchasedStatusTextView?.visibility = View.GONE
            checkedIn = true
        } else {
            redeemedStatusTextView?.visibility = View.GONE
            purchasedStatusTextView?.visibility = View.VISIBLE
            checkedIn = false
        }
    }
}