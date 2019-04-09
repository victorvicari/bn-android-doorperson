package com.bigneon.doorperson.db.dml

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
object TableSyncDML {
    const val TABLE_SYNC = "sync"
    const val SYN_TABLE_NAME = "syn_table_name"
    const val SYN_UPLOAD = "syn_upload"
    const val SYN_LAST_SYNC_TIME = "syn_last_sync_time"

    const val TABLE_SYNC_CREATE =
        ("create table $TABLE_SYNC($SYN_TABLE_NAME text not null, $SYN_UPLOAD text not null, $SYN_LAST_SYNC_TIME text null);")

    const val TABLE_SYNC_DELETE = "drop table if exists $TABLE_SYNC"
}