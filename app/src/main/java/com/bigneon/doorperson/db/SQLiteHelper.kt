package com.bigneon.doorperson.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.bigneon.doorperson.DoorpersonApplication
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.db.dml.TableEventsDML
import com.bigneon.doorperson.db.dml.TableSyncDML
import com.bigneon.doorperson.db.dml.TableTicketsDML

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SQLiteHelper : SQLiteOpenHelper(
    DoorpersonApplication.applicationContext(),
    AppConstants.DATABASE_NAME,
    null,
    AppConstants.DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(TableSyncDML.TABLE_SYNC_CREATE)
        db?.execSQL(TableEventsDML.TABLE_EVENTS_CREATE)
        db?.execSQL(TableTicketsDML.TABLE_TICKETS_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.w(
            SQLiteHelper::class.java.name,
            "Upgrading database from version $oldVersion to $newVersion, which will destroy all old data"
        )
        db?.execSQL(TableSyncDML.TABLE_SYNC_DELETE)
        db?.execSQL(TableTicketsDML.TABLE_TICKETS_DELETE)
        db?.execSQL(TableEventsDML.TABLE_EVENTS_DELETE)
        onCreate(db)
    }
}