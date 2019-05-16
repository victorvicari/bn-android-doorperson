package com.bigneon.doorperson.db.dml

import com.bigneon.doorperson.util.AppUtils.Companion.MAX_TIMESTAMP
import com.bigneon.doorperson.util.AppUtils.Companion.MIN_TIMESTAMP

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
object TableSyncDML {
    const val TABLE_SYNC = "sync"
    const val TABLE_NAME = "table_name"
    const val SYNC_DIRECTION = "syn_direction"
    const val LAST_SYNC_TIME = "last_sync_time"

    const val TABLE_SYNC_CREATE =
        "create table $TABLE_SYNC($TABLE_NAME text not null, $SYNC_DIRECTION text not null, $LAST_SYNC_TIME text null);"

    const val TABLE_SYNC_INSERT_EVENTS_D =
        "insert into $TABLE_SYNC($TABLE_NAME, $SYNC_DIRECTION, $LAST_SYNC_TIME) values('${TableEventsDML.TABLE_EVENTS}', 'D', '$MIN_TIMESTAMP')"
    const val TABLE_SYNC_INSERT_EVENTS_U =
        "insert into $TABLE_SYNC($TABLE_NAME, $SYNC_DIRECTION, $LAST_SYNC_TIME) values('${TableEventsDML.TABLE_EVENTS}', 'U', '$MAX_TIMESTAMP')"
    const val TABLE_SYNC_INSERT_TICKETS_D =
        "insert into $TABLE_SYNC($TABLE_NAME, $SYNC_DIRECTION, $LAST_SYNC_TIME) values('${TableTicketsDML.TABLE_TICKETS}', 'D', '$MIN_TIMESTAMP')"
    const val TABLE_SYNC_INSERT_TICKETS_U =
        "insert into $TABLE_SYNC($TABLE_NAME, $SYNC_DIRECTION, $LAST_SYNC_TIME) values('${TableTicketsDML.TABLE_TICKETS}', 'U', '$MAX_TIMESTAMP')"

    const val TABLE_SYNC_DELETE = "drop table if exists $TABLE_SYNC"
}