package com.bigneon.studio.db.dml

import com.bigneon.studio.config.AppConstants.Companion.MAX_TIMESTAMP
import com.bigneon.studio.config.AppConstants.Companion.MIN_TIMESTAMP
import com.bigneon.studio.db.dml.TableEventsDML.TABLE_EVENTS

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
object TableSyncDML {
    const val TABLE_SYNC = "sync"
    const val TABLE_NAME = "table_name"
    const val EVENT_ID = "event_id"
    const val SYNC_DIRECTION = "syn_direction"
    const val LAST_SYNC_TIME = "last_sync_time"

    const val TABLE_SYNC_CREATE =
        "create table $TABLE_SYNC($TABLE_NAME text not null, $EVENT_ID text null, $SYNC_DIRECTION text not null, $LAST_SYNC_TIME text null);"

    const val TABLE_SYNC_INSERT_EVENTS_D =
        "insert into $TABLE_SYNC($TABLE_NAME, $EVENT_ID, $SYNC_DIRECTION, $LAST_SYNC_TIME) values('$TABLE_EVENTS', null,  'D', '$MIN_TIMESTAMP')"
    const val TABLE_SYNC_INSERT_EVENTS_U =
        "insert into $TABLE_SYNC($TABLE_NAME, $EVENT_ID, $SYNC_DIRECTION, $LAST_SYNC_TIME) values('$TABLE_EVENTS', null, 'U', '$MAX_TIMESTAMP')"

    const val TABLE_SYNC_DELETE = "drop table if exists $TABLE_SYNC"
}