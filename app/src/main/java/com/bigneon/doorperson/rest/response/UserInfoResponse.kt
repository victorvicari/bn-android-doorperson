package com.bigneon.doorperson.rest.response

import com.bigneon.doorperson.rest.model.UserModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 21.03.2019..
 ****************************************************/
class UserInfoResponse {
    internal var user: UserModel? = null

    internal var roles: ArrayList<String>? = null
}