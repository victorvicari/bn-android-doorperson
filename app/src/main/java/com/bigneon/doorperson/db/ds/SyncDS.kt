package com.bigneon.doorperson.db.ds

import android.content.ContentValues
import android.database.Cursor
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.db.dml.TableSyncDML
import com.bigneon.doorperson.rest.model.SyncModel
import com.bigneon.doorperson.util.AppUtils

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SyncDS : BaseDS() {
    private val MIN_TIMESTAMP = "2000-01-01 00:00:00"
    private val MAX_TIMESTAMP = "2100-01-01 00:00:00"

    private val allColumns = arrayOf(
        TableSyncDML.SYN_TABLE_NAME,
        TableSyncDML.SYN_UPLOAD,
        TableSyncDML.SYN_LAST_SYNC_TIME
    )

    fun getLastSyncTime(syncTableName: AppConstants.SyncTableName, upload: Boolean): String? {
        val cursor = database?.query(
            TableSyncDML.TABLE_SYNC,
            allColumns,
            (TableSyncDML.SYN_TABLE_NAME + " = \"" + syncTableName + "\" and " + TableSyncDML.SYN_UPLOAD + " = \"" + (if (upload) "TRUE" else "FALSE") + "\""),
            null,
            null,
            null,
            null
        ) ?: return null

        return if (cursor.count > 0) {
            cursor.moveToFirst()
            val syncModel = cursorToSync(cursor)
            cursor.close()
            syncModel.lastSyncTime
        } else {
            if (upload) {
                MAX_TIMESTAMP
            } else {
                MIN_TIMESTAMP
            }
        }
    }

    fun setLastSyncTime(syncTableName: AppConstants.SyncTableName, upload: Boolean) {
        val cursor = database?.rawQuery(
            "select count(*) from " + TableSyncDML.TABLE_SYNC + " where " + TableSyncDML.SYN_TABLE_NAME + " = \"" + syncTableName + "\" and "
                    + TableSyncDML.SYN_UPLOAD + " = \"" + (if (upload) "TRUE" else "FALSE") + "\"",
            null
        ) ?: return

        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) { // INSERT
            val values = ContentValues()

            values.put(TableSyncDML.SYN_TABLE_NAME, syncTableName.toString())
            values.put(TableSyncDML.SYN_UPLOAD, if (upload) "TRUE" else "FALSE")
            values.put(TableSyncDML.SYN_LAST_SYNC_TIME, AppUtils.getCurrentTimestamp())

            database?.insert(TableSyncDML.TABLE_SYNC, null, values)
        } else { // UPDATE
            val values = ContentValues()

            values.put(TableSyncDML.SYN_LAST_SYNC_TIME, AppUtils.getCurrentTimestamp())

            database?.update(
                TableSyncDML.TABLE_SYNC,
                values,
                (TableSyncDML.SYN_TABLE_NAME + " = \"" + syncTableName + "\" and " + TableSyncDML.SYN_UPLOAD + " = \"" + (if (upload) "TRUE" else "FALSE") + "\""),
                null
            )
        }
    }

    private fun cursorToSync(cursor: Cursor): SyncModel {
        val syncModel = SyncModel()
        var index = 0
        syncModel.tableName = cursor.getString(index++)
        syncModel.upload = cursor.getString(index++)
        syncModel.lastSyncTime = cursor.getString(index)
        return syncModel
    }
}