package com.bigneon.studio.db.ds

import android.content.ContentValues
import android.database.Cursor
import com.bigneon.studio.config.AppConstants
import com.bigneon.studio.config.AppConstants.Companion.MAX_TIMESTAMP
import com.bigneon.studio.config.AppConstants.Companion.MIN_TIMESTAMP
import com.bigneon.studio.db.SQLiteHelper
import com.bigneon.studio.db.dml.TableSyncDML.EVENT_ID
import com.bigneon.studio.db.dml.TableSyncDML.LAST_SYNC_TIME
import com.bigneon.studio.db.dml.TableSyncDML.SYNC_DIRECTION
import com.bigneon.studio.db.dml.TableSyncDML.TABLE_NAME
import com.bigneon.studio.db.dml.TableSyncDML.TABLE_SYNC
import com.bigneon.studio.rest.model.SyncModel
import com.bigneon.studio.util.AppUtils.Companion.getCurrentTimestamp

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class SyncDS {
    private val allColumns = arrayOf(
        TABLE_NAME,
        EVENT_ID,
        SYNC_DIRECTION,
        LAST_SYNC_TIME
    )

    fun getLastSyncTime(syncTableName: AppConstants.SyncTableName, eventId: String, upload: Boolean): String? {
        SQLiteHelper.getDB().query(
            TABLE_SYNC,
            allColumns,
            (TABLE_NAME + " = \"" + syncTableName + "\" and " + EVENT_ID + " = \"" + eventId + "\" and " + SYNC_DIRECTION + " = \"" + (if (upload) "U" else "D") + "\""),
            null,
            null,
            null,
            null
        )?.use {
            return if (it.moveToFirst()) {
                val syncModel = cursorToSync(it)
                it.close()
                syncModel.lastSyncTime
            } else {
                if (upload) {
                    MAX_TIMESTAMP
                } else {
                    MIN_TIMESTAMP
                }
            }
        } ?: return null
    }

    fun setLastSyncTime(syncTableName: AppConstants.SyncTableName, eventId: String, upload: Boolean) {
        SQLiteHelper.getDB().query(
            TABLE_SYNC,
            allColumns,
            (TABLE_NAME + " = \"" + syncTableName + "\" and " + EVENT_ID + " = \"" + eventId + "\" and " + SYNC_DIRECTION + " = \"" + (if (upload) "U" else "D") + "\""),
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                // Update existing
                val values = ContentValues()

                values.put(LAST_SYNC_TIME, getCurrentTimestamp())

                SQLiteHelper.getDB().update(
                    TABLE_SYNC,
                    values,
                    (TABLE_NAME + " = \"" + syncTableName + "\" and " + EVENT_ID + " = \"" + eventId + "\" and " + SYNC_DIRECTION + " = \"" + (if (upload) "U" else "D") + "\""),
                    null
                )
            } else {
                // Create new
                val values = ContentValues()
                values.put(TABLE_NAME, syncTableName.toString())
                values.put(EVENT_ID, eventId)
                values.put(SYNC_DIRECTION, if (upload) "U" else "D")
                values.put(LAST_SYNC_TIME, getCurrentTimestamp())
                SQLiteHelper.getDB().insert(TABLE_SYNC, null, values)
            }
        }
    }

    private fun cursorToSync(cursor: Cursor): SyncModel {
        val syncModel = SyncModel()
        var index = 0
        syncModel.tableName = cursor.getString(index++)
        syncModel.eventId = cursor.getString(index++)
        syncModel.syncDirection = cursor.getString(index++)
        syncModel.lastSyncTime = cursor.getString(index)
        return syncModel
    }
}