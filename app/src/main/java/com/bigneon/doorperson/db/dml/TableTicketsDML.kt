package com.bigneon.doorperson.db.dml

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
object TableTicketsDML {
    const val TABLE_TICKETS = "tickets"
    const val TICKET_ID = "ticket_id"
    const val EVENT_ID = "event_id"
    const val USER_ID = "user_id"
    const val FIRST_NAME = "first_name"
    const val LAST_NAME = "last_name"
    const val EMAIL = "email"
    const val PHONE = "phone"
    const val PROFILE_PIC_URL = "profile_pic_url"
    const val PRICE_IN_CENTS = "price_in_cents"
    const val TICKET_TYPE_NAME = "ticket_type_name"
    const val REDEEM_KEY = "redeem_key"
    const val STATUS = "status"
    const val REDEEMED_BY = "redeemed_by"
    const val REDEEMED_AT = "redeemed_at"

    const val TABLE_TICKETS_CREATE =
        "create table $TABLE_TICKETS($TICKET_ID text not null, $EVENT_ID text not null, $USER_ID text null, $FIRST_NAME text null, " +
                "$LAST_NAME text null, $EMAIL text null, $PHONE text null, $PROFILE_PIC_URL text null, $PRICE_IN_CENTS integer not null, " +
                "$TICKET_TYPE_NAME text not null, $REDEEM_KEY text not null, $STATUS text not null, $REDEEMED_BY text null, $REDEEMED_AT text null);"

    const val TABLE_TICKETS_DELETE = "drop table if exists $TABLE_TICKETS"
}