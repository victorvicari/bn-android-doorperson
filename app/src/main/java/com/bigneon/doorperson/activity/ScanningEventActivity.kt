package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bigneon.doorperson.R
import com.bigneon.doorperson.auth.AppAuth
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.response.DashboardResponse
import kotlinx.android.synthetic.main.activity_scanning_event.*
import kotlinx.android.synthetic.main.content_scanning_event.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanningEventActivity : AppCompatActivity() {
    private val TAG = ScanningEventActivity::class.java.simpleName
    private var eventId: String = ""

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning_event)
        setSupportActionBar(scanning_events_toolbar)

        eventId = intent.getStringExtra("eventId")

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scanning_events_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        scanning_events_toolbar.setNavigationOnClickListener {
            startActivity(Intent(getContext(), EventsActivity::class.java))
        }

        getDashboardForEvent()

        scanning_events_button.setOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }
    }

    private fun getDashboardForEvent() {
        val getDashboardForEventCall =
            RestAPI.client().getDashboardForEvent(AppAuth.getAccessToken(getContext()), eventId)
        val callbackGetDashboardForEvent = object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (response.code() == 401) { //Unauthorized
                    Snackbar
                        .make(scanning_events_layout, "Unauthorized request!", Snackbar.LENGTH_LONG)
                        .setDuration(5000).show()
                    Log.e(TAG, "MSG:" + response.message() + ", CODE: " + response.code())
                } else {
                    val ticketsRedeemed = response.body()!!.event?.ticketsRedeemed ?: 0
                    val soldHeld = response.body()!!.event?.soldHeld ?: 0
                    val soldUnreserved = response.body()!!.event?.soldUnreserved ?: 0
                    number_of_redeemed.text =
                        getString(R.string._1_d_of_2_d_redeemed, ticketsRedeemed, soldHeld + soldUnreserved)
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                Snackbar
                    .make(scanning_events_layout, "Authentication error!", Snackbar.LENGTH_LONG)
                    .setAction(
                        "RETRY"
                    ) { startActivity(Intent(getContext(), LoginActivity::class.java)) }.setDuration(5000).show()
                Log.e(TAG, "Failure MSG:" + t.message)
            }
        }
        getDashboardForEventCall.enqueue(callbackGetDashboardForEvent)
    }
}