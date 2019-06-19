package com.bigneon.doorperson.sync

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.support.annotation.Nullable


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 18.06.2019..
 ****************************************************/
class StubContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    @Nullable
    override fun query(
        uri: Uri, projection: Array<String>, selection: String,
        selectionArgs: Array<String>,
        sortOrder: String
    ): Cursor? {
        return null
    }

    @Nullable
    override fun getType(uri: Uri): String? {
        return null
    }

    @Nullable
    override fun insert(uri: Uri, values: ContentValues): Uri? {
        return null
    }

    override fun delete(
        uri: Uri, selection: String,
        selectionArgs: Array<String>
    ): Int {
        return 0
    }

    override fun update(
        uri: Uri, values: ContentValues, selection: String,
        selectionArgs: Array<String>
    ): Int {
        return 0
    }
}