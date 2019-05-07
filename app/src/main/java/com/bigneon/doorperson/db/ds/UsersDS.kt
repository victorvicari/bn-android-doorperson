package com.bigneon.doorperson.db.ds

import android.content.ContentValues
import android.database.Cursor
import com.bigneon.doorperson.db.dml.TableUsersDML
import com.bigneon.doorperson.rest.model.UserModel

/****************************************************
 * Copyright (c) 2016 - 2019.
 * All right reserved!
 * Created by SRKI-ST on 25.04.2019..
 ****************************************************/
class UsersDS : BaseDS() {
    init {
        open()
    }

    private val allColumns = arrayOf(
        TableUsersDML.USER_ID,
        TableUsersDML.FIRST_NAME,
        TableUsersDML.LAST_NAME,
        TableUsersDML.EMAIL,
        TableUsersDML.PHONE,
        TableUsersDML.PROFILE_PIC_URL
    )

    fun getUser(userId: String): UserModel? {
        database?.query(
            TableUsersDML.TABLE_USERS,
            allColumns,
            TableUsersDML.USER_ID + " = '" + userId + "'",
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                val ticket = cursorToUser(it)
                it.close()
                return ticket
            }
        } ?: return null
        return null
    }

    fun userExists(userId: String): Boolean {
        database?.rawQuery(
            "select count(*) from " + TableUsersDML.TABLE_USERS + " where " + TableUsersDML.USER_ID + " = '" + userId + "'",
            null
        )?.use {
            if (it.moveToFirst()) {
                val count = it.getInt(0)
                it.close()
                return count > 0
            }
        } ?: return false
        return false
    }

    fun createUser(
        userId: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        profilePicURL: String
    ) {
        val values = ContentValues()
        values.put(TableUsersDML.USER_ID, userId)
        values.put(TableUsersDML.FIRST_NAME, firstName)
        values.put(TableUsersDML.LAST_NAME, lastName)
        values.put(TableUsersDML.EMAIL, email)
        values.put(TableUsersDML.PHONE, phone)
        values.put(TableUsersDML.PROFILE_PIC_URL, profilePicURL)

        database?.insert(TableUsersDML.TABLE_USERS, null, values)
    }

    fun updateUser(
        userId: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        profilePicURL: String
    ) {
        val values = ContentValues()
        values.put(TableUsersDML.FIRST_NAME, firstName)
        values.put(TableUsersDML.LAST_NAME, lastName)
        values.put(TableUsersDML.EMAIL, email)
        values.put(TableUsersDML.PHONE, phone)
        values.put(TableUsersDML.PROFILE_PIC_URL, profilePicURL)

        database?.update(
            TableUsersDML.TABLE_USERS,
            values,
            TableUsersDML.USER_ID + " = '" + userId + "'",
            null
        )
    }

    fun deleteUser(userId: String) {
        println("UserModel deleted with pk: $userId")
        database?.delete(TableUsersDML.TABLE_USERS, TableUsersDML.USER_ID + " = '" + userId + "'", null)
    }

    private fun cursorToUser(cursor: Cursor): UserModel {
        val userModel = UserModel()
        var index = 0
        userModel.userId = cursor.getString(index++)
        userModel.firstName = cursor.getString(index++)
        userModel.lastName = cursor.getString(index++)
        userModel.email = cursor.getString(index++)
        userModel.phone = cursor.getString(index++)
        userModel.profilePicURL = cursor.getString(index)
        return userModel
    }
}