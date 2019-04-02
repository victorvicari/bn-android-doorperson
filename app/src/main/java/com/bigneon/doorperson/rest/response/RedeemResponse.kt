package com.bigneon.doorperson.rest.response

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 01.04.2019..
 ****************************************************/
class RedeemResponse {
    internal var id: String? = null

    @SerializedName("ticket_type")
    internal var ticketType: String? = null

    @SerializedName("userId")
    internal var user_id: String? = null

    @SerializedName("orderId")
    internal var order_id: String? = null

    @SerializedName("orderItemId")
    internal var order_item_id: String? = null

    @SerializedName("priceInCents")
    internal var price_in_cents: Int? = null

    @SerializedName("firstName")
    internal var first_name: String? = null

    @SerializedName("lastName")
    internal var last_name: String? = null

    internal var email: String? = null

    internal var phone: String? = null

    @SerializedName("redeemKey")
    internal var redeem_key: String? = null

    @SerializedName("redeemDate")
    internal var redeem_date: String? = null

    internal var status: String? = null

    @SerializedName("eventId")
    internal var event_id: String? = null

    @SerializedName("eventName")
    internal var event_name: String? = null

    @SerializedName("doorTime")
    internal var door_time: String? = null

    @SerializedName("eventStart")
    internal var event_start: String? = null

    @SerializedName("venueId")
    internal var venue_id: String? = null

    @SerializedName("venueName")
    internal var venue_name: String? = null
}