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
        TableTicketsDML.FIRST_NAME,
        TableTicketsDML.LAST_NAME,
        TableTicketsDML.PRICE_IN_CENTS,
        TableTicketsDML.TICKET_TYPE_NAME,
        TableTicketsDML.REDEEM_KEY,
        TableTicketsDML.STATUS
    )

    fun getTicket(ticketId: String): TicketModel? {
        val cursor = database?.query(
            TableTicketsDML.TABLE_TICKETS,
            allColumns,
            TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
            null,
            null,
            null,
            null
        ) ?: return null

        cursor.moveToFirst()
        val model = cursorToTicket(cursor)
        cursor.close()
        return model
    }

    fun getTicketByPk(ticketId: String): TicketModel? {
        val cursor = database?.query(
            TableTicketsDML.TABLE_TICKETS,
            allColumns,
            TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
            null,
            null,
            null,
            null
        ) ?: return null

        cursor.moveToFirst()
        val model = cursorToTicket(cursor)
        cursor.close()
        return model
    }

    fun getAllTicketsForEvent(eventId: String): ArrayList<TicketModel>? {
        val ticketModels = ArrayList<TicketModel>()

        val cursor =
            database?.query(
                TableTicketsDML.TABLE_TICKETS,
                allColumns,
                TableTicketsDML.EVENT_ID + " = '" + eventId + "'",
                null,
                null,
                null,
                null
            ) ?: return null

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val projectModel = cursorToTicket(cursor)
            ticketModels.add(projectModel)
            cursor.moveToNext()
        }
        // make sure to close the cursor
        cursor.close()
        return ticketModels
    }

    fun getAllTickets(): ArrayList<TicketModel>? {
        val ticketModels = ArrayList<TicketModel>()

        val cursor =
            database?.query(TableTicketsDML.TABLE_TICKETS, allColumns, null, null, null, null, null) ?: return null

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val projectModel = cursorToTicket(cursor)
            ticketModels.add(projectModel)
            cursor.moveToNext()
        }
        // make sure to close the cursor
        cursor.close()
        return ticketModels
    }

    fun ticketExists(ticketId: String): Boolean {
        val cursor = database?.rawQuery(
            "select count(*) from " + TableTicketsDML.TABLE_TICKETS + " where " + TableTicketsDML.TICKET_ID + " = '" + ticketId + "'",
            null
        ) ?: return false

        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

    fun createTicket(
        ticketId: String,
        eventId: String,
        firstName: String,
        lastName: String,
        priceInCents: Int,
        ticketTypeName: String,
        redeemKey: String,
        status: String
    ) {
        val values = ContentValues()
        values.put(TableTicketsDML.TICKET_ID, ticketId)
        values.put(TableTicketsDML.EVENT_ID, eventId)
        values.put(TableTicketsDML.FIRST_NAME, firstName)
        values.put(TableTicketsDML.LAST_NAME, lastName)
        values.put(TableTicketsDML.PRICE_IN_CENTS, priceInCents)
        values.put(TableTicketsDML.TICKET_TYPE_NAME, ticketTypeName)
        values.put(TableTicketsDML.REDEEM_KEY, redeemKey)
        values.put(TableTicketsDML.STATUS, status)

        database?.insert(TableTicketsDML.TABLE_TICKETS, null, values)
    }

    fun updateTicket(
        ticketId: String,
        eventId: String,
        firstName: String,
        lastName: String,
        priceInCents: Int,
        ticketTypeName: String,
        redeemKey: String,
        status: String
    ) {
        val values = ContentValues()
        values.put(TableTicketsDML.TICKET_ID, ticketId)
        values.put(TableTicketsDML.EVENT_ID, eventId)
        values.put(TableTicketsDML.FIRST_NAME, firstName)
        values.put(TableTicketsDML.LAST_NAME, lastName)
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
        ticketModel.firstName = cursor.getString(index++)
        ticketModel.lastName = cursor.getString(index++)
        ticketModel.priceInCents = cursor.getInt(index++)
        ticketModel.ticketType = cursor.getString(index++)
        ticketModel.redeemKey = cursor.getString(index++)
        ticketModel.status = cursor.getString(index)
        return ticketModel
    }
}