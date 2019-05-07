package com.bigneon.doorperson.db.ds

import android.content.ContentValues
import android.database.Cursor
import com.bigneon.doorperson.db.dml.TableEventsDML
import com.bigneon.doorperson.rest.model.EventModel
import java.util.*

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class EventsDS : BaseDS() {
    init {
        open()
    }

    private val allColumns = arrayOf(
        TableEventsDML.EVENT_ID,
        TableEventsDML.NAME,
        TableEventsDML.PROMO_IMAGE_URL
    )

    fun getEvent(eventId: String): EventModel? {
        database?.query(
            TableEventsDML.TABLE_EVENTS,
            allColumns,
            TableEventsDML.EVENT_ID + " = '" + eventId + "'",
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

        database?.query(TableEventsDML.TABLE_EVENTS, allColumns, null, null, null, null, null)?.use {
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
        database?.rawQuery(
            "select count(*) from " + TableEventsDML.TABLE_EVENTS + " where " + TableEventsDML.EVENT_ID + " = '" + eventId + "'",
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
        values.put(TableEventsDML.EVENT_ID, eventId)
        values.put(TableEventsDML.NAME, name)
        values.put(TableEventsDML.PROMO_IMAGE_URL, promoImageURL)

        database?.insert(TableEventsDML.TABLE_EVENTS, null, values)
    }

    fun updateEvent(
        eventId: String,
        name: String,
        promoImageURL: String
    ) {
        val values = ContentValues()
        values.put(TableEventsDML.NAME, name)
        values.put(TableEventsDML.PROMO_IMAGE_URL, promoImageURL)

        database?.update(TableEventsDML.TABLE_EVENTS, values, TableEventsDML.EVENT_ID + " = '" + eventId + "'", null)
    }

    fun deleteEvent(eventId: String) {
        println("EventModel deleted with pk: $eventId")
        database?.delete(TableEventsDML.TABLE_EVENTS, TableEventsDML.EVENT_ID + " = '" + eventId + "'", null)
    }

    private fun cursorToEvent(cursor: Cursor): EventModel {
        val eventModel = EventModel()
        var index = 0
        eventModel.id = cursor.getString(index++)
        eventModel.name = cursor.getString(index++)
        eventModel.promoImageURL = cursor.getString(index)
        return eventModel
    }
}