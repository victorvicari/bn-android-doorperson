package com.bigneon.studio.viewholder

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bigneon.studio.rest.model.TicketModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 22.03.2019..
 ****************************************************/
class TicketViewHolder internal constructor(itemView: View, list: MutableList<TicketModel>?, context: Context) :
    TicketBaseViewHolder(itemView) {
    var lastNameAndFirstNameTextView: TextView? = null
    private var priceAndTicketTypeTextView: TextView? = null
    private var ticketIdTextView: TextView? = null
    private var redeemedStatusTextView: TextView? = null
    private var checkedStatusTextView: TextView? = null
    private var checkedNoInternetImageView: ImageView? = null
    private var duplicateStatusTextView: TextView? = null
    private var purchasedStatusTextView: TextView? = null
    private var ticketItemBackgroundRedeemedOrChecked: TextView? = null
    private var ticketItemBackgroundPurchased: TextView? = null

    private var list: MutableList<TicketModel>? = null

    var checkedIn: Boolean = false
    private var priceInCents: Int? = null
    private var ticketType: String? = null

    private var context: Context? = null

    init {
        lastNameAndFirstNameTextView = itemView.findViewById(com.bigneon.studio.R.id.last_name_and_first_name)
        priceAndTicketTypeTextView = itemView.findViewById(com.bigneon.studio.R.id.price_and_ticket_type)
        ticketIdTextView = itemView.findViewById(com.bigneon.studio.R.id.ticket_id)
        redeemedStatusTextView = itemView.findViewById(com.bigneon.studio.R.id.redeemed_status_item)
        checkedStatusTextView = itemView.findViewById(com.bigneon.studio.R.id.checked_status_item)
        checkedNoInternetImageView = itemView.findViewById(com.bigneon.studio.R.id.checked_no_internet_item)
        duplicateStatusTextView = itemView.findViewById(com.bigneon.studio.R.id.duplicate_status_item)
        purchasedStatusTextView = itemView.findViewById(com.bigneon.studio.R.id.purchased_status_item)
        ticketItemBackgroundRedeemedOrChecked =
            itemView.findViewById(com.bigneon.studio.R.id.ticket_item_background_redeemed_or_checked)
        ticketItemBackgroundPurchased =
            itemView.findViewById(com.bigneon.studio.R.id.ticket_item_background_purchased)

        this.list = list
        this.context = context
    }

    override fun bind(position: Int) {
        val ticket = list?.get(position)

        priceInCents = ticket?.priceInCents
        ticketType = ticket?.ticketType

        lastNameAndFirstNameTextView?.text =
            context!!.getString(
                com.bigneon.studio.R.string.last_name_first_name,
                ticket?.lastName,
                ticket?.firstName
            )

        priceAndTicketTypeTextView?.text =
            context!!.getString(com.bigneon.studio.R.string.price_ticket_type, priceInCents?.div(100), ticketType)

        ticketIdTextView?.text = "#" + ticket?.ticketId?.takeLast(8)

        val ticketStatus = ticket?.status?.toLowerCase()
        val statusRedeemed = context!!.getString(com.bigneon.studio.R.string.redeemed).toLowerCase()
        val statusChecked = context!!.getString(com.bigneon.studio.R.string.checked).toLowerCase()
        val statusDuplicate = context!!.getString(com.bigneon.studio.R.string.duplicate).toLowerCase()
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