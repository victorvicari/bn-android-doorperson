package com.bigneon.doorperson.db.ds

import android.content.ContentValues
import android.database.Cursor
import com.bigneon.doorperson.db.dml.TableTicketsDML
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
        TableTicketsDML.TICKET_ID,
        TableTicketsDML.EVENT_ID,
        TableTicketsDML.USER_ID,
        TableTicketsDML.FIRST_NAME,
        TableTicketsDML.LAST_NAME,
        TableTicketsDML.EMAIL,
        TableTicketsDML.PHONE,
        TableTicketsDML.PROFILE_PIC_URL,
        TableTicketsDML.PRICE_IN_CENTS,
        TableTicketsDML.TICKET_TYPE_NAME,
        TableTicketsDML.REDEEM_KEY,
        TableTicketsDML.STATUS
    )

    fun getTicket(ticketId: String): TicketModel? {
        database?.query(
            TableTicketsDML.TABLE_TICKETS,
            allColumns,
            TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
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
            TableTicketsDML.TABLE_TICKETS,
            allColumns,
            TableTicketsDML.EVENT_ID + " = '" + eventId + "'",
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
            TableTicketsDML.TABLE_TICKETS,
            allColumns,
            TableTicketsDML.STATUS + " = 'CHECKED'",
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

        database?.query(TableTicketsDML.TABLE_TICKETS, allColumns, null, null, null, null, null)?.use {
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
            "select count(*) from " + TableTicketsDML.TABLE_TICKETS + " where " + TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
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
            "select count(*) from " + TableTicketsDML.TABLE_TICKETS + " where " + TableTicketsDML.EVENT_ID + " = '" + eventId + "' and " +
                    TableTicketsDML.STATUS + " = 'REDEEMED'",
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
            "select count(*) from " + TableTicketsDML.TABLE_TICKETS + " where " + TableTicketsDML.EVENT_ID + " = '" + eventId + "' and " +
                    TableTicketsDML.STATUS + " = 'CHECKED'",
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
            "select count(*) from " + TableTicketsDML.TABLE_TICKETS + " where " + TableTicketsDML.EVENT_ID + " = '" + eventId + "'",
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
        values.put(TableTicketsDML.STATUS, "CHECKED")
        database?.update(
            TableTicketsDML.TABLE_TICKETS,
            values,
            TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
            null
        )
        return getTicket(ticketId)
    }

    fun setPurchasedTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(TableTicketsDML.STATUS, "PURCHASED")
        database?.update(
            TableTicketsDML.TABLE_TICKETS,
            values,
            TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
            null
        )
        return getTicket(ticketId)
    }

    fun setRedeemedTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(TableTicketsDML.STATUS, "REDEEMED")
        database?.update(
            TableTicketsDML.TABLE_TICKETS,
            values,
            TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
            null
        )
        return getTicket(ticketId)
    }

    fun setDuplicateTicket(ticketId: String): TicketModel? {
        val values = ContentValues()
        values.put(TableTicketsDML.STATUS, "DUPLICATE")
        database?.update(
            TableTicketsDML.TABLE_TICKETS,
            values,
            TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
            null
        )
        return getTicket(ticketId)
    }

    fun createTicket(
        ticketId: String,
        eventId: String,
        userId: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        phone: String?,
        profilePicURL: String?,
        priceInCents: Int,
        ticketTypeName: String,
        redeemKey: String,
        status: String
    ) {
        val values = ContentValues()
        values.put(TableTicketsDML.TICKET_ID, ticketId)
        values.put(TableTicketsDML.EVENT_ID, eventId)
        values.put(TableTicketsDML.USER_ID, userId)
        values.put(TableTicketsDML.FIRST_NAME, firstName)
        values.put(TableTicketsDML.LAST_NAME, lastName)
        values.put(TableTicketsDML.EMAIL, email)
        values.put(TableTicketsDML.PHONE, phone)
        values.put(TableTicketsDML.PROFILE_PIC_URL, profilePicURL)
        values.put(TableTicketsDML.PRICE_IN_CENTS, priceInCents)
        values.put(TableTicketsDML.TICKET_TYPE_NAME, ticketTypeName)
        values.put(TableTicketsDML.REDEEM_KEY, redeemKey)
        values.put(TableTicketsDML.STATUS, status)

        database?.insert(TableTicketsDML.TABLE_TICKETS, null, values)
    }

    fun updateTicket(
        ticketId: String,
        eventId: String,
        userId: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        phone: String?,
        profilePicURL: String?,
        priceInCents: Int,
        ticketTypeName: String,
        redeemKey: String,
        status: String
    ) {
        val values = ContentValues()
        values.put(TableTicketsDML.EVENT_ID, eventId)
        values.put(TableTicketsDML.USER_ID, userId)
        values.put(TableTicketsDML.FIRST_NAME, firstName)
        values.put(TableTicketsDML.LAST_NAME, lastName)
        values.put(TableTicketsDML.EMAIL, email)
        values.put(TableTicketsDML.PHONE, phone)
        values.put(TableTicketsDML.PROFILE_PIC_URL, profilePicURL)
        values.put(TableTicketsDML.PRICE_IN_CENTS, priceInCents)
        values.put(TableTicketsDML.TICKET_TYPE_NAME, ticketTypeName)
        values.put(TableTicketsDML.REDEEM_KEY, redeemKey)
        values.put(TableTicketsDML.STATUS, status)

        database?.update(
            TableTicketsDML.TABLE_TICKETS,
            values,
            TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
            null
        )
    }

    fun deleteTicket(ticketId: String) {
        println("TicketModel deleted with pk: $ticketId")
        database?.delete(TableTicketsDML.TABLE_TICKETS, TableTicketsDML.TICKET_ID + " = '" + ticketId + "'", null)
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
        ticketModel.status = cursor.getString(index)
        return ticketModel
    }
}