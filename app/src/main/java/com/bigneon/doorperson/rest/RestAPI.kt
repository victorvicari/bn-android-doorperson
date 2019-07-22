package com.bigneon.doorperson.rest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bigneon.doorperson.activity.DuplicateTicketCheckinActivity
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
import com.bigneon.doorperson.rest.response.TicketsResponse
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
    private val client: RestClient

    private object Loader {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        internal var INSTANCE = RestAPI()
    }

    init {
        val interceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val builder = originalRequest.newBuilder()
            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor()
        // set your desired log level
        logging.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(logging)
            .addInterceptor(interceptor)
            .build()

        val builder = Retrofit.Builder()
        builder.baseUrl(BASE_URL)
        builder.client(okHttpClient)
        builder.addConverterFactory(GsonConverterFactory.create())
        val retrofit = builder.build()
        client = retrofit.create(RestClient::class.java)
    }

    companion object {
        private val TAG = RestAPI::class.java.simpleName

        private fun client(): RestClient {
            return Loader.INSTANCE.client
        }

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun setContext(con: Context) {
            context = con
        }

        fun authenticate(email: String, password: String, setAccessToken: (accessToken: String?) -> Unit) {
            val authRequest = AuthRequest()
            authRequest.email = email
            authRequest.password = password

            val authTokenCall = client().authenticate(authRequest)

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
                val refreshTokenCall = client().refreshToken(refreshTokenRequest)

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

        // Synchronous call
        fun accessToken(): String? {
            if (isNetworkAvailable(context)) {
                val refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN) ?: ""
                if (refreshToken == "") return null

                val refreshTokenRequest = RefreshTokenRequest()
                refreshTokenRequest.refreshToken = refreshToken
                val refreshTokenCall = client().refreshToken(refreshTokenRequest)

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

//        fun getScannableEvents(accessToken: String, setEvents: (ArrayList<EventModel>?) -> Unit) {
//            val getScannableEventsCall = client().getScannableEvents(accessToken)
//            val getScannableEventsCallback = object : Callback<EventsResponse> {
//                override fun onResponse(call: Call<EventsResponse>, response: Response<EventsResponse>) {
//                    if (response.body() != null) {
//                        setEvents(response.body()!!.data)
//                    } else {
//                        Log.e(TAG, "Getting scannable events failed")
//                    }
//                }
//
//                override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
//                    Log.e(TAG, "Getting scannable events failed")
//                }
//            }
//
//            getScannableEventsCall.enqueue(getScannableEventsCallback)
//        }

        // Synchronous call
        fun getScannableEvents(accessToken: String): ArrayList<EventModel>? {
            val getScannableEventsCall = client().getScannableEvents(accessToken)
            return getScannableEventsCall.execute().body()?.data
        }

        fun getTicketsForEvent(
            accessToken: String,
            eventId: String,
            changesSince: String?,
            limit: Int?,
            page: Int?,
            setTickets: (ArrayList<TicketModel>?) -> Unit
        ) {
            val getTicketsForEventCall =
                client().getTicketsForEvent(accessToken, eventId, changesSince, limit, page, null)
            val getTicketsForEventCallback = object : Callback<TicketsResponse> {
                override fun onResponse(call: Call<TicketsResponse>, response: Response<TicketsResponse>) {
                    if (response.body() != null) {
                        setTickets(response.body()?.data)
                    } else {
                        Log.e(TAG, "Getting tickets for event $eventId failed")
                    }
                }

                override fun onFailure(call: Call<TicketsResponse>, t: Throwable) {
                    Log.e(TAG, "Getting tickets for event $eventId failed")
                }
            }

            getTicketsForEventCall.enqueue(getTicketsForEventCallback)
        }

        // Synchronous call
        fun getTicketsForEvent(
            accessToken: String,
            eventId: String,
            changesSince: String?,
            limit: Int?,
            filter: String?,
            page: Int?
        ): ArrayList<TicketModel>? {
            val getTicketsForEventCall =
                client().getTicketsForEvent(accessToken, eventId, changesSince, limit, page, filter)
            return getTicketsForEventCall.execute().body()?.data
        }

//        fun getTotalNumberOfTicketsForEvent(
//            accessToken: String,
//            eventId: String,
//            setTotalNumberOfTickets: (Int?) -> Unit
//        ) {
//            val getTicketsForEventCall =
//                client().getTicketsForEvent(accessToken, eventId, MIN_TIMESTAMP, 1, 0, null)
//            val getTicketsForEventCallback = object : Callback<TicketsResponse> {
//                override fun onResponse(call: Call<TicketsResponse>, response: Response<TicketsResponse>) {
//                    if (response.body() != null) {
//                        setTotalNumberOfTickets(response.body()?.paging?.total)
//                    } else {
//                        Log.e(TAG, "Getting total number of tickets for event $eventId failed")
//                    }
//                }
//
//                override fun onFailure(call: Call<TicketsResponse>, t: Throwable) {
//                    setTotalNumberOfTickets(-1)
//                    Log.e(TAG, "Getting total number of tickets for event $eventId failed")
//                }
//            }
//
//            getTicketsForEventCall.enqueue(getTicketsForEventCallback)
//        }

        // Synchronous call
//        fun getTotalNumberOfTicketsForEvent(
//            accessToken: String,
//            eventId: String
//        ): Int? {
//            val getTicketsForEventCall =
//                client().getTicketsForEvent(accessToken, eventId, MIN_TIMESTAMP, 1, 0, null)
//
//            return getTicketsForEventCall.execute().body()?.paging?.total
//        }

        fun redeemTicketForEvent(
            accessToken: String,
            eventId: String,
            ticketId: String,
            firstName: String,
            lastName: String,
            redeemKey: String,
            redeemTicketResult: ((isDuplicateTicket: Boolean) -> Unit)?
        ) {
            try {
                val redeemRequest = RedeemRequest()
                redeemRequest.redeemKey = redeemKey
                val redeemTicketForEventCall = client()
                    .redeemTicketForEvent(accessToken, eventId, ticketId, redeemRequest)
                val callbackRedeemTicketForEvent = object : Callback<TicketModel> {
                    override fun onResponse(call: Call<TicketModel>, response: Response<TicketModel>) {
                        if (response.body() != null) {
                            Log.e(TAG, "Redeem ticket for event $eventId succeeded")
                            redeemTicketResult?.invoke(false)
                        } else {
                            if (response.code() == 409) {
                                val getTicketCall = client().getTicket(accessToken, ticketId)
                                val getTicketCallResponse = getTicketCall.execute()
                                if (getTicketCallResponse.body() != null) {
                                    val ticketStatus = getTicketCallResponse.body()!!.ticket?.status?.toLowerCase()
                                    val statusRedeemed =
                                        context.getString(com.bigneon.doorperson.R.string.redeemed).toLowerCase()
                                    if (ticketStatus == statusRedeemed) {
                                        val intent = Intent(context, DuplicateTicketCheckinActivity::class.java)
                                        intent.putExtra("ticketId", ticketId)
                                        intent.putExtra("lastAndFirstName", "$lastName, $firstName")
                                        context.startActivity(intent)
                                    }
                                }
                                redeemTicketResult?.invoke(true)
                            } else {
                                redeemTicketResult?.invoke(false)
                            }
                        }
                    }

                    override fun onFailure(call: Call<TicketModel>, t: Throwable) {
                        Log.e(TAG, "Redeem ticket for event $eventId failed")
                        redeemTicketResult?.invoke(false)
                    }
                }
                redeemTicketForEventCall.enqueue(callbackRedeemTicketForEvent)
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
        }

        // Synchronous call
        fun redeemTicketForEvent(
            accessToken: String,
            eventId: String,
            ticketId: String,
            firstName: String?,
            lastName: String?,
            redeemKey: String
        ): Boolean? {
            try {
                val redeemRequest = RedeemRequest()
                redeemRequest.redeemKey = redeemKey
                val redeemTicketForEventCall = client()
                    .redeemTicketForEvent(accessToken, eventId, ticketId, redeemRequest)

                val response = redeemTicketForEventCall.execute()
                return if (response?.body() != null) {
                    Log.e(TAG, "Redeem ticket for event $eventId succeeded")
                    false
                } else
                    return if (response.code() == 409) {
                        val getTicketCall = client().getTicket(accessToken, ticketId)
                        val getTicketCallResponse = getTicketCall.execute()
                        if (getTicketCallResponse.body() != null) {
                            val ticketStatus = getTicketCallResponse.body()!!.ticket?.status?.toLowerCase()
                            val statusRedeemed =
                                context.getString(com.bigneon.doorperson.R.string.redeemed).toLowerCase()
                            if (ticketStatus == statusRedeemed) {
                                val intent = Intent(context, DuplicateTicketCheckinActivity::class.java)
                                intent.putExtra("ticketId", ticketId)
                                intent.putExtra("lastAndFirstName", "$lastName, $firstName")
                                context.startActivity(intent)
                            }
                        }
                        Log.e(TAG, "Redeem ticket for event $eventId failed")
                        true
                    } else {
                        Log.e(TAG, "Redeem ticket for event $eventId succeeded")
                        false
                    }
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
            return null
        }

//        fun getEvent(
//            accessToken: String,
//            eventId: String,
//            getEvent: (EventModel?) -> Unit
//        ) {
//            val getEventCall = client().getEvent(accessToken, eventId)
//
//            val getEventCallback = object : Callback<EventModel> {
//                override fun onResponse(call: Call<EventModel>, response: Response<EventModel>) {
//                    if (response.body() != null) {
//                        getEvent(response.body())
//                    } else {
//                        getEvent(null)
//                    }
//                }
//
//                override fun onFailure(call: Call<EventModel>, t: Throwable) {
//                    getEvent(null)
//                }
//            }
//            getEventCall.enqueue(getEventCallback)
//        }

        // Synchronous call
        fun getEvent(
            accessToken: String,
            eventId: String
        ): EventModel? {
            val getEventCall = client().getEvent(accessToken, eventId)
            return getEventCall.execute().body()
        }

        // Synchronous call
        fun getEventDashboard(
            accessToken: String,
            eventId: String
        ): EventDashboardModel? {
            val getEventDashboardCall = client().getEventDashboard(accessToken, eventId)
            return getEventDashboardCall.execute().body()?.event
        }

//        fun getTicket(
//            accessToken: String,
//            ticketId: String,
//            getTicketResult: (isRedeemed: Boolean, ticket: TicketModel?) -> Unit
//        ) {
//            try {
//                val getTicketCall = client().getTicket(accessToken, ticketId)
//                val getTicketCallback = object : Callback<TicketResponse> {
//                    override fun onResponse(call: Call<TicketResponse>, response: Response<TicketResponse>) {
//                        if (response.body() != null) {
//                            val ticket = response.body()!!.ticket!!
//                            ticket.userId = response.body()!!.user?.userId ?: ""
//                            ticket.firstName = response.body()!!.user?.firstName ?: ""
//                            ticket.lastName = response.body()!!.user?.lastName ?: ""
//                            ticket.email = response.body()!!.user?.email ?: ""
//                            ticket.phone = response.body()!!.user?.phone ?: ""
//                            ticket.profilePicURL = response.body()!!.user?.profilePicURL ?: ""
//                            ticket.eventId = response.body()!!.event?.id ?: ""
//                            getTicketResult(true, ticket)
//                            Log.e(TAG, "Redeem ticket $ticketId succeeded")
//                        } else {
//                            getTicketResult(false, null)
//                            Log.e(TAG, "Redeem ticket $ticketId failed")
//                        }
//                    }
//
//                    override fun onFailure(call: Call<TicketResponse>, t: Throwable) {
//                        getTicketResult(false, null)
//                        Log.e(TAG, "Redeem ticket $ticketId failed")
//                    }
//                }
//                getTicketCall.enqueue(getTicketCallback)
//            } catch (e: Exception) {
//                Log.e(TAG, e.message)
//            }
//        }

        // Synchronous call
        fun getTicket(
            accessToken: String,
            ticketId: String
        ): TicketModel? {
            try {
                val getTicketCall = client().getTicket(accessToken, ticketId)
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
                Log.e(TAG, e.message)
            }
            return null
        }
    }
}