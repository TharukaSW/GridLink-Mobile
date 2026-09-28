package com.example.gridlink.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

data class UserSession(
    val userId: String,
    val nic: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val role: String,
    val status: String,
    val token: String,
    val expiration: String
)

/**
 * Manages user authentication session and app configuration in local SQLite database.
 */
class SessionManager(context: Context) {
    private val dbHelper = GridLinkDbHelper(context)

    fun saveSession(session: UserSession) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Clear existing session
            db.delete(GridLinkDbHelper.TABLE_USER_SESSION, null, null)

            val values = ContentValues().apply {
                put(GridLinkDbHelper.COL_USER_ID, session.userId)
                put(GridLinkDbHelper.COL_NIC, session.nic)
                put(GridLinkDbHelper.COL_FULL_NAME, session.fullName)
                put(GridLinkDbHelper.COL_EMAIL, session.email)
                put(GridLinkDbHelper.COL_PHONE, session.phoneNumber)
                put(GridLinkDbHelper.COL_ADDRESS, session.address)
                put(GridLinkDbHelper.COL_ROLE, session.role)
                put(GridLinkDbHelper.COL_STATUS, session.status)
                put(GridLinkDbHelper.COL_TOKEN, session.token)
                put(GridLinkDbHelper.COL_EXPIRATION, session.expiration)
                put(GridLinkDbHelper.COL_LOGGED_IN_AT, System.currentTimeMillis())
            }
            db.insert(GridLinkDbHelper.TABLE_USER_SESSION, null, values)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getSession(): UserSession? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            GridLinkDbHelper.TABLE_USER_SESSION,
            null,
            null,
            null,
            null,
            null,
            "${GridLinkDbHelper.COL_SESSION_ID} DESC",
            "1"
        )

        return cursor.use {
            if (it.moveToFirst()) {
                val userId = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_USER_ID)) ?: ""
                val nic = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_NIC)) ?: ""
                val fullName = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_FULL_NAME)) ?: ""
                val email = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_EMAIL)) ?: ""
                val phone = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_PHONE)) ?: ""
                val address = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_ADDRESS)) ?: ""
                val role = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_ROLE)) ?: ""
                val status = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_STATUS)) ?: ""
                val token = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_TOKEN)) ?: ""
                val expiration = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_EXPIRATION)) ?: ""

                UserSession(
                    userId = userId,
                    nic = nic,
                    fullName = fullName,
                    email = email,
                    phoneNumber = phone,
                    address = address,
                    role = role,
                    status = status,
                    token = token,
                    expiration = expiration
                )
            } else {
                null
            }
        }
    }

    fun getToken(): String? {
        return getSession()?.token
    }

    fun isLoggedIn(): Boolean {
        val session = getSession()
        return session != null && session.token.isNotBlank()
    }

    fun getUserRole(): String? {
        return getSession()?.role
    }

    fun getUserNic(): String? {
        return getSession()?.nic
    }

    fun updateProfile(fullName: String, phoneNumber: String, address: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(GridLinkDbHelper.COL_FULL_NAME, fullName)
            put(GridLinkDbHelper.COL_PHONE, phoneNumber)
            put(GridLinkDbHelper.COL_ADDRESS, address)
        }
        db.update(GridLinkDbHelper.TABLE_USER_SESSION, values, null, null)
    }

    fun updateStatus(status: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(GridLinkDbHelper.COL_STATUS, status)
        }
        db.update(GridLinkDbHelper.TABLE_USER_SESSION, values, null, null)
    }

    fun clearSession() {
        val db = dbHelper.writableDatabase
        db.delete(GridLinkDbHelper.TABLE_USER_SESSION, null, null)
        db.delete(GridLinkDbHelper.TABLE_CACHED_RESERVATIONS, null, null)
    }

    fun getApiBaseUrl(): String {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            GridLinkDbHelper.TABLE_SETTINGS,
            arrayOf(GridLinkDbHelper.COL_SETTING_VALUE),
            "${GridLinkDbHelper.COL_SETTING_KEY} = ?",
            arrayOf("api_base_url"),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                val url = it.getString(0)
                if (url.isNullOrBlank()) "http://10.0.2.2:5015/" else url
            } else {
                "http://10.0.2.2:5015/"
            }
        }
    }

    fun setApiBaseUrl(url: String) {
        val formattedUrl = if (url.endsWith("/")) url else "$url/"
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(GridLinkDbHelper.COL_SETTING_KEY, "api_base_url")
            put(GridLinkDbHelper.COL_SETTING_VALUE, formattedUrl)
        }
        db.insertWithOnConflict(
            GridLinkDbHelper.TABLE_SETTINGS,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }
}
