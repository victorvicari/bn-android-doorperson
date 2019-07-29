package com.bigneon.doorperson.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import com.bigneon.doorperson.R
import com.bigneon.doorperson.controller.TicketDataHandler
import com.bigneon.doorperson.db.ds.EventsDS
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.TicketModel

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class RedeemCheckedService : IntentService("RedeemCheckedService") {
    private var eventsDS = EventsDS()
    private var ticketsDS = TicketsDS()
    override fun onHandleIntent(intent: Intent?) {
        val allEvents = eventsDS.getAllEvents() ?: return
        for (event in allEvents) {
            val checkedTickets = ticketsDS.getCheckedTicketsForEvent(event.id!!)
            if (checkedTickets != null && checkedTickets.size > 0) {
                fun setAccessTokenForEvent(accessToken: String?) {
                    if (accessToken != null) {
                        RefreshTicketsAsyncTask(checkedTickets, accessToken, event.id!!, ticketsDS, this).execute()
                    }
                }
                RestAPI.accessToken(::setAccessTokenForEvent)
            }
        }
    }
}

class RefreshTicketsAsyncTask(
    private var checkedTickets: ArrayList<TicketModel>,
    private var accessToken: String,
    var eventId: String,
    private var ticketsDS: TicketsDS,
    var context: Context
) : AsyncTask<Void, Void, Unit>() {

    override fun doInBackground(vararg params: Void?) {
        for (ticket in checkedTickets) {
            val redeemTicketResult = RestAPI.redeemTicketForEvent(
                accessToken,
                eventId,
                ticket.ticketId!!,
                ticket.redeemKey!!
            )
            if (redeemTicketResult == null) {
                ticketsDS.setDuplicateTicket(ticket.ticketId!!)
                TicketDataHandler.updateTicket(
                    ticket.ticketId!!,
                    context.getString(R.string.duplicate).toLowerCase()
                )
            } else {
                ticketsDS.setRedeemedTicket(ticket.ticketId!!)
                TicketDataHandler.updateTicket(
                    ticket.ticketId!!,
                    context.getString(R.string.redeemed).toLowerCase()
                )
            }
        }
    }

    override fun onPostExecute(result: Unit) {
        super.onPostExecute(result)
        TicketDataHandler.refreshTicketList()
    }
}
