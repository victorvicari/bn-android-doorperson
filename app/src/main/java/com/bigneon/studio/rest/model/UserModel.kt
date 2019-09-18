package com.bigneon.studio.rest.model

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 17.04.2019..
 ****************************************************/
class UserModel {
    @SerializedName("id")
    internal var userId: String? = null

    @SerializedName("first_name")
    internal var firstName: String? = null

    @SerializedName("last_name")
    internal var lastName: String? = null

    internal var email: String? = null

    internal var phone: String? = null

    @SerializedName("profile_pic_url")
    internal var profilePicURL: String? = null
}
