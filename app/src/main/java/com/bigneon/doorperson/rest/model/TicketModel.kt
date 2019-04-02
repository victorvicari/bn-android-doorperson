package com.bigneon.doorperson.rest.model

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 02.04.2019..
 ****************************************************/
class TicketModel {
    internal var id: String? = null

    @SerializedName("price_in_cents")
    internal var priceInCents: Int? = null

    @SerializedName("ticket_type_name")
    internal var ticketTypeName: String? = null

    @SerializedName("redeem_key")
    internal var redeemKey: String? = null

    internal var status: String? = null
}