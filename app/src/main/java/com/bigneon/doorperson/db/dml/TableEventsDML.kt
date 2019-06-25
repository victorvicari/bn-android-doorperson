package com.bigneon.doorperson.db.dml

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
object TableEventsDML {
    const val TABLE_EVENTS = "events"
    const val EVENT_ID = "event_id"
    const val NAME = "name"
    const val PROMO_IMAGE_URL = "promo_image_url"
    const val TOTAL_NUM_OF_TICKETS = "total_num_of_tickets"

    const val TABLE_EVENTS_CREATE =
        "create table $TABLE_EVENTS($EVENT_ID text not null, $NAME text not null, $PROMO_IMAGE_URL text not null, $TOTAL_NUM_OF_TICKETS integer null);"

    const val TABLE_EVENTS_DELETE = "drop table if exists $TABLE_EVENTS"
}