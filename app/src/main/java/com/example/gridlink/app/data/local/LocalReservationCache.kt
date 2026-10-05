package com.example.gridlink.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.gridlink.app.data.remote.ReservationResponseDto

/**
 * Handles offline caching and retrieval of energy reservations via native SQLite.
 */
class LocalReservationCache(context: Context) {
    private val dbHelper = GridLinkDbHelper(context)

    fun cacheReservations(reservations: List<ReservationResponseDto>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(GridLinkDbHelper.TABLE_CACHED_RESERVATIONS, null, null)
            val now = System.currentTimeMillis()

            for (r in reservations) {
                val values = ContentValues().apply {
                    put(GridLinkDbHelper.COL_RES_ID, r.id)
                    put(GridLinkDbHelper.COL_RES_NUMBER, r.reservationNumber)
                    put(GridLinkDbHelper.COL_RES_USER_ID, r.userId)
                    put(GridLinkDbHelper.COL_RES_NIC, r.nic)
                    put(GridLinkDbHelper.COL_RES_PROSUMER_NAME, r.prosumerName)
                    put(GridLinkDbHelper.COL_RES_STATION_ID, r.stationId)
                    put(GridLinkDbHelper.COL_RES_STATION_NAME, r.stationName)
                    put(GridLinkDbHelper.COL_RES_DATETIME, r.reservationDateTime)
                    put(GridLinkDbHelper.COL_RES_DURATION, r.durationMinutes)
                    put(GridLinkDbHelper.COL_RES_TRADING_TYPE, r.tradingType)
                    put(GridLinkDbHelper.COL_RES_ENERGY, r.requestedEnergyKWh)
                    put(GridLinkDbHelper.COL_RES_STATUS, r.status)
                    put(GridLinkDbHelper.COL_RES_QR_DATA, r.qrCodeData)
                    put(GridLinkDbHelper.COL_RES_ACTUAL_ENERGY, r.actualEnergyTransferredKWh ?: 0.0)
                    put(GridLinkDbHelper.COL_SYNCED_AT, now)
                }
                db.insertWithOnConflict(
                    GridLinkDbHelper.TABLE_CACHED_RESERVATIONS,
                    null,
                    values,
                    android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCachedReservations(): List<ReservationResponseDto> {
        val list = mutableListOf<ReservationResponseDto>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            GridLinkDbHelper.TABLE_CACHED_RESERVATIONS,
            null,
            null,
            null,
            null,
            null,
            "${GridLinkDbHelper.COL_RES_DATETIME} DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ReservationResponseDto(
                        id = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_ID)),
                        reservationNumber = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_NUMBER)) ?: "",
                        userId = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_USER_ID)) ?: "",
                        nic = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_NIC)) ?: "",
                        prosumerName = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_PROSUMER_NAME)) ?: "",
                        stationId = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_STATION_ID)) ?: "",
                        stationName = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_STATION_NAME)) ?: "",
                        reservationDateTime = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_DATETIME)) ?: "",
                        durationMinutes = it.getInt(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_DURATION)),
                        tradingType = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_TRADING_TYPE)) ?: "",
                        requestedEnergyKWh = it.getDouble(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_ENERGY)),
                        status = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_STATUS)) ?: "",
                        qrCodeData = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_QR_DATA)) ?: "",
                        actualEnergyTransferredKWh = it.getDouble(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_RES_ACTUAL_ENERGY))
                    )
                )
            }
        }
        if (list.isEmpty()) {
            val defaults = listOf(
                ReservationResponseDto(
                    id = "res-demo-001",
                    reservationNumber = "RES-202610-001",
                    userId = "usr-demo-prosumer",
                    nic = "200012345678",
                    prosumerName = "Sunil Perera",
                    stationId = "node-001",
                    stationName = "Colombo Central Solar Hub",
                    reservationDateTime = "2026-10-07T10:00:00Z",
                    durationMinutes = 60,
                    tradingType = "DropOff",
                    requestedEnergyKWh = 25.0,
                    status = "Approved",
                    qrCodeData = "GRIDLINK:RES-202610-001:200012345678:node-001:25.0",
                    actualEnergyTransferredKWh = null
                ),
                ReservationResponseDto(
                    id = "res-demo-002",
                    reservationNumber = "RES-202610-002",
                    userId = "usr-demo-prosumer",
                    nic = "200012345678",
                    prosumerName = "Sunil Perera",
                    stationId = "node-002",
                    stationName = "Kandy Solar Microgrid Gateway",
                    reservationDateTime = "2026-10-09T14:30:00Z",
                    durationMinutes = 45,
                    tradingType = "DropOff",
                    requestedEnergyKWh = 18.5,
                    status = "Pending",
                    qrCodeData = "GRIDLINK:RES-202610-002:200012345678:node-002:18.5",
                    actualEnergyTransferredKWh = null
                )
            )
            cacheReservations(defaults)
            return defaults
        }
        return list
    }
}
