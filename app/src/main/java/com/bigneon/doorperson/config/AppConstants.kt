package com.bigneon.doorperson.config

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 20.03.2019..
 ****************************************************/
interface AppConstants {
    companion object {
        const val BASE_URL = "https://dev.api.bigneon.com"
//        const val BASE_URL = "https://staging.api.bigneon.com"
//        const val BASE_URL = "https://api.staging.bigneon.com"

        const val PREFS_FILENAME = "com.bigneon.doorperson.prefs"
        const val REFRESH_TOKEN = "refresh_token"
        const val ACCESS_TOKEN = "access_token"

        const val CHECK_IN_MODE = "check_in_mode"
        const val CHECK_IN_MODE_MANUAL = "M"
        const val CHECK_IN_MODE_AUTOMATIC = "A"

        const val LAST_CHECKED_TICKET_ID = "last_checked_ticket_id"

        // DB const
        const val DATABASE_NAME = "doorperson.db"
        const val DATABASE_VERSION = 2

        // Date constants
        const val MIN_TIMESTAMP = "2000-01-01T00:00:00.000000"
        const val MAX_TIMESTAMP = "2100-01-01T00:00:00.000000"
        const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"

        const val SYNC_PAGE_LIMIT = 100
        const val PAGE_LIMIT = 100
    }

    enum class SyncTableName constructor(private val tableName: String) {
        EVENTS("events"),
        TICKETS("tickets");

        override fun toString(): String {
            return this.tableName
        }
    }
}