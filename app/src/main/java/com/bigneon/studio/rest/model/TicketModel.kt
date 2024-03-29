package com.bigneon.studio.rest.model

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 02.04.2019..
 ****************************************************/
class TicketModel {
    @SerializedName("id")
    internal var ticketId: String? = null

    @SerializedName("event_id")
    internal var eventId: String? = null

    @SerializedName("user_id")
    internal var userId: String? = null

    @SerializedName("first_name")
    internal var firstName: String? = null

    @SerializedName("last_name")
    internal var lastName: String? = null

    @SerializedName("email")
    internal var email: String? = null

    @SerializedName("phone")
    internal var phone: String? = null

    @SerializedName("profile_pic_url")
    internal var profilePicURL: String? = null

    @SerializedName("price_in_cents")
    internal var priceInCents: Int? = null

    @SerializedName("ticket_type")
    internal var ticketType: String? = null

    @SerializedName("redeem_key")
    internal var redeemKey: String? = null

    internal var status: String? = null

    @SerializedName("redeemed_by")
    internal var redeemedBy: String? = null

    @SerializedName("redeemed_at")
    internal var redeemedAt: String? = null
}