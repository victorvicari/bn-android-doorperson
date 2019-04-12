package com.bigneon.doorperson.rest

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.auth0.jwt.JWT
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.AppConstants.Companion.BASE_URL
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.rest.model.EventModel
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.rest.request.AuthRequest
import com.bigneon.doorperson.rest.request.RefreshTokenRequest
import com.bigneon.doorperson.rest.response.AuthTokenResponse
import com.bigneon.doorperson.rest.response.EventsResponse
import com.bigneon.doorperson.rest.response.TicketsResponse
import com.bigneon.doorperson.util.NetworkUtils
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
        val interceptor = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val originalRequest = chain.request()
                val builder = originalRequest.newBuilder()
                return chain.proceed(builder.build())
            }
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
        client = retrofit.create<RestClient>(RestClient::class.java)
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
            if (NetworkUtils.instance().isNetworkAvailable(context)) {
                val refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN) ?: ""
                if (refreshToken == "") setAccessToken(null)

                val refreshTokenRequest = RefreshTokenRequest()
                refreshTokenRequest.refreshToken = refreshToken
                val refreshTokenCall = client().refreshToken(refreshTokenRequest)

                val refreshTokenCallback = object : Callback<AuthTokenResponse> {
                    override fun onResponse(call: Call<AuthTokenResponse>, response: Response<AuthTokenResponse>) {
                        if (response.body() != null) {
                            SharedPrefs.setProperty(AppConstants.REFRESH_TOKEN, response.body()?.refreshToken)
                            val accessToken = response.body()?.accessToken
                            val jwt = JWT.decode(accessToken)
                            //TODO store this expires at and re-use this access token unless it has expired. Then call refresh.
                            //println(jwt.expiresAt)
                            SharedPrefs.setProperty(AppConstants.ACCESS_TOKEN, accessToken)
                            setAccessToken("Bearer " + accessToken)
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

        fun getScannableEvents(accessToken: String, setEvents: (ArrayList<EventModel>?) -> Unit) {
            val getScannableEventsCall = client().getScannableEvents(accessToken)
            val getScannableEventsCallback = object : Callback<EventsResponse> {
                override fun onResponse(call: Call<EventsResponse>, response: Response<EventsResponse>) {
                    if (response.body() != null) {
                        setEvents(response.body()!!.data)
                    } else {
                        Log.e(TAG, "Getting scannable events failed")
                    }
                }

                override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
                    Log.e(TAG, "Getting scannable events failed")
                }
            }

            getScannableEventsCall.enqueue(getScannableEventsCallback)
        }

        fun getTicketsForEvent(accessToken: String, eventId: String, setTickets: (ArrayList<TicketModel>?) -> Unit) {
            val getTicketsForEventCall =
                client().getTicketsForEvent(accessToken, eventId, null)
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


/*


        fun getTicketOLD(context: Context, view: View, ticketId: String) {
            try {
                val getTicketCall = client().getTicket(AppAuth.getAccessToken(context), ticketId)
                val callbackGetTicket = object : Callback<TicketResponse> {
                    override fun onResponse(call: Call<TicketResponse>, response: Response<TicketResponse>) {
                        if (response.code() == 401) { //Unauthorized
                            refreshToken(context, view)
                        } else {
                            val ticketResponse = response.body()

                            if (ticketResponse != null) {
                                val intent = Intent(context, TicketActivity::class.java)
                                intent.putExtra("id", ticketResponse.ticket?.id)
                                intent.putExtra("eventId", ticketResponse.event?.id)
                                intent.putExtra("redeemKey", ticketResponse.ticket?.redeemKey)
                                intent.putExtra("searchGuestText", "")
                                intent.putExtra("firstName", ticketResponse.user?.firstName)
                                intent.putExtra("lastName", ticketResponse.user?.lastName)
                                intent.putExtra("priceInCents", ticketResponse.ticket?.priceInCents)
                                intent.putExtra("ticketTypeName", ticketResponse.ticket?.ticketTypeName)
                                intent.putExtra("status", ticketResponse.ticket?.status)
                                intent.putExtra("position", -1)
                                context.startActivity(intent)
                            } else {

                            }

                            Log.d(TAG, "SUCCESS")
                        }
                    }

                    override fun onFailure(call: Call<TicketResponse>, t: Throwable) {

                    }
                }
                getTicketCall.enqueue(callbackGetTicket)
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
        }

        */
    }
}