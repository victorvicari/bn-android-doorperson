package com.bigneon.doorperson.db

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.db.dml.TableEventsDML
import com.bigneon.doorperson.db.dml.TableSyncDML
import com.bigneon.doorperson.db.dml.TableTicketsDML
import com.bigneon.doorperson.db.dml.TableUsersDML

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SQLiteHelper : SQLiteOpenHelper(
    context,
    AppConstants.DATABASE_NAME,
    null,
    AppConstants.DATABASE_VERSION
) {
    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun setContext(con: Context) {
            context = con
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(TableSyncDML.TABLE_SYNC_CREATE)
        db?.execSQL(TableEventsDML.TABLE_EVENTS_CREATE)
        db?.execSQL(TableTicketsDML.TABLE_TICKETS_CREATE)
        db?.execSQL(TableUsersDML.TABLE_USERS_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.w(
            SQLiteHelper::class.java.name,
            "Upgrading database from version $oldVersion to $newVersion, which will destroy all old data"
        )
        db?.execSQL(TableUsersDML.TABLE_USERS_DELETE)
        db?.execSQL(TableSyncDML.TABLE_SYNC_DELETE)
        db?.execSQL(TableTicketsDML.TABLE_TICKETS_DELETE)
        db?.execSQL(TableEventsDML.TABLE_EVENTS_DELETE)
        onCreate(db)
    }
}