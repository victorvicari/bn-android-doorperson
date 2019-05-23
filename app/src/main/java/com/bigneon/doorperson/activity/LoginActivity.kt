package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import com.bigneon.doorperson.R
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.db.SQLiteHelper
import com.bigneon.doorperson.db.SyncController
import com.bigneon.doorperson.receiver.NetworkStateReceiver
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.util.NetworkUtils
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.content_login.*


class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.simpleName
    private var showPassword: Boolean = false
    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            override fun networkAvailable() {
                turn_on_wifi.visibility = View.GONE
            }

            override fun networkUnavailable() {
                turn_on_wifi.visibility = View.VISIBLE
            }
        }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_login)

        SharedPrefs.setContext(this)
        RestAPI.setContext(this)
        SyncController.setContext(this)
        SQLiteHelper.setContext(this)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        turn_on_wifi.setOnClickListener {
            NetworkUtils.instance().setWiFiEnabled(this, true)
        }

        email_address.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                email_address_message.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable) {}
        })

        password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                password_message.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable) {}
        })

        show_hide_password.setOnClickListener {
            show_hide_password.text =
                if (show_hide_password.text == getString(R.string.show)) getString(R.string.hide) else getString(
                    R.string.show
                )

            showPassword = show_hide_password.text == getString(R.string.show)
            password.transformationMethod =
                if (showPassword) PasswordTransformationMethod.getInstance() else HideReturnsTransformationMethod.getInstance()
        }

        loginBtn.isIndeterminateProgressMode = true
        loginBtn.setOnClickListener {
            when {
                email_address.text.toString().isEmpty() -> {
                    email_address_message.visibility = View.VISIBLE
                    email_address.startAnimation(loadAnimation(this, R.anim.shake))
                }
                password.text.toString().isEmpty() || password.text?.length!! < 7 -> {
                    password_message.visibility = View.VISIBLE
                    password_with_show_hide.startAnimation(loadAnimation(this, R.anim.shake))
                }
                else -> {
                    if (loginBtn.progress == 0) {
                        loginBtn.progress = 30
                    }
                    Handler().postDelayed({
                        try {
                            val email = email_address.text.toString()
                            val password = password.text.toString()
                            fun setAccessToken(accessToken: String?) {
                                if (accessToken == null) {
                                    loginBtn.progress = -1
                                    loginBtn.startAnimation(loadAnimation(this, R.anim.shake))
                                    Handler().postDelayed({
                                        loginBtn.progress = 0
                                    }, 3000)
                                } else {
                                    Crashlytics.setUserEmail(email)
                                    loginBtn.progress = 100
                                    Handler().postDelayed({
                                        startActivity(Intent(getContext(), EventsActivity::class.java))
                                        finish()
                                    }, 1000)
                                }
                            }
                            RestAPI.authenticate(email, password, ::setAccessToken)
                        } catch (e: Exception) {
                            Log.e(TAG, e.message)
                        }
                    }, 3000)
                }
            }
        }
    }

    override fun onStart() {
        NetworkUtils.instance().addNetworkStateListener(this, networkStateReceiverListener)
        super.onStart()
    }

    override fun onStop() {
        NetworkUtils.instance().removeNetworkStateListener(this, networkStateReceiverListener)
        super.onStop()
    }

    override fun onBackPressed() {
        finish()
        moveTaskToBack(true)
    }
}
