package com.bigneon.doorperson.db.ds

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.bigneon.doorperson.db.SQLiteHelper

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
open class BaseDS {
    private var dbHelper: SQLiteHelper = SQLiteHelper()
    protected var database: SQLiteDatabase? = null

    @Throws(SQLException::class)
    protected fun open() {
        database = dbHelper.writableDatabase
    }

    // TODO - Analyze where to close DS
    protected fun close() {
        dbHelper.close()
    }
}