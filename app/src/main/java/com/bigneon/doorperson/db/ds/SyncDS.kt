package com.bigneon.doorperson.db.ds

import android.content.ContentValues
import android.database.Cursor
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.AppConstants.Companion.MAX_TIMESTAMP
import com.bigneon.doorperson.config.AppConstants.Companion.MIN_TIMESTAMP
import com.bigneon.doorperson.db.SQLiteHelper
import com.bigneon.doorperson.db.dml.TableSyncDML.LAST_SYNC_TIME
import com.bigneon.doorperson.db.dml.TableSyncDML.SYNC_DIRECTION
import com.bigneon.doorperson.db.dml.TableSyncDML.TABLE_NAME
import com.bigneon.doorperson.db.dml.TableSyncDML.TABLE_SYNC
import com.bigneon.doorperson.rest.model.SyncModel
import com.bigneon.doorperson.util.AppUtils

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SyncDS {
    private val allColumns = arrayOf(
        TABLE_NAME,
        SYNC_DIRECTION,
        LAST_SYNC_TIME
    )

    fun getLastSyncTime(syncTableName: AppConstants.SyncTableName, upload: Boolean): String? {
        SQLiteHelper.getDB().query(
            TABLE_SYNC,
            allColumns,
            (TABLE_NAME + " = \"" + syncTableName + "\" and " + SYNC_DIRECTION + " = \"" + (if (upload) "U" else "D") + "\""),
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                val syncModel = cursorToSync(it)
                it.close()
                return syncModel.lastSyncTime
            } else {
                if (upload) {
                    MAX_TIMESTAMP
                } else {
                    MIN_TIMESTAMP
                }
            }
        } ?: return null
        return null
    }

    fun setLastSyncTime(syncTableName: AppConstants.SyncTableName, upload: Boolean) {
        val values = ContentValues()

        values.put(LAST_SYNC_TIME, AppUtils.getCurrentTimestamp())

        SQLiteHelper.getDB().update(
            TABLE_SYNC,
            values,
            (TABLE_NAME + " = \"" + syncTableName + "\" and " + SYNC_DIRECTION + " = \"" + (if (upload) "U" else "D") + "\""),
            null
        )
    }

    private fun cursorToSync(cursor: Cursor): SyncModel {
        val syncModel = SyncModel()
        var index = 0
        syncModel.tableName = cursor.getString(index++)
        syncModel.syncDirection = cursor.getString(index++)
        syncModel.lastSyncTime = cursor.getString(index)
        return syncModel
    }
}