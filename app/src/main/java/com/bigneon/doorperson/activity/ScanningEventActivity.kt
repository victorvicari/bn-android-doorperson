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
import com.bigneon.doorperson.rest.response.GuestsResponse
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

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scanning_events_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        scanning_events_toolbar.setNavigationOnClickListener {
            startActivity(Intent(getContext(), EventsActivity::class.java))
        }

        getGuestsForEvent()

        scanning_events_button.setOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }
    }

    private fun getGuestsForEvent() {
        eventId = intent.getStringExtra("eventId")

        val getGuestsForEventCall =
            RestAPI.client().getGuestsForEvent(AppAuth.getAccessToken(getContext()), eventId, null)
        val callbackGetScannableEvents = object : Callback<GuestsResponse> {
            override fun onResponse(call: Call<GuestsResponse>, response: Response<GuestsResponse>) {
                if (response.code() == 401) { //Unauthorized
                    Snackbar
                        .make(scanning_events_layout, "Unauthorized request!", Snackbar.LENGTH_LONG)
                        .setDuration(5000).show()
                    Log.e(TAG, "MSG:" + response.message() + ", CODE: " + response.code())
                } else {
                    val purNumber = response.body()!!.data?.stream()
                        ?.filter { g -> g.status.equals("Purchased") }
                        ?.count()
                    val redNumber = response.body()!!.data?.stream()
                        ?.filter { g -> g.status.equals("Redeemed") }
                        ?.count()
                    val purAndRedNumber = purNumber!!.plus(redNumber!!)
                    number_of_redeemed.text = getString(R.string._1_d_of_2_d_redeemed, redNumber, purAndRedNumber)
                }
            }

            override fun onFailure(call: Call<GuestsResponse>, t: Throwable) {
                Snackbar
                    .make(scanning_events_layout, "Authentication error!", Snackbar.LENGTH_LONG)
                    .setAction(
                        "RETRY"
                    ) { startActivity(Intent(getContext(), LoginActivity::class.java)) }.setDuration(5000).show()
                Log.e(TAG, "Failure MSG:" + t.message)
            }
        }
        getGuestsForEventCall.enqueue(callbackGetScannableEvents)
    }
}