//package com.bigneon.doorperson.rest
//
//import android.content.Context
//import android.content.Intent
//import android.support.design.widget.Snackbar
//import android.support.v7.widget.LinearLayoutManager
//import android.support.v7.widget.RecyclerView
//import android.util.Log
//import android.view.View
//import com.bigneon.doorperson.R
//import com.bigneon.doorperson.activity.EventsActivity
//import com.bigneon.doorperson.activity.GuestActivity
//import com.bigneon.doorperson.activity.LoginActivity
//import com.bigneon.doorperson.activity.ScanningEventActivity
//import com.bigneon.doorperson.adapter.EventListAdapter
//import com.bigneon.doorperson.adapter.OnItemClickListener
//import com.bigneon.doorperson.adapter.addOnItemClickListener
//import com.bigneon.doorperson.auth.AppAuth
//import com.bigneon.doorperson.config.AppConstants
//import com.bigneon.doorperson.config.AppConstants.Companion.BASE_URL
//import com.bigneon.doorperson.config.SharedPrefs
//import com.bigneon.doorperson.rest.model.GuestModel
//import com.bigneon.doorperson.rest.request.AuthRequest
//import com.bigneon.doorperson.rest.request.RedeemRequest
//import com.bigneon.doorperson.rest.request.RefreshTokenRequest
//import com.bigneon.doorperson.rest.response.*
//import kotlinx.android.synthetic.main.content_guest.view.*
//import kotlinx.android.synthetic.main.content_login.view.*
//import kotlinx.android.synthetic.main.content_scanning_event.view.*
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
///****************************************************
// * Copyright (c) 2016 - 2019.
// * All right reserved!
// * Created by SRKI-ST on 20.03.2019..
// ****************************************************/
//class RestAPI private constructor() {
//    private val client: RestClient
//
//    private object Loader {
//        @Volatile
//        internal var INSTANCE = RestAPI()
//    }
//
//    init {
//        val interceptor = object : Interceptor {
//            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
//                val originalRequest = chain.request()
//                val builder = originalRequest.newBuilder()
//                return chain.proceed(builder.build())
//            }
//        }
//
//        val logging = HttpLoggingInterceptor()
//        // set your desired log level
//        logging.level = HttpLoggingInterceptor.Level.BODY
//
//        val okHttpClient = OkHttpClient().newBuilder()
//            .addInterceptor(logging)
//            .addInterceptor(interceptor)
//            .build()
//
//        val builder = Retrofit.Builder()
//        builder.baseUrl(BASE_URL)
//        builder.client(okHttpClient)
//        builder.addConverterFactory(GsonConverterFactory.create())
//        val retrofit = builder.build()
//        client = retrofit.create<RestClient>(RestClient::class.java)
//    }
//
//    companion object {
//        private val TAG = RestAPI::class.java.simpleName
//
//        private fun client(): RestClient {
//            return Loader.INSTANCE.client
//        }
//
//        fun authenticate(context: Context, view: View) {
//            val authRequest = AuthRequest()
//            authRequest.email = if (view.email_address.text != null) view.email_address.text.toString() else ""
//            authRequest.password = if (view.password.text != null) view.password.text.toString() else ""
//
//            val authTokenCall = client().authenticate(authRequest)
//
//            val callbackAuthToken = object : Callback<AuthTokenResponse> {
//                override fun onResponse(call: Call<AuthTokenResponse>, response: Response<AuthTokenResponse>) {
//                    if (response.body() != null) {
//                        SharedPrefs.setProperty(
//                            AppConstants.ACCESS_TOKEN,
//                            response.body()!!.accessToken.orEmpty()
//                        )
//                        SharedPrefs.setProperty(
//                            AppConstants.REFRESH_TOKEN,
//                            response.body()!!.refreshToken.orEmpty()
//                        )
//
//                        context.startActivity(Intent(context, EventsActivity::class.java))
//                    } else {
//                        Snackbar
//                            .make(view, "Username and/or password does not match!", Snackbar.LENGTH_LONG)
//                            .setDuration(5000).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<AuthTokenResponse>, t: Throwable) {
//                    Snackbar
//                        .make(view, "Authentication error!", Snackbar.LENGTH_LONG)
//                        .setAction("RETRY") { authenticate(context, view) }.setDuration(5000).show()
//                }
//            }
//
//            authTokenCall.enqueue(callbackAuthToken)
//        }
//
//        fun refreshToken(context: Context, view: View) {
//            try {
//                val refreshTokenRequest = RefreshTokenRequest()
//                refreshTokenRequest.refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN)
//                val refreshTokenCall = client().refreshToken(refreshTokenRequest)
//
//                val callbackRefreshToken = object : Callback<AuthTokenResponse> {
//                    override fun onResponse(call: Call<AuthTokenResponse>, response: Response<AuthTokenResponse>) {
//                        if (response.body() != null) {
//                            SharedPrefs.setProperty(
//                                AppConstants.ACCESS_TOKEN,
//                                response.body()!!.accessToken.orEmpty()
//                            )
//                            SharedPrefs.setProperty(
//                                AppConstants.REFRESH_TOKEN,
//                                response.body()!!.refreshToken.orEmpty()
//                            )
//
//                            context.startActivity(Intent(context, EventsActivity::class.java))
//                        } else {
//                            Snackbar
//                                .make(view, "Refresh token is not valid!", Snackbar.LENGTH_LONG)
//                                .setDuration(5000).show()
//                            Log.e(TAG, "MSG:" + response.message() + ", CODE: " + response.code())
//                        }
//                    }
//
//                    override fun onFailure(call: Call<AuthTokenResponse>, t: Throwable) {
//                        Snackbar
//                            .make(view, "Authentication error!", Snackbar.LENGTH_LONG)
//                            .setAction(
//                                "RETRY"
//                            ) { context.startActivity(Intent(context, LoginActivity::class.java)) }.setDuration(5000)
//                            .show()
//                        Log.e(TAG, "Failure MSG:" + t.message)
//                    }
//                }
//
//                refreshTokenCall.enqueue(callbackRefreshToken)
//            } catch (e: Exception) {
//                Log.e(TAG, e.message)
//            }
//        }
//
//        fun getDashboardForEvent(context: Context, view: View, eventId: String) {
//            val getDashboardForEventCall =
//                client().getDashboardForEvent(AppAuth.getAccessToken(context), eventId)
//            val callbackGetDashboardForEvent = object : Callback<DashboardResponse> {
//                override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
//                    if (response.code() == 401) { //Unauthorized
//                        refreshToken(context, view)
//                    } else {
//                        val ticketsRedeemed = response.body()!!.event?.ticketsRedeemed ?: 0
//                        val soldHeld = response.body()!!.event?.soldHeld ?: 0
//                        val soldUnreserved = response.body()!!.event?.soldUnreserved ?: 0
//                        view.number_of_redeemed.text =
//                            context.getString(R.string._1_d_of_2_d_redeemed, ticketsRedeemed, soldHeld + soldUnreserved)
//                    }
//                }
//
//                override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
//                    refreshToken(context, view)
//                }
//            }
//            getDashboardForEventCall.enqueue(callbackGetDashboardForEvent)
//        }
//
//        fun getScannableEvents(context: Context, view: View) {
//            try {
//                val getScannableEventsCall = client().getScannableEvents(AppAuth.getAccessToken(context))
//                val callbackGetScannableEvents = object : Callback<EventsResponse> {
//                    override fun onResponse(call: Call<EventsResponse>, response: Response<EventsResponse>) {
//                        if (response.code() == 401) { //Unauthorized
//                            refreshToken(context, view)
//                        } else {
//                            val eventsListView: RecyclerView =
//                                view.findViewById(com.bigneon.doorperson.R.id.events_list_view)
//                            val eventList = response.body()!!.data
//
//                            eventsListView.layoutManager =
//                                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//
//                            eventsListView.adapter = EventListAdapter(eventList!!)
//
//                            eventsListView.addOnItemClickListener(object : OnItemClickListener {
//                                override fun onItemClicked(position: Int, view: View) {
//                                    val eventId = eventList[position].id
//                                    val intent = Intent(context, ScanningEventActivity::class.java)
//                                    intent.putExtra("eventId", eventId)
//                                    context.startActivity(intent)
//                                }
//                            })
//
//                            Log.d(TAG, "SUCCESS")
//                        }
//                    }
//
//                    override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
//                        refreshToken(context, view)
//                    }
//                }
//                getScannableEventsCall.enqueue(callbackGetScannableEvents)
//            } catch (e: Exception) {
//                Log.e(TAG, e.message)
//            }
//        }
//
//        fun getTicketsForEvent(
//            context: Context,
//            view: View,
//            eventId: String,
//            populateGuestList: (guestList: ArrayList<GuestModel>?) -> Unit
//        ) {
//            val getGuestsForEventCall =
//                client().getTicketsForEvent(AppAuth.getAccessToken(context), eventId, null)
//            val callbackGetScannableEvents = object : Callback<TicketsResponse> {
//                override fun onResponse(call: Call<TicketsResponse>, response: Response<TicketsResponse>) {
//                    if (response.code() == 401) { //Unauthorized
//                        refreshToken(context, view)
//                    } else {
//                        populateGuestList(response.body()!!.data)
//                    }
//                }
//
//                override fun onFailure(call: Call<TicketsResponse>, t: Throwable) {
//                    refreshToken(context, view)
//                }
//            }
//            getGuestsForEventCall.enqueue(callbackGetScannableEvents)
//        }
//
//        fun getTicket(context: Context, view: View, ticketId: String) {
//            try {
//                val getTicketCall = client().getTicket(AppAuth.getAccessToken(context), ticketId)
//                val callbackGetTicket = object : Callback<TicketResponse> {
//                    override fun onResponse(call: Call<TicketResponse>, response: Response<TicketResponse>) {
//                        if (response.code() == 401) { //Unauthorized
//                            refreshToken(context, view)
//                        } else {
//                            val ticketResponse = response.body()
//
//                            if (ticketResponse != null) {
//                                val intent = Intent(context, GuestActivity::class.java)
//                                intent.putExtra("id", ticketResponse.ticket?.id)
//                                intent.putExtra("eventId", ticketResponse.event?.id)
//                                intent.putExtra("redeemKey", ticketResponse.ticket?.redeemKey)
//                                intent.putExtra("searchGuestText", "")
//                                intent.putExtra("firstName", ticketResponse.user?.firstName)
//                                intent.putExtra("lastName", ticketResponse.user?.lastName)
//                                intent.putExtra("priceInCents", ticketResponse.ticket?.priceInCents)
//                                intent.putExtra("ticketTypeName", ticketResponse.ticket?.ticketTypeName)
//                                intent.putExtra("status", ticketResponse.ticket?.status)
//                                intent.putExtra("position", -1)
//                                context.startActivity(intent)
//                            } else {
//
//                            }
//
//                            Log.d(TAG, "SUCCESS")
//                        }
//                    }
//
//                    override fun onFailure(call: Call<TicketResponse>, t: Throwable) {
//
//                    }
//                }
//                getTicketCall.enqueue(callbackGetTicket)
//            } catch (e: Exception) {
//                Log.e(TAG, e.message)
//            }
//        }
//
//        fun redeemTicketForEvent(context: Context, view: View, eventId: String, ticketId: String, redeemKey: String) {
//            try {
//                val redeemRequest = RedeemRequest()
//                redeemRequest.redeemKey = redeemKey
//                val redeemTicketForEventCall = client()
//                    .redeemTicketForEvent(AppAuth.getAccessToken(context), eventId, ticketId, redeemRequest)
//                val callbackRedeemTicketForEvent = object : Callback<RedeemResponse> {
//                    override fun onResponse(call: Call<RedeemResponse>, response: Response<RedeemResponse>) {
//                        if (response.code() == 401) { //Unauthorized
//                            refreshToken(context, view)
//                        } else {
//                            val redeemResponse = response.body()
//
//                            if (redeemResponse != null) {
//                                view.redeemed_status?.visibility = View.VISIBLE
//                                view.purchased_status?.visibility = View.GONE
//                                view.complete_check_in?.visibility = View.GONE
//
//                                Snackbar
//                                    .make(
//                                        view,
//                                        "Checked in ${redeemResponse.lastName + ", " + redeemResponse.firstName}",
//                                        Snackbar.LENGTH_LONG
//                                    )
//                                    .setDuration(5000).show()
//                            } else {
//                                Snackbar
//                                    .make(
//                                        view,
//                                        "User ticket already redeemed! Redeem key: $redeemKey",
//                                        Snackbar.LENGTH_LONG
//                                    )
//                                    .setDuration(5000).show()
//                            }
//
//                            Log.d(TAG, "SUCCESS")
//                        }
//                    }
//
//                    override fun onFailure(call: Call<RedeemResponse>, t: Throwable) {
//                        refreshToken(context, view)
//                    }
//                }
//                redeemTicketForEventCall.enqueue(callbackRedeemTicketForEvent)
//            } catch (e: Exception) {
//                Log.e(TAG, e.message)
//            }
//        }
//    }
//}