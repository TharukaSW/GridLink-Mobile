package com.example.gridlink.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Native Android SQLiteOpenHelper implementation for local persistence.
 * Complies strictly with the assignment requirement: Pure native Android with local SQLite database (no frameworks).
 * Stores:
 * 1. User authentication session and profile data
 * 2. Cached solar microgrid stations (reference data)
 * 3. Cached energy reservations
 * 4. Application settings (API server base URL)
 */
class GridLinkDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "gridlink_local.db"
        const val DATABASE_VERSION = 1

        // Table: user_session
        const val TABLE_USER_SESSION = "user_session"
        const val COL_SESSION_ID = "id"
        const val COL_USER_ID = "user_id"
        const val COL_NIC = "nic"
        const val COL_FULL_NAME = "full_name"
        const val COL_EMAIL = "email"
        const val COL_PHONE = "phone_number"
        const val COL_ADDRESS = "address"
        const val COL_ROLE = "role"
        const val COL_STATUS = "status"
        const val COL_TOKEN = "jwt_token"
        const val COL_EXPIRATION = "expiration"
        const val COL_LOGGED_IN_AT = "logged_in_at"

        // Table: cached_stations
        const val TABLE_CACHED_STATIONS = "cached_stations"
        const val COL_STATION_ID = "station_id"
        const val COL_STATION_CODE = "station_code"
        const val COL_STATION_NAME = "station_name"
        const val COL_STATION_ADDRESS = "address"
        const val COL_LATITUDE = "latitude"
        const val COL_LONGITUDE = "longitude"
        const val COL_CAPACITY_KWH = "capacity_kwh"
        const val COL_TOTAL_SLOTS = "total_slots"
        const val COL_AVAILABLE_SLOTS = "available_slots"
        const val COL_OPERATING_HOURS = "operating_hours"
        const val COL_IS_ACTIVE = "is_active"
        const val COL_SYNCED_AT = "synced_at"

        // Table: cached_reservations
        const val TABLE_CACHED_RESERVATIONS = "cached_reservations"
        const val COL_RES_ID = "reservation_id"
        const val COL_RES_NUMBER = "reservation_number"
        const val COL_RES_USER_ID = "user_id"
        const val COL_RES_NIC = "nic"
        const val COL_RES_PROSUMER_NAME = "prosumer_name"
        const val COL_RES_STATION_ID = "station_id"
        const val COL_RES_STATION_NAME = "station_name"
        const val COL_RES_DATETIME = "reservation_datetime"
        const val COL_RES_DURATION = "duration_minutes"
        const val COL_RES_TRADING_TYPE = "trading_type"
        const val COL_RES_ENERGY = "requested_energy_kwh"
        const val COL_RES_STATUS = "status"
        const val COL_RES_QR_DATA = "qr_data"
        const val COL_RES_ACTUAL_ENERGY = "actual_energy_kwh"

        // Table: app_settings
        const val TABLE_SETTINGS = "app_settings"
        const val COL_SETTING_KEY = "setting_key"
        const val COL_SETTING_VALUE = "setting_value"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create user session table
        val createUserSessionTable = """
            CREATE TABLE $TABLE_USER_SESSION (
                $COL_SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT,
                $COL_NIC TEXT NOT NULL,
                $COL_FULL_NAME TEXT,
                $COL_EMAIL TEXT,
                $COL_PHONE TEXT,
                $COL_ADDRESS TEXT,
                $COL_ROLE TEXT NOT NULL,
                $COL_STATUS TEXT,
                $COL_TOKEN TEXT NOT NULL,
                $COL_EXPIRATION TEXT,
                $COL_LOGGED_IN_AT INTEGER
            )
        """.trimIndent()
        db.execSQL(createUserSessionTable)

        // Create cached stations table
        val createStationsTable = """
            CREATE TABLE $TABLE_CACHED_STATIONS (
                $COL_STATION_ID TEXT PRIMARY KEY,
                $COL_STATION_CODE TEXT,
                $COL_STATION_NAME TEXT,
                $COL_STATION_ADDRESS TEXT,
                $COL_LATITUDE REAL,
                $COL_LONGITUDE REAL,
                $COL_CAPACITY_KWH REAL,
                $COL_TOTAL_SLOTS INTEGER,
                $COL_AVAILABLE_SLOTS INTEGER,
                $COL_OPERATING_HOURS TEXT,
                $COL_IS_ACTIVE INTEGER,
                $COL_SYNCED_AT INTEGER
            )
        """.trimIndent()
        db.execSQL(createStationsTable)

        // Create cached reservations table
        val createReservationsTable = """
            CREATE TABLE $TABLE_CACHED_RESERVATIONS (
                $COL_RES_ID TEXT PRIMARY KEY,
                $COL_RES_NUMBER TEXT,
                $COL_RES_USER_ID TEXT,
                $COL_RES_NIC TEXT,
                $COL_RES_PROSUMER_NAME TEXT,
                $COL_RES_STATION_ID TEXT,
                $COL_RES_STATION_NAME TEXT,
                $COL_RES_DATETIME TEXT,
                $COL_RES_DURATION INTEGER,
                $COL_RES_TRADING_TYPE TEXT,
                $COL_RES_ENERGY REAL,
                $COL_RES_STATUS TEXT,
                $COL_RES_QR_DATA TEXT,
                $COL_RES_ACTUAL_ENERGY REAL,
                $COL_SYNCED_AT INTEGER
            )
        """.trimIndent()
        db.execSQL(createReservationsTable)

        // Create settings table
        val createSettingsTable = """
            CREATE TABLE $TABLE_SETTINGS (
                $COL_SETTING_KEY TEXT PRIMARY KEY,
                $COL_SETTING_VALUE TEXT
            )
        """.trimIndent()
        db.execSQL(createSettingsTable)

        // Insert default base URL for local development (10.0.2.2 maps to host machine in Android emulator)
        val defaultUrlValues = ContentValues().apply {
            put(COL_SETTING_KEY, "api_base_url")
            put(COL_SETTING_VALUE, "http://10.0.2.2:5015/")
        }
        db.insert(TABLE_SETTINGS, null, defaultUrlValues)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_SESSION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CACHED_STATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CACHED_RESERVATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
    }
}
