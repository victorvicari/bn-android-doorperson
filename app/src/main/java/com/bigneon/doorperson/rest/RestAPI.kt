package com.bigneon.doorperson.rest

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.AppConstants.Companion.BASE_URL
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.rest.model.EventDashboardModel
import com.bigneon.doorperson.rest.model.EventModel
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.rest.request.AuthRequest
import com.bigneon.doorperson.rest.request.RedeemRequest
import com.bigneon.doorperson.rest.request.RefreshTokenRequest
import com.bigneon.doorperson.rest.response.AuthTokenResponse
import com.bigneon.doorperson.rest.response.UserInfoResponse
import com.bigneon.doorperson.util.NetworkUtils.Companion.isNetworkAvailable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.03.2019..
 ****************************************************/
class RestAPI private constructor() {
    companion object {
        private val TAG = RestAPI::class.java.simpleName
        private lateinit var client: RestClient

        fun setBaseURL() {
            val interceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val builder = originalRequest.newBuilder()
                chain.proceed(builder.build())
            }

            val logging = HttpLoggingInterceptor()

            logging.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClient = OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .addInterceptor(interceptor)
                .build()

            val builder = Retrofit.Builder()
            var baseURL = SharedPrefs.getProperty("BASE_URL")
            if (baseURL.isNullOrBlank()) {
                baseURL = BASE_URL
            }
            builder.baseUrl(baseURL)
            builder.client(okHttpClient)
            builder.addConverterFactory(GsonConverterFactory.create())
            val retrofit = builder.build()
            client = retrofit.create(RestClient::class.java)
        }

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun setContext(con: Context) {
            context = con
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Asynchronous calls                                                                                         //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        fun authenticate(email: String, password: String, setAccessToken: (accessToken: String?) -> Unit) {
            val authRequest = AuthRequest()
            authRequest.email = email
            authRequest.password = password

            val authTokenCall = client.authenticate(authRequest)

            val authTokenCallback = object : Callback<AuthTokenResponse> {
                override fun onResponse(call: Call<AuthTokenResponse>, response: Response<AuthTokenResponse>) {
                    if (response.body() != null) {
                        SharedPrefs.setProperty(AppConstants.REFRESH_TOKEN, response.body()?.refreshToken)
                        setAccessToken("Bearer " + response.body()?.accessToken)
                    } else {
                        setAccessToken(null)
                    }
                }

                override fun onFailure(call: Call<AuthTokenResponse>, t: Throwable) {
                    setAccessToken(null)
                    Log.e(TAG, "Authentication failed for $email")
                }
            }

            authTokenCall.enqueue(authTokenCallback)
        }

        fun accessToken(setAccessToken: (accessToken: String?) -> Unit) {
            if (isNetworkAvailable(context)) {
                val refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN) ?: ""
                if (refreshToken == "") setAccessToken(null)

                val refreshTokenRequest = RefreshTokenRequest()
                refreshTokenRequest.refreshToken = refreshToken
                val refreshTokenCall = client.refreshToken(refreshTokenRequest)

                val refreshTokenCallback = object : Callback<AuthTokenResponse> {
                    override fun onResponse(call: Call<AuthTokenResponse>, response: Response<AuthTokenResponse>) {
                        if (response.body() != null) {
                            SharedPrefs.setProperty(AppConstants.REFRESH_TOKEN, response.body()?.refreshToken)
                            SharedPrefs.setProperty(AppConstants.ACCESS_TOKEN, response.body()?.accessToken)
                            setAccessToken("Bearer " + response.body()?.accessToken)
                        } else {
                            setAccessToken(null)
                        }
                    }

                    override fun onFailure(call: Call<AuthTokenResponse>, t: Throwable) {
                        setAccessToken(null)
                        Log.e(TAG, "Refresh token failed")
                    }
                }

                refreshTokenCall.enqueue(refreshTokenCallback)
            } else { // Not connected
                val accessToken = SharedPrefs.getProperty(AppConstants.ACCESS_TOKEN) ?: ""
                if (accessToken == "")
                    setAccessToken(null)
                else
                    setAccessToken("Bearer $accessToken")
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Synchronous calls                                                                                          //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        fun accessToken(): String? {
            if (isNetworkAvailable(context)) {
                val refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN) ?: ""
                if (refreshToken == "") return null

                val refreshTokenRequest = RefreshTokenRequest()
                refreshTokenRequest.refreshToken = refreshToken
                val refreshTokenCall = client.refreshToken(refreshTokenRequest)

                val accessToken = refreshTokenCall.execute().body()
                return if (accessToken != null) {
                    SharedPrefs.setProperty(AppConstants.REFRESH_TOKEN, accessToken.refreshToken)
                    SharedPrefs.setProperty(AppConstants.ACCESS_TOKEN, accessToken.accessToken)
                    "Bearer " + accessToken.accessToken
                } else
                    null
            } else { // Not connected
                val accessToken = SharedPrefs.getProperty(AppConstants.ACCESS_TOKEN) ?: ""
                return if (accessToken == "")
                    null
                else
                    "Bearer $accessToken"
            }
        }

        fun getScannableEvents(accessToken: String): ArrayList<EventModel>? {
            val getScannableEventsCall = client.getScannableEvents(accessToken)
            return getScannableEventsCall.execute().body()?.data
        }

        fun getTicketsForEvent(
            accessToken: String,
            eventId: String,
            changesSince: String?,
            limit: Int?,
            filter: String?,
            page: Int?
        ): ArrayList<TicketModel>? {
            val getTicketsForEventCall =
                client.getTicketsForEvent(accessToken, eventId, changesSince, limit, page, filter)
            return getTicketsForEventCall.execute().body()?.data
        }

        fun redeemTicketForEvent(
            accessToken: String,
            eventId: String,
            ticketId: String,
            redeemKey: String
        ): TicketModel? {
            try {
                val redeemRequest = RedeemRequest()
                redeemRequest.redeemKey = redeemKey
                val redeemTicketForEventCall = client
                    .redeemTicketForEvent(accessToken, eventId, ticketId, redeemRequest)

                val response = redeemTicketForEventCall.execute()
                return if (response?.body() != null) {
                    Log.e(TAG, "Redeem ticket for event $eventId succeeded")
                    response.body()
                } else
                    return if (response.code() == 409) {
                        Log.e(TAG, "Redeem ticket for event $eventId failed")
                        null
                    } else {
                        Log.e(TAG, "Redeem ticket for event $eventId succeeded")
                        response?.body()
                    }
            } catch (e: Exception) {
                Log.e(TAG, e.message!!)
            }
            return null
        }

        fun getEvent(
            accessToken: String,
            eventId: String
        ): EventModel? {
            val getEventCall = client.getEvent(accessToken, eventId)
            return getEventCall.execute().body()
        }

        fun getEventDashboard(
            accessToken: String,
            eventId: String
        ): EventDashboardModel? {
            val getEventDashboardCall = client.getEventDashboard(accessToken, eventId)
            return getEventDashboardCall.execute().body()?.event
        }

        fun getTicket(
            accessToken: String,
            ticketId: String
        ): TicketModel? {
            try {
                val getTicketCall = client.getTicket(accessToken, ticketId)
                val response = getTicketCall.execute()
                return if (response.body() != null) {
                    val ticket = response.body()!!.ticket!!
                    ticket.userId = response.body()!!.user?.userId ?: ""
                    ticket.firstName = response.body()!!.user?.firstName ?: ""
                    ticket.lastName = response.body()!!.user?.lastName ?: ""
                    ticket.email = response.body()!!.user?.email ?: ""
                    ticket.phone = response.body()!!.user?.phone ?: ""
                    ticket.profilePicURL = response.body()!!.user?.profilePicURL ?: ""
                    ticket.eventId = response.body()!!.event?.id ?: ""
                    Log.e(TAG, "Redeem ticket $ticketId succeeded")
                    ticket
                } else {
                    Log.e(TAG, "Redeem ticket $ticketId failed")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message!!)
            }
            return null
        }

        fun getUserInfo(
            accessToken: String
        ): UserInfoResponse? {
            try {
                val getUserInfoCall = client.getUserInfo(accessToken)
                val response = getUserInfoCall.execute()
                return if (response.body() != null) response.body() else {
                    Log.e(TAG, "getting user info failed")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message!!)
            }
            return null
        }
    }
}