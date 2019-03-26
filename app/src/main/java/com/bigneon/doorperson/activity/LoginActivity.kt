package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bigneon.doorperson.config.AppConstants.Companion.ACCESS_TOKEN
import com.bigneon.doorperson.config.AppConstants.Companion.REFRESH_TOKEN
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.request.AuthRequest
import com.bigneon.doorperson.rest.response.AuthTokenResponse
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.simpleName

    fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_login)

    }

    fun btnLoginClick(view: View) {
        btnLoginClick()
    }

    fun btnLoginClick() {

        try {
            val authRequest = AuthRequest()
            authRequest.email = if (email_address.text != null) email_address.text.toString() else ""
            authRequest.password = if (password.text != null) password.text.toString() else ""
            val authTokenCall = RestAPI.client().authenticate(authRequest)

            val callbackAuthToken = object : Callback<AuthTokenResponse> {
                override fun onResponse(call: Call<AuthTokenResponse>, response: Response<AuthTokenResponse>) {
                    if (response.body() != null) {
                        SharedPrefs.setProperty(getContext(), ACCESS_TOKEN, response.body()!!.accessToken.orEmpty())
                        SharedPrefs.setProperty(getContext(), REFRESH_TOKEN, response.body()!!.refreshToken.orEmpty())

                        startActivity(Intent(getContext(), EventsActivity::class.java))
                    } else {
                        Snackbar
                            .make(login_layout, "Username and/or password does not match!", Snackbar.LENGTH_LONG)
                            .setDuration(5000).show()
                    }
                }

                override fun onFailure(call: Call<AuthTokenResponse>, t: Throwable) {
                    Snackbar
                        .make(login_layout, "Authentication error!", Snackbar.LENGTH_LONG)
                        .setAction("RETRY") { btnLoginClick() }.setDuration(5000).show()
                }
            }

            authTokenCall.enqueue(callbackAuthToken)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
    }
}
