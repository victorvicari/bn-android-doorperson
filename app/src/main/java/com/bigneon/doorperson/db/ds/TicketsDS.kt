package com.bigneon.doorperson.db.ds

import android.content.ContentValues
import android.database.Cursor
import com.bigneon.doorperson.db.dml.TableTicketsDML
import com.bigneon.doorperson.db.dml.TableTicketsDML.EMAIL
import com.bigneon.doorperson.db.dml.TableTicketsDML.EVENT_ID
import com.bigneon.doorperson.db.dml.TableTicketsDML.FIRST_NAME
import com.bigneon.doorperson.db.dml.TableTicketsDML.LAST_NAME
import com.bigneon.doorperson.db.dml.TableTicketsDML.PHONE
import com.bigneon.doorperson.db.dml.TableTicketsDML.PRICE_IN_CENTS
import com.bigneon.doorperson.db.dml.TableTicketsDML.PROFILE_PIC_URL
import com.bigneon.doorperson.db.dml.TableTicketsDML.REDEEMED_AT
import com.bigneon.doorperson.db.dml.TableTicketsDML.REDEEMED_BY
import com.bigneon.doorperson.db.dml.TableTicketsDML.REDEEM_KEY
import com.bigneon.doorperson.db.dml.TableTicketsDML.STATUS
import com.bigneon.doorperson.db.dml.TableTicketsDML.TABLE_TICKETS
import com.bigneon.doorperson.db.dml.TableTicketsDML.TICKET_ID
import com.bigneon.doorperson.db.dml.TableTicketsDML.TICKET_TYPE
import com.bigneon.doorperson.db.dml.TableTicketsDML.USER_ID
import com.bigneon.doorperson.rest.model.TicketModel
import java.util.*

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 09.04.2019..
 ****************************************************/
class TicketsDS : BaseDS() {
    init {
        open()
    }

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

    fun getTicket(ticketId: String): TicketModel? {
        database?.query(
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

    fun getAllTicketsForEvent(eventId: String): ArrayList<TicketModel>? {
        val ticketModels = ArrayList<TicketModel>()
        database?.query(
            TABLE_TICKETS,
            allColumns,
            "$EVENT_ID = '$eventId'",
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

    fun getAllCheckedTickets(): ArrayList<TicketModel>? {
        val ticketModels = ArrayList<TicketModel>()
        database?.query(
            TABLE_TICKETS,
            allColumns,
            "$STATUS = 'CHECKED'",
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

    fun getAllTickets(): ArrayList<TicketModel>? {
        val ticketModels = ArrayList<TicketModel>()
        database?.query(TABLE_TICKETS, allColumns, null, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                while (!it.isAfterLast) {
                    val projectModel = cursorToTicket(it)
                    ticketModels.add(projectModel)
                    it.moveToNext()
                }
                it.close()
                return ticketModels
            }
        } ?: return null
        return null
    }

    fun ticketExists(ticketId: String): Boolean {
        database?.rawQuery(
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

    fun getRedeemedTicketNumberForEvent(eventId: String): Int {
        database?.rawQuery(
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
        database?.rawQuery(
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

    fun getAllTicketNumberForEvent(eventId: String): Int {
        database?.rawQuery(
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

    fun setCheckedTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(STATUS, "CHECKED")
        database?.update(
            TABLE_TICKETS,
            values,
            "$TICKET_ID = '$ticketId'",
            null
        )
        return getTicket(ticketId)
    }

    fun setPurchasedTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(STATUS, "PURCHASED")
        database?.update(
            TABLE_TICKETS,
            values,
            "$TICKET_ID = '$ticketId'",
            null
        )
        return getTicket(ticketId)
    }

    fun setRedeemedTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(STATUS, "REDEEMED")
        database?.update(
            TABLE_TICKETS,
            values,
            "$TICKET_ID = '$ticketId'",
            null
        )
        return getTicket(ticketId)
    }

    fun setDuplicateTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(STATUS, "DUPLICATE")
        database?.update(
            TABLE_TICKETS,
            values,
            "$TICKET_ID = '$ticketId'",
            null
        )
        return getTicket(ticketId)
    }

    private fun createTicket(ticketModel: TicketModel) {
        createTicket(
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
            ticketModel.status!!,
            ticketModel.redeemedBy,
            ticketModel.redeemedAt
        )
    }

    private fun createTicket(
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
        values.put(STATUS, status)
        values.put(REDEEMED_BY, redeemedBy)
        values.put(REDEEMED_AT, redeemedAt)

        database?.insert(TABLE_TICKETS, null, values)
    }

    fun updateTicket(ticketModel: TicketModel) {
        updateTicket(
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
            ticketModel.status!!,
            ticketModel.redeemedBy,
            ticketModel.redeemedAt
        )
    }

    private fun updateTicket(
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
        values.put(STATUS, status)
        values.put(REDEEMED_BY, redeemedBy)
        values.put(REDEEMED_AT, redeemedAt)

        database?.update(
            TableTicketsDML.TABLE_TICKETS,
            values,
            "$TICKET_ID = '$ticketId'",
            null
        )
    }

    fun createTicketList(tickets: ArrayList<TicketModel>) {
        database?.beginTransaction()
        tickets.forEach {
            createTicket(it)
        }
        database?.setTransactionSuccessful()
        database?.endTransaction()
    }

    fun createOrUpdateTicketList(tickets: ArrayList<TicketModel>) {
        database?.beginTransaction()
        tickets.forEach {
            if (ticketExists(it.ticketId!!)) {
                updateTicket(it)
            } else {
                createTicket(it)
            }
        }
        database?.setTransactionSuccessful()
        database?.endTransaction()
    }

    fun deleteTicket(ticketId: String) {
        println("TicketModel deleted with pk: $ticketId")
        database?.delete(TABLE_TICKETS, "$TICKET_ID = '$ticketId'", null)
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
        ticketModel.status = cursor.getString(index++)
        ticketModel.redeemedBy = cursor.getString(index++)
        ticketModel.redeemedAt = cursor.getString(index)
        return ticketModel
    }
}