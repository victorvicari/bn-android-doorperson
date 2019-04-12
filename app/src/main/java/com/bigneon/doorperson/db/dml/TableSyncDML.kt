package com.bigneon.doorperson.db.dml

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
        ("create table $TABLE_SYNC($TABLE_NAME text not null, $SYNC_DIRECTION text not null, $LAST_SYNC_TIME text null);")

    const val TABLE_SYNC_DELETE = "drop table if exists $TABLE_SYNC"
}