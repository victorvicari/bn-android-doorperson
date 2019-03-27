package com.bigneon.doorperson.rest

import com.bigneon.doorperson.config.AppConstants.Companion.BASE_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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

        val okHttpClient = OkHttpClient().newBuilder().addInterceptor(interceptor).build()

        val builder = Retrofit.Builder()
        builder.baseUrl(BASE_URL)
        builder.client(okHttpClient)
        builder.addConverterFactory(GsonConverterFactory.create())
        val retrofit = builder.build()
        client = retrofit.create<RestClient>(RestClient::class.java)
    }

    companion object {

        fun client(): RestClient {
            return Loader.INSTANCE.client
        }
    }
}