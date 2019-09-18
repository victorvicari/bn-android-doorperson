package com.bigneon.studio.rest.response

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.03.2019..
 ****************************************************/
class AuthTokenResponse {
    @SerializedName("access_token")
    internal var accessToken: String? = null

    @SerializedName("refresh_token")
    internal var refreshToken: String? = null
}