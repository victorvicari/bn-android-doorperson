package com.bigneon.doorperson.rest

import com.bigneon.doorperson.rest.request.AuthRequest
import com.bigneon.doorperson.rest.request.RefreshTokenRequest
import com.bigneon.doorperson.rest.response.AuthTokenResponse
import com.bigneon.doorperson.rest.response.DashboardResponse
import com.bigneon.doorperson.rest.response.EventsResponse
import com.bigneon.doorperson.rest.response.GuestsResponse
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

    @GET("events/{eventId}/dashboard")
    fun getDashboardForEvent(@Header("Authorization") token: String, @Path("eventId") eventId: String): Call<DashboardResponse>


//    @GET("events/{eventId}/guests")
//    fun getGuestsForEvent(@Header("Authorization") token: String, @Path("eventId") eventId: String, @Query("query") query: String?): Call<GuestsResponse>

    @GET("events/{eventId}/guests?query=")
    fun getGuestsForEvent(@Header("Authorization") token: String, @Path("eventId") eventId: String, @Query("query") query: String?): Call<GuestsResponse>

//    @Headers("Content-Type: application/json;charset=UTF-8")
//    @GET("tickets")
//    fun getTickets(@Header("Authorization") authHeader: String): Call<List<Ticket>>
}