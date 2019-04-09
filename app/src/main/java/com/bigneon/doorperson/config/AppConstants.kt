package com.bigneon.doorperson.config

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.03.2019..
 ****************************************************/
interface AppConstants {
    companion object {
        const val BASE_URL = "https://beta.bigneon.com/api/"

        const val PREFS_FILENAME = "com.bigneon.doorperson.prefs"
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"

        const val CHECK_IN_MODE = "check_in_mode"
        const val CHECK_IN_MODE_MANUAL = "M"
        const val CHECK_IN_MODE_AUTOMATIC = "A"

        // DB const
        const val DATABASE_NAME = "doorperson.db"
        const val DATABASE_VERSION = 1

    }

    enum class SyncTableName constructor(private val tableName: String) {
        EVENTS("events"),
        TICKETS("tickets");

        override fun toString(): String {
            return this.tableName
        }
    }
}