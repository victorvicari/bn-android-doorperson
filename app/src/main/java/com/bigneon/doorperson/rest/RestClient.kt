package com.bigneon.doorperson.rest

import com.bigneon.doorperson.rest.model.AuthRequest
import com.bigneon.doorperson.rest.model.AuthToken
import com.bigneon.doorperson.rest.model.EventsResponse
import com.bigneon.doorperson.rest.model.RefreshTokenRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.03.2019..
 ****************************************************/
interface RestClient {
    @POST("auth/token")
    fun authenticate(@Body authRequest: AuthRequest): Call<AuthToken>

    @POST("auth/token/refresh")
    fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Call<AuthToken>

    @GET("events/checkins")
    fun getScannableEvents(@Header("Authorization") token : String) : Call<EventsResponse>

//    @Headers("Content-Type: application/json;charset=UTF-8")
//    @GET("tickets")
//    fun getTickets(@Header("Authorization") authHeader: String): Call<List<Ticket>>
}