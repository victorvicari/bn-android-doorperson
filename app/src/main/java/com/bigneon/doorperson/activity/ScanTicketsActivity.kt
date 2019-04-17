package com.bigneon.doorperson.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView.BufferType
import com.bigneon.doorperson.R
import com.bigneon.doorperson.config.AppConstants
import com.bigneon.doorperson.config.SharedPrefs
import com.bigneon.doorperson.db.SyncController
import com.bigneon.doorperson.db.ds.TicketsDS
import com.bigneon.doorperson.rest.RestAPI
import com.bigneon.doorperson.rest.model.TicketModel
import com.bigneon.doorperson.util.NetworkUtils
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_scan_tickets.*
import kotlinx.android.synthetic.main.activity_ticket.*
import kotlinx.android.synthetic.main.content_ticket.view.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONObject


class ScanTicketsActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private val TAG = ScanTicketsActivity::class.java.simpleName
    private var eventId: String? = null

    private var mScannerView: ZXingScannerView? = null
    private var cameraPermissionGranted: Boolean = false
    private var checkInMode: String? = null
    private var ticketsDS: TicketsDS? = null

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the scanner view as the content view
        setContentView(R.layout.activity_scan_tickets)

        ticketsDS = TicketsDS()

        eventId = intent.getStringExtra("eventId")

        mScannerView = zxscan   // Programmatically initialize the scanner view

        // Set initial check-in mode from shared preferences if it's already stored. Otherwise set manual
        checkInMode =
            SharedPrefs.getProperty(AppConstants.CHECK_IN_MODE) ?: AppConstants.CHECK_IN_MODE_MANUAL
        if (checkInMode == "") checkInMode = AppConstants.CHECK_IN_MODE_MANUAL

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
                        startActivity(Intent(getContext(), EventsActivity::class.java))
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
            val json = rawResult.text
            val jsonObj = JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1))
            val jsonObjectData = jsonObj.getJSONObject("data")
            val redeemKey = jsonObjectData.getString("redeem_key")
            val ticketId = jsonObjectData.getString("id")

            fun checkInTicket() {
                val ticket = ticketsDS!!.setCheckedTicket(ticketId)
                if (ticket != null) {
                    scanning_ticket_layout.checked_status?.visibility = View.VISIBLE
                    scanning_ticket_layout.purchased_status?.visibility = View.GONE
                    scanning_ticket_layout.complete_check_in?.visibility = View.GONE

                    Snackbar
                        .make(
                            scanning_ticket_layout,
                            "Checked in ${ticket.lastName + ", " + ticket.firstName}",
                            Snackbar.LENGTH_LONG
                        )
                        .setDuration(5000).show()
                } else {
                    Snackbar
                        .make(
                            scanning_ticket_layout,
                            "User ticket already redeemed! Redeem key: $redeemKey",
                            Snackbar.LENGTH_LONG
                        )
                        .setDuration(5000).show()
                }
            }

            fun redeemTicket() {
                fun setAccessToken(accessToken: String?) {
                    if (accessToken == null) {
                        Snackbar
                            .make(
                                scanning_ticket_layout,
                                "Ticket is NOT redeemed. Error in connection",
                                Snackbar.LENGTH_LONG
                            )
                            .setDuration(5000).show()
                        startActivity(
                            Intent(
                                getContext(),
                                LoginActivity::class.java
                            )
                        )
                    } else {
                        fun redeemTicketResult() {
                            fun getTicketResult(isRedeemed: Boolean, ticket: TicketModel?) {
                                if (isRedeemed) {
                                    ticketsDS!!.setRedeemedTicket(ticketId)

                                    scanning_ticket_layout.redeemed_status?.visibility = View.VISIBLE
                                    scanning_ticket_layout.purchased_status?.visibility = View.GONE
                                    scanning_ticket_layout.complete_check_in?.visibility = View.GONE

                                    Snackbar
                                        .make(
                                            scanning_ticket_layout,
                                            "Redeemed ${ticket?.lastName + ", " + ticket?.firstName}",
                                            Snackbar.LENGTH_LONG
                                        )
                                        .setDuration(5000).show()
                                } else {
                                    // build alert dialog
                                    val dialogBuilder = AlertDialog.Builder(getContext())

                                    // set message of alert dialog
                                    dialogBuilder.setMessage("User ticket is NOT redeemed because offline mode has been disabled and there is no internet connection")
                                        .setCancelable(false)
                                        .setPositiveButton("Turn on the offline mode") { _, _ ->
                                            run {
                                                SyncController.isOfflineModeEnabled = true
                                                checkInTicket()
                                            }
                                        }
                                        .setNegativeButton("Turn on the WiFi") { _, _ ->
                                            run {
                                                NetworkUtils.instance().setWiFiEnabled(getContext(), true)
                                                redeemTicket()
                                            }
                                        }
                                    val alert = dialogBuilder.create()
                                    alert.setTitle("Error in connection!")
                                    alert.show()
                                }
                            }
                            RestAPI.getTicket(accessToken, ticketId, ::getTicketResult)
                        }

                        RestAPI.redeemTicketForEvent(
                            accessToken,
                            eventId!!,
                            ticketId!!,
                            redeemKey!!,
                            ::redeemTicketResult
                        )
                    }
                }
                RestAPI.accessToken(::setAccessToken)
            }

            if (checkInMode == AppConstants.CHECK_IN_MODE_MANUAL) {
                val ticket = ticketsDS!!.getTicket(ticketId)

                val intent = Intent(getContext(), TicketActivity::class.java)
                intent.putExtra("ticketId", ticket?.ticketId)
                intent.putExtra("eventId", ticket?.eventId)
                intent.putExtra("redeemKey", ticket?.redeemKey)
                intent.putExtra("searchGuestText", "")
                intent.putExtra("firstName", ticket?.firstName)
                intent.putExtra("lastName", ticket?.lastName)
                intent.putExtra("priceInCents", ticket?.priceInCents)
                intent.putExtra("ticketTypeName", ticket?.ticketType)
                intent.putExtra("status", ticket?.status)
                intent.putExtra("position", -1)
                startActivity(intent)
            } else {
                if (SyncController.isOfflineModeEnabled) {
                    checkInTicket()
                } else {
                    redeemTicket()
                }
            }

            Log.v(TAG, rawResult.text) // Prints scan results
            Log.v(TAG, rawResult.barcodeFormat.toString()) // Prints the scan format (qrcode, pdf417 etc.)

            mScannerView?.resumeCameraPreview(this)
        }
    }

    private fun setButtonText() {
        val text = SpannableString(
            getString(R.string.check_in_mode) + " " + (if (checkInMode == AppConstants.CHECK_IN_MODE_MANUAL) getString(R.string.manual) else getString(
                R.string.automatic
            ))
        )
        text.setSpan(ForegroundColorSpan(getColor(R.color.colorWhite)), 0, getString(R.string.check_in_mode).length, 0)
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
}
