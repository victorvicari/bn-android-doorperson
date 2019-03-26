package com.bigneon.doorperson.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView.BufferType
import com.bigneon.doorperson.R
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_scan_tickets.*
import me.dm7.barcodescanner.zxing.ZXingScannerView


class ScanTicketsActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private val TAG = ScanTicketsActivity::class.java.simpleName

    private var mScannerView: ZXingScannerView? = null
    private var cameraPermissionGranted: Boolean = false
    private var checkInMode: String = "M"

    private fun getContext(): Context {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the scanner view as the content view
        setContentView(R.layout.activity_scan_tickets)
        mScannerView = zxscan   // Programmatically initialize the scanner view

        setButtonText()

        check_in_mode_button.setOnClickListener {
            checkInMode = if (checkInMode == "M") "A" else "M"
            setButtonText()
        }

        check_in_mode_exit.setOnClickListener {
            startActivity(Intent(getContext(), ScanningEventActivity::class.java))

            val eventId = intent.getStringExtra("eventId")
            val intent = Intent(getContext(), ScanningEventActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        if (checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 5);
        } else {
            cameraPermissionGranted = true
        }

        guest_list_layout.setOnClickListener {
            startActivity(Intent(getContext(), GuestListActivity::class.java))
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
            Snackbar
                .make(scan_tickets_layout, rawResult.text, Snackbar.LENGTH_LONG)
                .setDuration(5000).show()

            Log.v(TAG, rawResult.text); // Prints scan results
            Log.v(TAG, rawResult.barcodeFormat.toString()); // Prints the scan format (qrcode, pdf417 etc.)

            mScannerView!!.resumeCameraPreview(this);
        }
    }

    private fun setButtonText() {
        val text = SpannableString(
            getString(R.string.check_in_mode) + " " + (if (checkInMode == "M") getString(R.string.manual) else getString(
                R.string.automatic
            ))
        )
        text.setSpan(ForegroundColorSpan(getColor(R.color.colorWhite)), 0, getString(R.string.check_in_mode).length, 0)
        text.setSpan(
            ForegroundColorSpan(getColor(R.color.colorAccent)),
            getString(R.string.check_in_mode).length + 1,
            getString(R.string.check_in_mode).length + (if (checkInMode == "M") getString(R.string.manual).length else getString(
                R.string.automatic
            ).length) + 1,
            0
        )
        // shove our styled text into the Button
        check_in_mode_button.setText(text, BufferType.SPANNABLE)

    }
}
