package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.bigneon.doorperson.adapter.GuestListAdapter
import com.bigneon.doorperson.adapter.OnItemClickListener
import com.bigneon.doorperson.adapter.addOnItemClickListener
import com.bigneon.doorperson.auth.AppAuth
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.GuestModel
import com.bigneon.doorperson.rest.response.GuestsResponse
import kotlinx.android.synthetic.main.activity_guest_list.*
import kotlinx.android.synthetic.main.content_guest_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GuestListActivity : AppCompatActivity() {
    private val TAG = GuestListActivity::class.java.simpleName
    private var eventId: String = ""
    private var guestListView: RecyclerView? = null
    private var guestList: ArrayList<GuestModel>? = null

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_guest_list)

        setSupportActionBar(guest_list_toolbar)

        eventId = intent.getStringExtra("eventId")

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        search_guest.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val searchWords = charSequence.split(" ")
                val finallyFilteredGuestList = ArrayList<GuestModel>()

                for (word in searchWords) {
                    val filteredGuestList = guestList?.filter {
                        it.firstName?.toLowerCase()!!.contains(word.toLowerCase()) || it.lastName?.toLowerCase()!!.contains(
                            word.toLowerCase()
                        )
                    } as ArrayList<GuestModel>
                    filteredGuestList.forEach { if (it !in finallyFilteredGuestList) finallyFilteredGuestList.add(it) }
                }
                guestListView?.adapter = GuestListAdapter(finallyFilteredGuestList)
            }
        })

        guest_list_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), com.bigneon.doorperson.R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        guest_list_toolbar.setNavigationOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        getGuestsForEvent()
    }

    private fun getGuestsForEvent() {
        val getGuestsForEventCall =
            RestAPI.client().getGuestsForEvent(AppAuth.getAccessToken(getContext()), eventId, null)
        val callbackGetScannableEvents = object : Callback<GuestsResponse> {
            override fun onResponse(call: Call<GuestsResponse>, response: Response<GuestsResponse>) {
                if (response.code() == 401) { //Unauthorized
                    Snackbar
                        .make(scanning_guest_layout, "Unauthorized request!", Snackbar.LENGTH_LONG)
                        .setDuration(5000).show()
                    Log.e(TAG, "MSG:" + response.message() + ", CODE: " + response.code())
                } else {
                    guestListView = findViewById(com.bigneon.doorperson.R.id.guest_list_view)
                    guestList = response.body()!!.data

                    guestListView?.layoutManager =
                        LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)

                    guestListView?.adapter = GuestListAdapter(guestList!!)

                    guestListView?.addOnItemClickListener(object : OnItemClickListener {
                        override fun onItemClicked(position: Int, view: View) {
                            val intent = Intent(getContext(), GuestActivity::class.java)
                            intent.putExtra("eventId", eventId)
                            startActivity(intent)
                        }
                    })

                    Log.d(TAG, "SUCCESS")
                }
            }

            override fun onFailure(call: Call<GuestsResponse>, t: Throwable) {
                Snackbar
                    .make(scanning_guest_layout, "Authentication error!", Snackbar.LENGTH_LONG)
                    .setAction(
                        "RETRY"
                    ) { startActivity(Intent(getContext(), LoginActivity::class.java)) }.setDuration(5000).show()
                Log.e(TAG, "Failure MSG:" + t.message)
            }
        }
        getGuestsForEventCall.enqueue(callbackGetScannableEvents)
    }
}
