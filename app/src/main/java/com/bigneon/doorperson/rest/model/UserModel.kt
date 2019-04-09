package com.bigneon.doorperson.rest.model

import com.google.gson.annotations.SerializedName

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 02.04.2019..
 ****************************************************/
class UserModel {
    internal var id: String? = null

    @SerializedName("first_name")
    internal var firstName: String? = null

    @SerializedName("last_name")
    internal var lastName: String? = null

    internal var email: String? = null
}