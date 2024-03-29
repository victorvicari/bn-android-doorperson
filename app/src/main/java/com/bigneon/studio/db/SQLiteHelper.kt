package com.bigneon.studio.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.bigneon.studio.BigNeonApplication
import com.bigneon.studio.config.AppConstants
import com.bigneon.studio.db.dml.TableEventsDML
import com.bigneon.studio.db.dml.TableSyncDML
import com.bigneon.studio.db.dml.TableTicketsDML

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SQLiteHelper : SQLiteOpenHelper(
    BigNeonApplication.context,
    AppConstants.DATABASE_NAME,
    null,
    AppConstants.DATABASE_VERSION
) {
    companion object {
        private val instance = SQLiteHelper()

        fun getDB(): SQLiteDatabase {
            return instance.writableDatabase
        }

        fun deleteDB() {
            instance.writableDatabase.close()
            BigNeonApplication.context?.deleteDatabase(AppConstants.DATABASE_NAME)
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(TableSyncDML.TABLE_SYNC_CREATE)
        db?.execSQL(TableEventsDML.TABLE_EVENTS_CREATE)
        db?.execSQL(TableTicketsDML.TABLE_TICKETS_CREATE)

        db?.execSQL(TableSyncDML.TABLE_SYNC_INSERT_EVENTS_D)
        db?.execSQL(TableSyncDML.TABLE_SYNC_INSERT_EVENTS_U)
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