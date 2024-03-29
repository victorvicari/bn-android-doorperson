package com.bigneon.studio.db.ds

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.bigneon.studio.config.AppConstants.Companion.PAGE_LIMIT
import com.bigneon.studio.db.SQLiteHelper
import com.bigneon.studio.db.dml.TableTicketsDML.EMAIL
import com.bigneon.studio.db.dml.TableTicketsDML.EVENT_ID
import com.bigneon.studio.db.dml.TableTicketsDML.FIRST_NAME
import com.bigneon.studio.db.dml.TableTicketsDML.LAST_NAME
import com.bigneon.studio.db.dml.TableTicketsDML.PHONE
import com.bigneon.studio.db.dml.TableTicketsDML.PRICE_IN_CENTS
import com.bigneon.studio.db.dml.TableTicketsDML.PROFILE_PIC_URL
import com.bigneon.studio.db.dml.TableTicketsDML.REDEEMED_AT
import com.bigneon.studio.db.dml.TableTicketsDML.REDEEMED_BY
import com.bigneon.studio.db.dml.TableTicketsDML.REDEEM_KEY
import com.bigneon.studio.db.dml.TableTicketsDML.STATUS
import com.bigneon.studio.db.dml.TableTicketsDML.TABLE_TICKETS
import com.bigneon.studio.db.dml.TableTicketsDML.TICKET_ID
import com.bigneon.studio.db.dml.TableTicketsDML.TICKET_TYPE
import com.bigneon.studio.db.dml.TableTicketsDML.USER_ID
import com.bigneon.studio.rest.model.TicketModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class TicketsDS {

    private val allColumns = arrayOf(
        TICKET_ID,
        EVENT_ID,
        USER_ID,
        FIRST_NAME,
        LAST_NAME,
        EMAIL,
        PHONE,
        PROFILE_PIC_URL,
        PRICE_IN_CENTS,
        TICKET_TYPE,
        REDEEM_KEY,
        STATUS,
        REDEEMED_BY,
        REDEEMED_AT
    )

    fun getAllTicketNumberForEvent(eventId: String): Int {
        SQLiteHelper.getDB().rawQuery(
            "select count(*) from $TABLE_TICKETS where $EVENT_ID = '$eventId'",
            null
        )?.use {
            if (it.moveToFirst()) {
                it.moveToFirst()
                val count = it.getInt(0)
                it.close()
                return count
            }
        } ?: return 0
        return 0
    }

    private fun ticketExists(ticketId: String): Boolean {
        SQLiteHelper.getDB().rawQuery(
            "select count(*) from $TABLE_TICKETS where $TICKET_ID = '$ticketId'",
            null
        )?.use {
            if (it.moveToFirst()) {
                val count = it.getInt(0)
                it.close()
                return count > 0
            }
        } ?: return false
        return false
    }

    private fun createTicket(db: SQLiteDatabase, ticketModel: TicketModel) {
        createTicket(
            db,
            ticketModel.ticketId!!,
            ticketModel.eventId!!,
            ticketModel.userId!!,
            ticketModel.firstName,
            ticketModel.lastName,
            ticketModel.email,
            ticketModel.phone,
            ticketModel.profilePicURL,
            ticketModel.priceInCents!!,
            ticketModel.ticketType!!,
            ticketModel.redeemKey!!,
            ticketModel.status!!.toUpperCase(),
            ticketModel.redeemedBy,
            ticketModel.redeemedAt
        )
    }

    private fun createTicket(
        db: SQLiteDatabase,
        ticketId: String,
        eventId: String,
        userId: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        phone: String?,
        profilePicURL: String?,
        priceInCents: Int,
        ticketType: String,
        redeemKey: String,
        status: String,
        redeemedBy: String?,
        redeemedAt: String?
    ) {
        val values = ContentValues()
        values.put(TICKET_ID, ticketId)
        values.put(EVENT_ID, eventId)
        values.put(USER_ID, userId)
        values.put(FIRST_NAME, firstName)
        values.put(LAST_NAME, lastName)
        values.put(EMAIL, email)
        values.put(PHONE, phone)
        values.put(PROFILE_PIC_URL, profilePicURL)
        values.put(PRICE_IN_CENTS, priceInCents)
        values.put(TICKET_TYPE, ticketType)
        values.put(REDEEM_KEY, redeemKey)
        values.put(STATUS, status.toUpperCase())
        values.put(REDEEMED_BY, redeemedBy)
        values.put(REDEEMED_AT, redeemedAt)

        db.insert(TABLE_TICKETS, null, values)
    }

    private fun updateTicket(db: SQLiteDatabase, ticketModel: TicketModel) {
        updateTicket(
            db,
            ticketModel.ticketId!!,
            ticketModel.eventId!!,
            ticketModel.userId!!,
            ticketModel.firstName,
            ticketModel.lastName,
            ticketModel.email,
            ticketModel.phone,
            ticketModel.profilePicURL,
            ticketModel.priceInCents!!,
            ticketModel.ticketType!!,
            ticketModel.redeemKey!!,
            ticketModel.status!!.toUpperCase(),
            ticketModel.redeemedBy,
            ticketModel.redeemedAt
        )
    }

    private fun updateTicket(
        db: SQLiteDatabase,
        ticketId: String,
        eventId: String,
        userId: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        phone: String?,
        profilePicURL: String?,
        priceInCents: Int,
        ticketType: String,
        redeemKey: String,
        status: String,
        redeemedBy: String?,
        redeemedAt: String?
    ) {
        val values = ContentValues()
        values.put(EVENT_ID, eventId)
        values.put(USER_ID, userId)
        values.put(FIRST_NAME, firstName)
        values.put(LAST_NAME, lastName)
        values.put(EMAIL, email)
        values.put(PHONE, phone)
        values.put(PROFILE_PIC_URL, profilePicURL)
        values.put(PRICE_IN_CENTS, priceInCents)
        values.put(TICKET_TYPE, ticketType)
        values.put(REDEEM_KEY, redeemKey)
        values.put(STATUS, status.toUpperCase())
        values.put(REDEEMED_BY, redeemedBy)
        values.put(REDEEMED_AT, redeemedAt)

        db.update(
            TABLE_TICKETS,
            values,
            "$TICKET_ID = '$ticketId'",
            null
        )
    }

    fun createOrUpdateTicketList(tickets: ArrayList<TicketModel>) {
        val db = SQLiteHelper.getDB()
        db.beginTransaction()
        tickets.forEach {
            if (ticketExists(it.ticketId!!)) {
                updateTicket(db, it)
            } else {
                createTicket(db, it)
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun setDuplicateTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(STATUS, "DUPLICATE")
        SQLiteHelper.getDB().update(
            TABLE_TICKETS,
            values,
            "$TICKET_ID = '$ticketId'",
            null
        )
        return getTicket(ticketId)
    }

    fun getTicket(ticketId: String): TicketModel? {
        SQLiteHelper.getDB().query(
            TABLE_TICKETS,
            allColumns,
            "$TICKET_ID = '$ticketId'",
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                val ticket = cursorToTicket(it)
                it.close()
                return ticket
            }
        } ?: return null
        return null
    }

    fun setRedeemedTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(STATUS, "REDEEMED")
        SQLiteHelper.getDB().update(
            TABLE_TICKETS,
            values,
            "$TICKET_ID = '$ticketId'",
            null
        )
        return getTicket(ticketId)
    }

    fun getCheckedTicketsForEvent(eventId: String): ArrayList<TicketModel>? {
        val ticketModels = ArrayList<TicketModel>()

        SQLiteHelper.getDB().query(
            TABLE_TICKETS,
            allColumns,
            "$EVENT_ID = '$eventId' AND $STATUS = 'CHECKED'",
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                while (!it.isAfterLast) {
                    val ticket = cursorToTicket(it)
                    ticketModels.add(ticket)
                    it.moveToNext()
                }
                it.close()
                return ticketModels
            }
        } ?: return null
        return null
    }

    fun getTicketsForEvent(eventId: String, filter: String, page: Int): ArrayList<TicketModel>? {
        val ticketModels = ArrayList<TicketModel>()
        val sb = StringBuilder("$EVENT_ID = '$eventId' ")

        val searchWords = filter.split(" ")
        var andAdded = false
        for (word in searchWords) {
            if (word.isEmpty())
                continue
            if (andAdded) {
                sb.append("OR ")
            } else {
                sb.append("AND (")
                andAdded = true
            }
            sb.append("$TICKET_ID LIKE '%$word%' OR $FIRST_NAME LIKE '%$word%' OR $LAST_NAME LIKE '%$word%' OR $EMAIL LIKE '%$word%' OR $PHONE LIKE '%$word%' ")
        }
        if (andAdded) {
            sb.append(")")
        }

        SQLiteHelper.getDB().query(
            TABLE_TICKETS,
            allColumns,
            sb.toString(),
            null,
            null,
            null,
            null,
            "${PAGE_LIMIT.times(page)}, $PAGE_LIMIT"
        )?.use {
            if (it.moveToFirst()) {
                while (!it.isAfterLast) {
                    val ticket = cursorToTicket(it)
                    ticketModels.add(ticket)
                    it.moveToNext()
                }
                it.close()
                return ticketModels
            }
        } ?: return null
        return null
    }

    fun setCheckedTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(STATUS, "CHECKED")
        SQLiteHelper.getDB().update(
            TABLE_TICKETS,
            values,
            "$TICKET_ID = '$ticketId'",
            null
        )
        return getTicket(ticketId)
    }

    fun getRedeemedTicketNumberForEvent(eventId: String): Int {
        SQLiteHelper.getDB().rawQuery(
            "select count(*) from " + TABLE_TICKETS + " where " + EVENT_ID + " = '" + eventId + "' and " +
                    STATUS + " = 'REDEEMED'",
            null
        )?.use {
            if (it.moveToFirst()) {
                val count = it.getInt(0)
                it.close()
                return count
            }
        } ?: return 0
        return 0
    }

    fun getCheckedTicketNumberForEvent(eventId: String): Int {
        SQLiteHelper.getDB().rawQuery(
            "select count(*) from " + TABLE_TICKETS + " where " + EVENT_ID + " = '" + eventId + "' and " +
                    STATUS + " = 'CHECKED'",
            null
        )?.use {
            if (it.moveToFirst()) {
                val count = it.getInt(0)
                it.close()
                return count
            }
        } ?: return 0
        return 0
    }

    private fun cursorToTicket(cursor: Cursor): TicketModel {
        val ticketModel = TicketModel()
        var index = 0
        ticketModel.ticketId = cursor.getString(index++)
        ticketModel.eventId = cursor.getString(index++)
        ticketModel.userId = cursor.getString(index++)
        ticketModel.firstName = cursor.getString(index++)
        ticketModel.lastName = cursor.getString(index++)
        ticketModel.email = cursor.getString(index++)
        ticketModel.phone = cursor.getString(index++)
        ticketModel.profilePicURL = cursor.getString(index++)
        ticketModel.priceInCents = cursor.getInt(index++)
        ticketModel.ticketType = cursor.getString(index++)
        ticketModel.redeemKey = cursor.getString(index++)
        ticketModel.status = cursor.getString(index++).toUpperCase()
        ticketModel.redeemedBy = cursor.getString(index++)
        ticketModel.redeemedAt = cursor.getString(index)
        return ticketModel
    }
}