//package com.bigneon.doorperson.rest
//
//import com.bigneon.doorperson.config.AppConstants
//import com.bigneon.doorperson.config.SharedPrefs
//import com.bigneon.doorperson.rest.model.EventModel
//import com.bigneon.doorperson.rest.model.TicketModel
//import com.bigneon.doorperson.rest.request.AuthRequest
//import com.bigneon.doorperson.rest.request.RedeemRequest
//import com.bigneon.doorperson.rest.request.RefreshTokenRequest
//import com.bigneon.doorperson.rest.response.AuthTokenResponse
//import com.bigneon.doorperson.rest.response.EventsResponse
//import com.bigneon.doorperson.rest.response.RedeemResponse
//import com.bigneon.doorperson.rest.response.TicketsResponse
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
///****************************************************
// * Copyright (c) 2016 - 2019.
// * All right reserved!
// * Created by SRKI-ST on 09.04.2019..
// ****************************************************/
//class RestAPISync private constructor() {
//    private val client: RestClient
//
//    private object Loader {
//        @Volatile
//        internal var INSTANCE = RestAPISync()
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
//        builder.baseUrl(AppConstants.BASE_URL)
//        builder.client(okHttpClient)
//        builder.addConverterFactory(GsonConverterFactory.create())
//        val retrofit = builder.build()
//        client = retrofit.create<RestClient>(RestClient::class.java)
//    }
//
//    companion object {
//        private val TAG = RestAPISync::class.java.simpleName
//
//        private fun client(): RestClient {
//            return Loader.INSTANCE.client
//        }
//
//        fun authenticate(email: String, password: String): String? {
//            val authRequest = AuthRequest()
//            authRequest.email = email
//            authRequest.password = password
//            val authTokenCall = RestAPISync.client().authenticate(authRequest)
//            val response: Response<AuthTokenResponse> = authTokenCall.execute()
//            SharedPrefs.setProperty(AppConstants.REFRESH_TOKEN, response.body()?.refreshToken)
//            return response.body()?.accessToken
//        }
//
//        fun accessToken(): String? {
//            val refreshToken = SharedPrefs.getProperty(AppConstants.REFRESH_TOKEN) ?: return null
//            if (refreshToken == "") return null
//            val refreshTokenRequest = RefreshTokenRequest()
//            refreshTokenRequest.refreshToken = refreshToken
//            val refreshTokenCall = RestAPISync.client().refreshToken(refreshTokenRequest)
//            val response: Response<AuthTokenResponse> = refreshTokenCall.execute()
//            return response.body()?.accessToken
//        }
//
////        private fun bearerAccessToken(): String {
////            return "Bearer " + (accessToken() ?: "")
////        }
//
//
//        fun getScannableEvents(accessToken : String): ArrayList<EventModel>? {
//            val getScannableEventsCall = RestAPISync.client().getScannableEvents(accessToken)
//            val response: Response<EventsResponse> = getScannableEventsCall.execute()
//            return response.body()?.data
//        }
//
//        fun getTicketsForEvent(eventId: String): ArrayList<TicketModel>? {
//            val getTicketsForEventCall =
//                RestAPISync.client().getTicketsForEvent(bearerAccessToken(), eventId, null)
//            val response: Response<TicketsResponse> = getTicketsForEventCall.execute()
//            return response.body()?.data
//        }
//
//        fun redeemTicketForEvent(eventId: String, ticketId: String, redeemKey: String): RedeemResponse? {
//            val redeemRequest = RedeemRequest()
//                redeemRequest.redeemKey = redeemKey
//            val redeemTicketForEventCall = client()
//                .redeemTicketForEvent(bearerAccessToken(), eventId, ticketId, redeemRequest)
//            val response: Response<RedeemResponse> = redeemTicketForEventCall.execute()
//            return response.body()
//        }
//    }
//}