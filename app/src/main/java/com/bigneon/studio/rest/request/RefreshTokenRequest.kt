package com.bigneon.studio.rest.request

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.03.2019..
 ****************************************************/
class RefreshTokenRequest {
    @SerializedName("refresh_token")
    internal var refreshToken: String? = null
}