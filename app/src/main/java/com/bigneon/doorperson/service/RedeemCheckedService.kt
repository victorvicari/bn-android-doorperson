package com.bigneon.doorperson.service

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import org.jetbrains.anko.doAsync

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class RedeemCheckedService : IntentService("RedeemCheckedService") {
    private val TAG = RedeemCheckedService::class.java.simpleName
    override fun onHandleIntent(intent: Intent?) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST)
        val eventId = intent?.getStringExtra("eventId") ?: return
        fun setAccessTokenForEvent(accessToken: String?) {
            if (accessToken != null) {
                val ticketsDS = TicketsDS()
                val checkedTickets = ticketsDS.getCheckedTicketsForEvent(eventId)
                if (checkedTickets != null) {
                    for (ticket in checkedTickets) {
                        doAsync {
                            val isDuplicateTicket = accessToken.let {
                                RestAPI.redeemTicketForEvent(
                                    it,
                                    eventId,
                                    ticket.ticketId!!,
                                    ticket.firstName ?: "",
                                    ticket.lastName ?: "",
                                    ticket.redeemKey!!
                                )
                            }
                            if (isDuplicateTicket!!) {
                                Log.d(
                                    TAG,
                                    "Warning: DUPLICATE TICKET! - Ticket ID: ${ticket.ticketId} has already been redeemed! "
                                )
                                ticketsDS.setDuplicateTicket(ticket.ticketId!!)
                            } else {
                                ticketsDS.setRedeemedTicket(ticket.ticketId!!)
                            }
                        }.get()
                    }
                }
            }
        }
        RestAPI.accessToken(::setAccessTokenForEvent)
    }
}
