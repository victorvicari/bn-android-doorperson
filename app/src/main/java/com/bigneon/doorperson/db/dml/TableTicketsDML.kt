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
    const val FIRST_NAME = "first_name"
    const val LAST_NAME = "last_name"
    const val PRICE_IN_CENTS = "price_in_cents"
    const val TICKET_TYPE_NAME = "ticket_type_name"
    const val REDEEM_KEY = "redeem_key"
    const val STATUS = "status"

    const val TABLE_TICKETS_CREATE =
        ("create table $TABLE_TICKETS($TICKET_ID text not null, $EVENT_ID text not null, $FIRST_NAME text not null, $LAST_NAME text not null, $PRICE_IN_CENTS integer not null, $TICKET_TYPE_NAME text not null, $REDEEM_KEY text not null, $STATUS text not null);")

    const val TABLE_TICKETS_DELETE = "drop table if exists $TABLE_TICKETS"

}