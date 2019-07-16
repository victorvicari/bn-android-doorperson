package com.bigneon.doorperson.rest

import com.bigneon.doorperson.rest.model.EventModel
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.rest.request.AuthRequest
import com.bigneon.doorperson.rest.request.RedeemRequest
import com.bigneon.doorperson.rest.request.RefreshTokenRequest
import com.bigneon.doorperson.rest.response.*
import retrofit2.Call
import retrofit2.http.*

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.03.2019..
 ****************************************************/
interface RestClient {
    @POST("auth/token")
    fun authenticate(
        @Body authRequest: AuthRequest
    ): Call<AuthTokenResponse>

    @POST("auth/token/refresh")
    fun refreshToken(
        @Body refreshTokenRequest: RefreshTokenRequest
    ): Call<AuthTokenResponse>

    @GET("events/checkins")
    fun getScannableEvents(
        @Header("Authorization") token: String
    ): Call<EventsResponse>

    @GET("events/{event_id}/guests")
    fun getTicketsForEvent(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: String,
        @Query("changes_since") changesSince: String?,
        @Query("limit") limit: Int?,
        @Query("page") page: Int?,
        @Query("query") query: String?
    ): Call<TicketsResponse>

    @POST("events/{event_id}/redeem/{ticket_id}")
    fun redeemTicketForEvent(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: String,
        @Path("ticket_id") ticketId: String,
        @Body redeemRequest: RedeemRequest
    ): Call<TicketModel>

    @GET("events/{event_id}")
    fun getEvent(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: String
    ): Call<EventModel>

    @GET("events/{event_id}/dashboard")
    fun getEventDashboard(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: String
    ): Call<EventDashboardResponse>

    @GET("tickets/{ticket_id}")
    fun getTicket(
        @Header("Authorization") token: String,
        @Path("ticket_id") ticketId: String
    ): Call<TicketResponse>
}