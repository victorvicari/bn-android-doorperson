package com.bigneon.doorperson.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.bigneon.doorperson.adapter.GuestListAdapter
import com.bigneon.doorperson.controller.RecyclerItemTouchHelper
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.GuestModel
import com.bigneon.doorperson.viewholder.GuestViewHolder
import kotlinx.android.synthetic.main.activity_guest_list.*
import kotlinx.android.synthetic.main.content_guest_list.*


class GuestListActivity : AppCompatActivity(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private val TAG = GuestListActivity::class.java.simpleName
    private var eventId: String = ""
    private var position: Int = -1
    //private var guestListView: RecyclerView? = null
    private val recyclerItemTouchHelper: RecyclerItemTouchHelper = RecyclerItemTouchHelper(this)

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
        itemTouchHelper.attachToRecyclerView(guest_list_view)

        search_guest.post {
            search_guest.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                    searchTextChanged(charSequence)
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

        val searchGuestText = intent.getStringExtra("searchGuestText") ?: ""
        if (searchGuestText.isEmpty()) {
            finallyFilteredGuestList.clear()
        } else {
            search_guest.setText(searchGuestText)
            searchTextChanged = true
        }
        eventId = intent.getStringExtra("eventId")
        position = intent.getIntExtra("position", -1)

        RestAPI.getGuestsForEvent(getContext(), guests_layout, eventId, position, searchGuestText, ::adaptListView)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        searchTextChanged(search_guest.text.toString())
        screenRotation = true
    }

    private fun searchTextChanged(charSequence: CharSequence) {
        val searchWords = charSequence.split(" ")
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
        searchTextChanged = true
        adaptListView(findViewById(com.bigneon.doorperson.R.id.guest_list_view))
    }

    private fun adaptListView(guestListView: RecyclerView) {
        if (screenRotation || searchTextChanged) {
            guestListView.adapter = GuestListAdapter(finallyFilteredGuestList)
            screenRotation = false
            searchTextChanged = false
        } else {
            guestListView.adapter = GuestListAdapter(guestList!!)
        }

        if (guestListView.adapter?.itemCount!! > 0) {
            guest_list_view.visibility = View.VISIBLE
            no_guests_found_placeholder.visibility = View.GONE
        } else {
            guest_list_view.visibility = View.GONE
            no_guests_found_placeholder.visibility = View.VISIBLE
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (viewHolder is GuestViewHolder) {
            if (!viewHolder.checkedIn) {
                // build alert dialog
                val dialogBuilder = AlertDialog.Builder(this)

                // set message of alert dialog
                dialogBuilder.setMessage("Checked in ${viewHolder.lastNameAndFirstNameTextView?.text.toString()}")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, _ ->
                        run {
                            Log.d(
                                TAG,
                                "ACTION: CHECK-IN: ${viewHolder.lastNameAndFirstNameTextView?.text.toString()}"
                            )
                            dialog.cancel()
                            ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder.itemView)
                        }
                    }
                val alert = dialogBuilder.create()
                alert.setTitle("Alert")
                alert.show()
            } else {
                viewHolder.swipeBack = true
            }

//            // get the removed item name to display it in snack bar
//            val name = finallyFilteredGuestList.get(viewHolder.adapterPosition).getName()
//
//            // backup of removed item for undo purpose
//            val deletedItem = finallyFilteredGuestList.get(viewHolder.adapterPosition)
//            val deletedIndex = viewHolder.adapterPosition
//
//            // remove the item from recycler view
//            mAdapter.removeItem(viewHolder.adapterPosition)
//
//            // showing snack bar with Undo option
//            val snackbar = Snackbar
//                .make(coordinatorLayout, name + " removed from cart!", Snackbar.LENGTH_LONG)
//            snackbar.setAction("UNDO", View.OnClickListener {
//                // undo is selected, restore the deleted item
//                mAdapter.restoreItem(deletedItem, deletedIndex)
//            })
//            snackbar.setActionTextColor(Color.YELLOW)
//            snackbar.show()
        }
    }
}
