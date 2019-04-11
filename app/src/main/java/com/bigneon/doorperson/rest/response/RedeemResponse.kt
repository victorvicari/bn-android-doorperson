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

    @SerializedName("user_id")
    internal var userId: String? = null

    @SerializedName("order_id")
    internal var orderId: String? = null

    @SerializedName("order_item_id")
    internal var orderItemId: String? = null

    @SerializedName("price_in_cents")
    internal var priceInCents: Int? = null

    @SerializedName("first_name")
    internal var firstName: String? = null

    @SerializedName("last_name")
    internal var lastName: String? = null

    internal var email: String? = null

    internal var phone: String? = null

    @SerializedName("redeem_key")
    internal var redeemKey: String? = null

    @SerializedName("redeem_date")
    internal var redeemDate: String? = null

    internal var status: String? = null

    @SerializedName("event_id")
    internal var eventId: String? = null

    @SerializedName("event_name")
    internal var eventName: String? = null

    @SerializedName("door_time")
    internal var doorTime: String? = null

    @SerializedName("event_start")
    internal var eventStart: String? = null

    @SerializedName("venue_id")
    internal var venueId: String? = null

    @SerializedName("venue_name")
    internal var venueName: String? = null
}