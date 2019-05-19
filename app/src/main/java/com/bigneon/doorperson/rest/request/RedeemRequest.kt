package com.bigneon.doorperson.rest.request

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 01.04.2019..
 ****************************************************/
class RedeemRequest {
    @SerializedName("redeem_key")
    internal var redeemKey: String? = null
}