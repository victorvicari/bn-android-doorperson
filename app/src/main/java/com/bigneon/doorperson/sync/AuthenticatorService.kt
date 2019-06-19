package com.bigneon.doorperson.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder



/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 18.06.2019..
 ****************************************************/
class AuthenticatorService : Service() {

    private var authenticator: Authenticator? = null

    override fun onCreate() {
        authenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator!!.iBinder
    }
}