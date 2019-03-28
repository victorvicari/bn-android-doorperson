package com.bigneon.doorperson.rest.model

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 21.03.2019..
 ****************************************************/
class DashboardModel {
    @SerializedName("sold_held")
    internal var soldHeld: Int? = null

    @SerializedName("sold_unreserved")
    internal var soldUnreserved: Int? = null

    @SerializedName("tickets_redeemed")
    internal var ticketsRedeemed: Int? = null
}
