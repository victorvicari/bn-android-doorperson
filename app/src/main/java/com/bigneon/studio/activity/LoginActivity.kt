package com.bigneon.studio.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import com.bigneon.studio.R
import com.bigneon.studio.config.SharedPrefs
import com.bigneon.studio.receiver.NetworkStateReceiver
import com.bigneon.studio.rest.RestAPI
import com.bigneon.studio.util.NetworkUtils.Companion.addNetworkStateListener
import com.bigneon.studio.util.NetworkUtils.Companion.removeNetworkStateListener
import com.bigneon.studio.util.NetworkUtils.Companion.setWiFiDisabled
import com.bigneon.studio.util.NetworkUtils.Companion.setWiFiEnabled
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.content_login.*

class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.simpleName
    private var showPassword: Boolean = false
    private var turnOnWifiClicked: Boolean = false
    private var countDownTimerIsTicking: Boolean = false

    private var networkStateReceiverListener: NetworkStateReceiver.NetworkStateReceiverListener =
        object : NetworkStateReceiver.NetworkStateReceiverListener {
            val countDownTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    turn_on_wifi.progress = -1
                    Handler().postDelayed({
                        try {
                            turnOnWifiClicked = false
                            turn_on_wifi.progress = 0
                            loginBtn.isEnabled = false
                            setWiFiDisabled(getContext())
                        } catch (e: Exception) {
                            Log.e(TAG, e.message)
                        }
                    }, 3000)
                }
            }

            override fun networkAvailable() {
                if (turnOnWifiClicked) {
                    turn_on_wifi.progress = 100
                    Handler().postDelayed({
                        try {
                            turnOnWifiClicked = false
                            turn_on_wifi.visibility = View.INVISIBLE
                            loginBtn.isEnabled = true
                            if (countDownTimerIsTicking) {
                                countDownTimer.cancel()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, e.message)
                        }
                    }, 3000)
                } else {
                    turn_on_wifi.visibility = View.INVISIBLE
                    loginBtn.isEnabled = true
                }
            }

            override fun networkUnavailable() {
                if (turnOnWifiClicked) {
                    if (countDownTimerIsTicking) {
                        countDownTimer.cancel()
                    }
                    countDownTimer.start()
                    countDownTimerIsTicking = true
                } else {
                    turn_on_wifi.progress = 0
                    turn_on_wifi.visibility = View.VISIBLE
                    loginBtn.isEnabled = false
                }
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
        RestAPI.setBaseURL()

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        login_email_address.setText(intent.getStringExtra("email"))
        //login_password.setText(intent.getStringExtra("password"))

        turn_on_wifi.isIndeterminateProgressMode = true
        turn_on_wifi.setOnClickListener {
            turnOnWifiClicked = true
            if (turn_on_wifi.progress == 0) {
                turn_on_wifi.progress = 30
            }
            setWiFiEnabled(getContext())
        }

        login_email_address.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                email_address_message.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(s: Editable) {}
        })

        login_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                password_message.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(s: Editable) {}
        })

        loginBtn.isIndeterminateProgressMode = true
        loginBtn.setOnClickListener {
            when {
                login_email_address.text.toString().isEmpty() -> {
                    email_address_message.visibility = View.VISIBLE
                    login_email_address.startAnimation(loadAnimation(this, R.anim.shake))
                }
                else -> {
                    if (loginBtn.progress == 0) {
                        loginBtn.progress = 30
                    }
                    Handler().postDelayed({
                        try {
                            val email = login_email_address.text.toString()
                            val password = login_password.text.toString()
                            fun setAccessToken(accessToken: String?) {
                                if (accessToken == null) {
                                    loginBtn.progress = -1
                                    loginBtn.startAnimation(loadAnimation(this, R.anim.shake))
                                    Handler().postDelayed({
                                        loginBtn.progress = 0

                                        intent.putExtra("email", login_email_address.text.toString())
                                        //intent.putExtra("password", login_password.text.toString())
                                        startActivity(intent)
                                    }, 3000)
                                } else {
                                    Crashlytics.setUserEmail(email)
                                    loginBtn.progress = 100
                                    Handler().postDelayed({
                                        startActivity(Intent(getContext(), EventListActivity::class.java))
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
        addNetworkStateListener(this, networkStateReceiverListener)
        super.onStart()
    }

    override fun onStop() {
        removeNetworkStateListener(this, networkStateReceiverListener)
        super.onStop()
    }

    override fun onBackPressed() {
        finish()
        moveTaskToBack(true)
    }
}
