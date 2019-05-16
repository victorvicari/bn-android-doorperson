package com.bigneon.doorperson.db.dml

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
object TableUsersDML {
    const val TABLE_USERS = "users"
    const val USER_ID = "user_id"
    const val FIRST_NAME = "first_name"
    const val LAST_NAME = "last_name"
    const val EMAIL = "email"
    const val PHONE = "phone"
    const val PROFILE_PIC_URL = "profile_pic_url"

    const val TABLE_USERS_CREATE =
        "create table $TABLE_USERS($USER_ID text not null, $FIRST_NAME text null, $LAST_NAME text null, $EMAIL text not null, $PHONE text null, $PROFILE_PIC_URL text null);"

    const val TABLE_USERS_DELETE = "drop table if exists $TABLE_USERS"

}