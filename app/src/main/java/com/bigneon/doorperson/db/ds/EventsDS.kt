package com.bigneon.doorperson.db.ds

import android.content.ContentValues
import android.database.Cursor
import com.bigneon.doorperson.db.SQLiteHelper
import com.bigneon.doorperson.db.dml.TableEventsDML.NAME
import com.bigneon.doorperson.db.dml.TableEventsDML.PROMO_IMAGE_URL
import com.bigneon.doorperson.db.dml.TableEventsDML.TABLE_EVENTS
import com.bigneon.doorperson.db.dml.TableTicketsDML.EVENT_ID
import com.bigneon.doorperson.rest.model.EventModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class EventsDS {
    private val allColumns = arrayOf(
        EVENT_ID,
        NAME,
        PROMO_IMAGE_URL
    )

    fun getEvent(eventId: String): EventModel? {
        SQLiteHelper.getDB().query(
            TABLE_EVENTS,
            allColumns,
            "$EVENT_ID = '$eventId'",
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                return cursorToEvent(it)
            }
        } ?: return null
        return null
    }

    fun getAllEvents(): ArrayList<EventModel>? {
        val eventModels = ArrayList<EventModel>()

        SQLiteHelper.getDB().query(TABLE_EVENTS, allColumns, null, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                while (!it.isAfterLast) {
                    val projectModel = cursorToEvent(it)
                    eventModels.add(projectModel)
                    it.moveToNext()
                }
                return eventModels
            }
        } ?: return null
        return null
    }

    fun eventExists(eventId: String): Boolean {
        SQLiteHelper.getDB().rawQuery(
            "select count(*) from $TABLE_EVENTS where $EVENT_ID = '$eventId'",
            null
        )?.use {
            if (it.moveToFirst()) {
                val count = it.getInt(0)
                return count > 0
            }
        } ?: return false

        return false
    }

    fun createEvent(
        eventId: String,
        name: String,
        promoImageURL: String
    ) {
        val values = ContentValues()
        values.put(EVENT_ID, eventId)
        values.put(NAME, name)
        values.put(PROMO_IMAGE_URL, promoImageURL)

        val db = SQLiteHelper.getDB()
        db.insert(TABLE_EVENTS, null, values)
        SQLiteHelper.closeDB(db)
    }

    fun updateEvent(
        eventId: String,
        name: String,
        promoImageURL: String
    ) {
        val values = ContentValues()
        values.put(NAME, name)
        values.put(PROMO_IMAGE_URL, promoImageURL)

        val db = SQLiteHelper.getDB()
        db.update(TABLE_EVENTS, values, "$EVENT_ID = '$eventId'", null)
        SQLiteHelper.closeDB(db)
    }

    private fun cursorToEvent(cursor: Cursor): EventModel {
        val eventModel = EventModel()
        var index = 0
        eventModel.id = cursor.getString(index++)
        eventModel.name = cursor.getString(index++)
        eventModel.promoImageURL = cursor.getString(index++)
        return eventModel
    }
}