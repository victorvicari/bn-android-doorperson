package com.bigneon.studio.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView.BufferType
import com.bigneon.studio.R
import com.bigneon.studio.config.AppConstants
import com.bigneon.studio.config.SharedPrefs
import com.bigneon.studio.controller.TicketDataHandler
import com.bigneon.studio.db.ds.TicketsDS
import com.bigneon.studio.util.AppUtils
import com.bigneon.studio.util.AppUtils.Companion.checkLogged
import com.bigneon.studio.util.AppUtils.Companion.enableOfflineMode
import com.bigneon.studio.util.ConnectionDialog
import com.bigneon.studio.util.NetworkUtils
import com.bigneon.studio.util.NetworkUtils.Companion.setWiFiEnabled
import com.google.zxing.Result
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_scan_tickets.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONObject
import org.json.JSONTokener


class ScanTicketsActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private val TAG = ScanTicketsActivity::class.java.simpleName
    private var eventId: String? = null
    private var scannedTicketId: String? = null
    private var status: String? = null
    private var mScannerView: ZXingScannerView? = null
    private var cameraPermissionGranted: Boolean = false
    private var checkInMode: String? = null
    private var ticketsDS: TicketsDS? = null
    private var readingTicket: Boolean = false
    private var searchGuestText: String = ""

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the scanner view as the content view
        setContentView(R.layout.activity_scan_tickets)

        checkLogged()

        ticketsDS = TicketsDS()

        eventId = intent.getStringExtra("eventId")
        val lastCheckedTicketId = SharedPrefs.getProperty(AppConstants.LAST_CHECKED_TICKET_ID + eventId)
        if (!lastCheckedTicketId.isNullOrEmpty())
            showPillUserInfo(true, lastCheckedTicketId)

        searchGuestText = intent.getStringExtra("searchGuestText") ?: ""

        mScannerView = zxscan   // Programmatically initialize the scanner view

        // Set initial check-in mode from shared preferences if it's already stored. Otherwise set manual
        checkInMode =
            SharedPrefs.getProperty(AppConstants.CHECK_IN_MODE) ?: AppConstants.CHECK_IN_MODE_AUTOMATIC
        if (checkInMode == "") checkInMode = AppConstants.CHECK_IN_MODE_AUTOMATIC

        setButtonText()

        check_in_mode_button.setOnClickListener {
            // Change check-in mode
            checkInMode =
                if (checkInMode == AppConstants.CHECK_IN_MODE_MANUAL) AppConstants.CHECK_IN_MODE_AUTOMATIC else AppConstants.CHECK_IN_MODE_MANUAL

            // Save new check-in mode to the shared preferences
            SharedPrefs.setProperty(
                AppConstants.CHECK_IN_MODE,
                if (checkInMode == AppConstants.CHECK_IN_MODE_MANUAL) AppConstants.CHECK_IN_MODE_MANUAL else AppConstants.CHECK_IN_MODE_AUTOMATIC
            )
            setButtonText()
        }

        check_in_mode_exit.setOnClickListener {
            val intent = Intent(getContext(), ScanningEventActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }

        if (checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 5)
        } else {
            cameraPermissionGranted = true
        }

        ticket_list_layout.setOnClickListener {
            val intent = Intent(getContext(), TicketListActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("ticketId", scannedTicketId)
            intent.putExtra("status", status)
            intent.putExtra("searchGuestText", searchGuestText)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            5 -> {
                for (i in 0 until permissions.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        cameraPermissionGranted = true
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Snackbar
                            .make(scan_tickets_layout, "Camera permission denied by user!", Snackbar.LENGTH_LONG)
                            .setDuration(5000).show()
                        Log.e(TAG, "Camera permission denied by user!")
                        startActivity(Intent(getContext(), EventListActivity::class.java))
                    }
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (cameraPermissionGranted) {
            mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
            mScannerView!!.startCamera()          // Start camera on resume
        }
    }

    public override fun onPause() {
        super.onPause()
        if (cameraPermissionGranted) {
            mScannerView!!.stopCamera()           // Stop camera on pause
        }
    }

    override fun handleResult(rawResult: Result) {
        if (cameraPermissionGranted) {
            if (readingTicket) {
                mScannerView?.resumeCameraPreview(this)
                return
            }

            readingTicket = true
            reading_ticket.visibility = View.VISIBLE
            val handler = Handler()
            handler.postDelayed({
                readingTicket = false
                reading_ticket.visibility = View.GONE
            }, 3000)

            val json = JSONTokener(rawResult.text).nextValue()
            if (json is JSONObject) {
                val jsonObj = JSONObject(rawResult.text)
                val jsonObjectData = jsonObj.getJSONObject("data")
                val ticketId = jsonObjectData.getString("id")

                fun completeCheckIn() {
                    val ticket = TicketDataHandler.getTicket(getContext(), ticketId)
                    if (ticket == null) {
                        object : ConnectionDialog() {
                            override fun positiveButtonAction(context: Context) {
                                enableOfflineMode()
                                completeCheckIn()
                            }

                            override fun negativeButtonAction(context: Context) {
                                setWiFiEnabled(context)
                                while (!NetworkUtils.isNetworkAvailable(context)) Thread.sleep(1000)
                                completeCheckIn()
                            }
                        }.showDialog(getContext())
                    } else {
                        if (ticket.ticketId == null) {
                            Snackbar
                                .make(scan_tickets_layout, "QR Code isn't valid!", Snackbar.LENGTH_LONG)
                                .setDuration(3000).show()
                            mScannerView?.resumeCameraPreview(this)

                            zxscan_error.visibility = View.VISIBLE
                            handler.postDelayed({
                                zxscan_error.visibility = View.GONE
                            }, 3000)

                            return
                        }
                        if (ticket.ticketId == SharedPrefs.getProperty(AppConstants.LAST_CHECKED_TICKET_ID + eventId)) {
                            Snackbar
                                .make(scan_tickets_layout, "You just scanned the ticket again!", Snackbar.LENGTH_LONG)
                                .setDuration(3000).show()

                            zxscan_error.visibility = View.VISIBLE
                            handler.postDelayed({
                                zxscan_error.visibility = View.GONE
                            }, 3000)

                            return
                        }
                        if (ticket.eventId != eventId) {
                            Snackbar
                                .make(
                                    scan_tickets_layout,
                                    "The ticket doesn't belong to the current event!",
                                    Snackbar.LENGTH_LONG
                                )
                                .setDuration(3000).show()

                            zxscan_error.visibility = View.VISIBLE
                            handler.postDelayed({
                                zxscan_error.visibility = View.GONE
                            }, 3000)

                            return
                        }
                        if (checkInMode == AppConstants.CHECK_IN_MODE_MANUAL) {
                            val intent = Intent(getContext(), TicketActivity::class.java)
                            intent.putExtra("ticketId", ticket.ticketId)
                            intent.putExtra("eventId", ticket.eventId)
                            intent.putExtra("redeemKey", ticket.redeemKey)
                            intent.putExtra("redeemedBy", ticket.redeemedBy)
                            intent.putExtra("redeemedAt", ticket.redeemedAt)
                            intent.putExtra("searchGuestText", searchGuestText)
                            intent.putExtra("firstName", ticket.firstName)
                            intent.putExtra("lastName", ticket.lastName)
                            intent.putExtra("priceInCents", ticket.priceInCents)
                            intent.putExtra("ticketType", ticket.ticketType)
                            intent.putExtra("status", ticket.status)
                            startActivity(intent)
                        } else {
                            val ticketState = TicketDataHandler.completeCheckIn(getContext(), ticket)
                            status = ticketState?.name
                            scannedTicketId = ticketId

                            when (ticketState) {
                                TicketDataHandler.TicketState.REDEEMED -> {
                                    runOnUiThread {
                                        Snackbar
                                            .make(
                                                scan_tickets_layout,
                                                "Redeemed ${"${ticket.lastName!!}, ${ticket.firstName!!}"}",
                                                Snackbar.LENGTH_LONG
                                            )
                                            .setDuration(3000).show()
                                        zxscan_ok.visibility = View.VISIBLE
                                        handler.postDelayed({
                                            zxscan_ok.visibility = View.GONE
                                        }, 3000)
                                    }
                                }
                                TicketDataHandler.TicketState.CHECKED -> {
                                    runOnUiThread {
                                        Snackbar
                                            .make(
                                                scan_tickets_layout,
                                                "Checked in ${"${ticket.lastName!!}, ${ticket.firstName!!}"}",
                                                Snackbar.LENGTH_LONG
                                            )
                                            .setDuration(3000).show()
                                        zxscan_ok.visibility = View.VISIBLE
                                        handler.postDelayed({
                                            zxscan_ok.visibility = View.GONE
                                        }, 3000)
                                    }
                                }
                                TicketDataHandler.TicketState.DUPLICATED -> {
                                    runOnUiThread {
                                        val intent = Intent(getContext(), DuplicateTicketCheckinActivity::class.java)
                                        intent.putExtra("ticketId", ticketId)
                                        intent.putExtra(
                                            "lastAndFirstName",
                                            "${ticket.lastName!!}, ${ticket.firstName!!}"
                                        )
                                        intent.putExtra("redeemedBy", ticket.redeemedBy)
                                        intent.putExtra("redeemedAt", ticket.redeemedAt)
                                        startActivity(intent)

                                        Snackbar
                                            .make(
                                                scan_tickets_layout,
                                                "Warning: Ticket redeemed by ${ticket.redeemedBy} ${AppUtils.getTimeAgo(
                                                    ticket.redeemedAt!!
                                                )}",
                                                Snackbar.LENGTH_LONG
                                            )
                                            .setDuration(5000).show()
                                        zxscan_error.visibility = View.VISIBLE
                                        handler.postDelayed({
                                            zxscan_error.visibility = View.GONE
                                        }, 3000)
                                    }
                                }
                                TicketDataHandler.TicketState.ERROR -> {
                                    object : ConnectionDialog() {
                                        override fun positiveButtonAction(context: Context) {
                                            enableOfflineMode()
                                            completeCheckIn()
                                        }

                                        override fun negativeButtonAction(context: Context) {
                                            setWiFiEnabled(getContext())
                                            while (!NetworkUtils.isNetworkAvailable(context)) Thread.sleep(1000)
                                            completeCheckIn()
                                        }
                                    }.showDialog(getContext())
                                }
                            }

                            SharedPrefs.setProperty(AppConstants.LAST_CHECKED_TICKET_ID + eventId, ticketId)
                        }
                    }
                }
                completeCheckIn()

                Log.v(TAG, rawResult.text) // Prints scan results
                Log.v(TAG, rawResult.barcodeFormat.toString()) // Prints the scan format (qrcode, pdf417 etc.)
            } else {
                Snackbar
                    .make(
                        scan_tickets_layout,
                        "Error: Invalid QR Code!",
                        Snackbar.LENGTH_LONG
                    )
                    .setDuration(5000).show()
                zxscan_error.visibility = View.VISIBLE
                handler.postDelayed({
                    zxscan_error.visibility = View.GONE
                }, 3000)
            }
            mScannerView?.resumeCameraPreview(this)
        }
    }

    private fun showPillUserInfo(success: Boolean, ticketId: String?) {
        val ticket = ticketsDS!!.getTicket(ticketId!!)

        pill_user_info.visibility = View.VISIBLE

        if (!ticket?.profilePicURL.isNullOrEmpty()) {
            Picasso
                .get() // give it the context
                .load(ticket?.profilePicURL) // load the image
                .into(pill_user_image) // select the ImageView to load it into
        }

        val firstAndLastName = "${ticket?.firstName}, ${ticket?.lastName}"
        pill_user_name.text = firstAndLastName
        pill_ticket_type.text = ticket?.ticketType

        pill_scanned_by.text =
            if (ticket?.redeemedBy != null) getString(R.string.pill_scanned_by_text, ticket.redeemedBy ?: "") else ""

        var redeemedAt = ""
        if (ticket?.redeemedAt != null) {
            redeemedAt = AppUtils.getTimeAgo(ticket.redeemedAt!!)
        }
        pill_scanned_time.text = redeemedAt

        Picasso
            .get()
            .load(if (success) R.drawable.icon_ok else R.drawable.icon_delete)
            .into(pill_checked_status_image)
    }

    private fun setButtonText() {
        val text = SpannableString(
            getString(R.string.check_in_mode) + " " + (if (checkInMode == AppConstants.CHECK_IN_MODE_MANUAL) getString(
                R.string.manual
            ) else getString(
                R.string.automatic
            ))
        )
        text.setSpan(
            ForegroundColorSpan(getColor(R.color.colorWhite)),
            0,
            getString(R.string.check_in_mode).length,
            0
        )
        text.setSpan(
            ForegroundColorSpan(getColor(R.color.colorAccent)),
            getString(R.string.check_in_mode).length + 1,
            getString(R.string.check_in_mode).length + (if (checkInMode == AppConstants.CHECK_IN_MODE_MANUAL) getString(
                R.string.manual
            ).length else getString(
                R.string.automatic
            ).length) + 1,
            0
        )
        // shove our styled text into the Button
        check_in_mode_button.setText(text, BufferType.SPANNABLE)
    }

    override fun onBackPressed() {
        val intent = Intent(getContext(), ScanningEventActivity::class.java)
        intent.putExtra("eventId", eventId)
        startActivity(intent)
        finish()
    }
}
