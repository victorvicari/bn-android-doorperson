package com.bigneon.doorperson.rest

import com.bigneon.doorperson.rest.request.AuthRequest
import com.bigneon.doorperson.rest.request.RedeemRequest
import com.bigneon.doorperson.rest.request.RefreshTokenRequest
import com.bigneon.doorperson.rest.response.AuthTokenResponse
import com.bigneon.doorperson.rest.response.EventsResponse
import com.bigneon.doorperson.rest.response.RedeemResponse
import com.bigneon.doorperson.rest.response.TicketsResponse
import retrofit2.Call
import retrofit2.http.*


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.03.2019..
 ****************************************************/
interface RestClient {
    @POST("auth/token")
    fun authenticate(@Body authRequest: AuthRequest): Call<AuthTokenResponse>

    @POST("auth/token/refresh")
    fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Call<AuthTokenResponse>

    @GET("events/checkins")
    fun getScannableEvents(@Header("Authorization") token: String): Call<EventsResponse>

    // TODO - Isn't need any more
//    @GET("events/{event_id}/dashboard")
//    fun getDashboardForEvent(@Header("Authorization") token: String, @Path("event_id") eventId: String): Call<DashboardResponse>

    // TODO - Replace getTicketsForEvent() when BE team fix query parameter. It doesn't supposed to be mandatory
//    @GET("events/{eventId}/guests")
//    fun getTicketsForEvent(@Header("Authorization") token: String, @Path("eventId") eventId: String, @Query("query") query: String?): Call<TicketsResponse>

    @GET("events/{event_id}/guests?query=")
    fun getTicketsForEvent(@Header("Authorization") token: String, @Path("event_id") eventId: String, @Query("query") query: String?): Call<TicketsResponse>

    @POST("events/{event_id}/redeem/{ticket_id}")
    fun redeemTicketForEvent(@Header("Authorization") token: String, @Path("event_id") eventId: String, @Path("ticket_id") ticketId: String, @Body redeemRequest: RedeemRequest): Call<RedeemResponse>

    // TODO - Isn't need any more
//    @GET("tickets/{ticket_id}")
//    fun getTicket(@Header("Authorization") token: String, @Path("ticket_id") ticketId: String): Call<TicketResponse>
}