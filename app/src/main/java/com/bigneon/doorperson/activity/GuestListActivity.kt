package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.bigneon.doorperson.adapter.GuestListAdapter
import com.bigneon.doorperson.adapter.OnItemClickListener
import com.bigneon.doorperson.adapter.addOnItemClickListener
import com.bigneon.doorperson.controller.RecyclerItemTouchHelper
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.GuestModel
import kotlinx.android.synthetic.main.activity_guest_list.*
import kotlinx.android.synthetic.main.content_guest_list.*
import kotlinx.android.synthetic.main.content_guest_list.view.*


class GuestListActivity : AppCompatActivity() {
    private val TAG = GuestListActivity::class.java.simpleName

    private var eventId: String = ""
    private var position: Int = -1
    private var offset: Int = 0
    private var searchGuestText: String = ""
    private val recyclerItemTouchHelper: RecyclerItemTouchHelper = RecyclerItemTouchHelper()

    companion object {
        private var searchTextChanged: Boolean = false
        private var screenRotation: Boolean = false
        var guestList: ArrayList<GuestModel>? = null
        val finallyFilteredGuestList = ArrayList<GuestModel>()
    }

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bigneon.doorperson.R.layout.activity_guest_list)

        setSupportActionBar(guest_list_toolbar)

        //this line shows back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val itemTouchHelper = ItemTouchHelper(recyclerItemTouchHelper)
        recyclerItemTouchHelper.parentLayout = guests_layout
        itemTouchHelper.attachToRecyclerView(guest_list_view)

        search_guest.post {
            search_guest.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                    searchTextChanged = true
                    adaptListView(findViewById(com.bigneon.doorperson.R.id.guest_list_view))
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }

        guest_list_toolbar.navigationIcon!!.setColorFilter(
            ContextCompat.getColor(getContext(), com.bigneon.doorperson.R.color.colorAccent),
            PorterDuff.Mode.SRC_ATOP
        )

        guest_list_toolbar.setNavigationOnClickListener {
            val intent = Intent(getContext(), ScanTicketsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        searchGuestText = intent.getStringExtra("searchGuestText") ?: ""
        if (searchGuestText.isEmpty()) {
            finallyFilteredGuestList.clear()
        } else {
            search_guest.setText(searchGuestText)
            searchTextChanged = true
        }
        eventId = intent.getStringExtra("eventId")
        position = intent.getIntExtra("position", -1)
        offset = intent.getIntExtra("offset", 0)

        RestAPI.getGuestsForEvent(getContext(), guests_layout, eventId, ::populateGuestList)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        screenRotation = true
    }

    private fun populateGuestList(guestModelList: ArrayList<GuestModel>?) {
        guestList = guestModelList
        guest_list_view.layoutManager =
            LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
        guests_layout.loading_guests_progress_bar.visibility = View.GONE
        adaptListView(guest_list_view)

        if (position >= 0) {
//            (guest_list_view.layoutManager as LinearLayoutManager).scrollToPosition(position)
            (guest_list_view.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, offset)
        }

        guest_list_view.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {

                val filteredList =
                    if (GuestListActivity.finallyFilteredGuestList.size > 0) GuestListActivity.finallyFilteredGuestList else GuestListActivity.guestList
                val intent = Intent(getContext(), GuestActivity::class.java)
                intent.putExtra("id", filteredList?.get(position)?.id)
                intent.putExtra("eventId", eventId)
                intent.putExtra("redeemKey", filteredList?.get(position)?.redeemKey)
                intent.putExtra("searchGuestText", searchGuestText)
                intent.putExtra("firstName", filteredList?.get(position)?.firstName)
                intent.putExtra("lastName", filteredList?.get(position)?.lastName)
                intent.putExtra("priceInCents", filteredList?.get(position)?.priceInCents)
                intent.putExtra("ticketTypeName", filteredList?.get(position)?.ticketType)
                intent.putExtra("status", filteredList?.get(position)?.status)
                intent.putExtra("position", position)
                intent.putExtra(
                    "offset",
                    (guest_list_view.layoutManager as LinearLayoutManager).findViewByPosition(position)!!.top
                )
                startActivity(intent)
            }
        })

        Log.d(TAG, "SUCCESS")
    }

    private fun adaptListView(guestListView: RecyclerView) {
        val searchWords = search_guest.text.toString().split(" ")
        finallyFilteredGuestList.clear()

        for (word in searchWords) {
            val filteredGuestList = guestList?.filter {
                it.firstName?.toLowerCase()!!.contains(word.toLowerCase()) || it.lastName?.toLowerCase()!!.contains(
                    word.toLowerCase()
                )
            } as ArrayList<GuestModel>
            filteredGuestList.forEach { if (it !in finallyFilteredGuestList) finallyFilteredGuestList.add(it) }
            finallyFilteredGuestList.sortedWith(compareBy({ it.lastName }, { it.firstName }))
        }

        if (screenRotation || searchTextChanged) {
            guestListView.adapter =
                GuestListAdapter(finallyFilteredGuestList)
            recyclerItemTouchHelper.guestList = finallyFilteredGuestList
            recyclerItemTouchHelper.adapter = guestListView.adapter as GuestListAdapter

            screenRotation = false
            searchTextChanged = false
        } else {
            guestListView.adapter =
                GuestListAdapter(guestList!!)
            recyclerItemTouchHelper.guestList = guestList
            recyclerItemTouchHelper.adapter = guestListView.adapter as GuestListAdapter
        }

        if (guestListView.adapter?.itemCount!! > 0) {
            guest_list_view.visibility = View.VISIBLE
            no_guests_found_placeholder.visibility = View.GONE
        } else {
            guest_list_view.visibility = View.GONE
            no_guests_found_placeholder.visibility = View.VISIBLE
        }
    }
}
