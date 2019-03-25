package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.bigneon.doorperson.adapter.EventListAdapter
import com.bigneon.doorperson.auth.AppAuth
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.AppConstants.Companion.REFRESH_TOKEN
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.AuthToken
import com.bigneon.doorperson.rest.model.EventsResponse
import com.bigneon.doorperson.rest.model.RefreshTokenRequest
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EventsActivity : AppCompatActivity() {
    private val TAG = EventsActivity::class.java.simpleName

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_events)
        val refreshToken = SharedPrefs.getProperty(getContext(), REFRESH_TOKEN)

        // If there is no refresh token, then user isn't logged in
        if (refreshToken.equals("")) {
            startActivity(Intent(getContext(), LoginActivity::class.java))
        } else {
            getScannableEvents()
        }
    }

    private fun getScannableEvents() {
        try {
            val getScannableEventsCall = RestAPI.client().getScannableEvents(AppAuth.getAccessToken(getContext()))
            val callbackGetScannableEvents = object : Callback<EventsResponse> {
                override fun onResponse(call: Call<EventsResponse>, response: Response<EventsResponse>) {
                    if(response.code() == 401) { //Unauthorized
                        refreshToken()
                    } else {
                        val listView : RecyclerView = findViewById(com.bigneon.doorperson.R.id.events_list_view)
                        val eventList = response.body()!!.data

                        val layoutManager = LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
                        listView.layoutManager = layoutManager

                        listView.adapter = EventListAdapter(getContext(), eventList!!)

                        Log.d(TAG, "SUCCESS")
                    }
                }

                override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
                    Log.d(TAG, "FAILURE")
                    refreshToken()
                }
            }
            getScannableEventsCall.enqueue(callbackGetScannableEvents)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }

    fun refreshToken() {
        try {
            val refreshTokenRequest = RefreshTokenRequest()
            refreshTokenRequest.refresh_token = SharedPrefs.getProperty(getContext(), REFRESH_TOKEN)
            val refreshTokenCall = RestAPI.client().refreshToken(refreshTokenRequest)

            val callbackRefreshToken = object : Callback<AuthToken> {
                override fun onResponse(call: Call<AuthToken>, response: Response<AuthToken>) {
                    if (response.body() != null) {
                        SharedPrefs.setProperty(getContext(), AppConstants.ACCESS_TOKEN, response.body()!!.access_token.orEmpty())
                        SharedPrefs.setProperty(getContext(), REFRESH_TOKEN, response.body()!!.refresh_token.orEmpty())

                        startActivity(Intent(getContext(), EventsActivity::class.java))
                    } else {
                        Snackbar
                            .make(login_layout, "Refresh token is not valid!", Snackbar.LENGTH_LONG)
                            .setDuration(5000).show()
                        Log.e(TAG, "MSG:" + response.message() + ", CODE: " + response.code())
                    }
                }

                override fun onFailure(call: Call<AuthToken>, t: Throwable) {
                    Snackbar
                        .make(login_layout, "Authentication error!", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", object : View.OnClickListener {
                            override fun onClick(view: View) {
                                startActivity(Intent(getContext(), LoginActivity::class.java))
                            }
                        }).setDuration(5000).show()
                    Log.e(TAG, "Failure MSG:" + t.message)
                }
            }

            refreshTokenCall.enqueue(callbackRefreshToken)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }

}
