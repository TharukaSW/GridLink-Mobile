package com.example.gridlink.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.gridlink.app.data.remote.MicrogridNodeDto

/**
 * Handles offline caching and retrieval of solar microgrid station reference data via native SQLite.
 */
class LocalStationCache(context: Context) {
    private val dbHelper = GridLinkDbHelper(context)

    fun cacheStations(stations: List<MicrogridNodeDto>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(GridLinkDbHelper.TABLE_CACHED_STATIONS, null, null)
            val now = System.currentTimeMillis()

            for (s in stations) {
                val values = ContentValues().apply {
                    put(GridLinkDbHelper.COL_STATION_ID, s.id)
                    put(GridLinkDbHelper.COL_STATION_CODE, s.stationCode)
                    put(GridLinkDbHelper.COL_STATION_NAME, s.stationName)
                    put(GridLinkDbHelper.COL_STATION_ADDRESS, s.address)
                    put(GridLinkDbHelper.COL_LATITUDE, s.latitude)
                    put(GridLinkDbHelper.COL_LONGITUDE, s.longitude)
                    put(GridLinkDbHelper.COL_CAPACITY_KWH, s.capacityKWh)
                    put(GridLinkDbHelper.COL_TOTAL_SLOTS, s.totalBatterySlots)
                    put(GridLinkDbHelper.COL_AVAILABLE_SLOTS, s.availableBatterySlots)
                    put(GridLinkDbHelper.COL_OPERATING_HOURS, s.operatingHours)
                    put(GridLinkDbHelper.COL_IS_ACTIVE, if (s.isActive) 1 else 0)
                    put(GridLinkDbHelper.COL_SYNCED_AT, now)
                }
                db.insertWithOnConflict(
                    GridLinkDbHelper.TABLE_CACHED_STATIONS,
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

    fun getCachedStations(): List<MicrogridNodeDto> {
        val list = mutableListOf<MicrogridNodeDto>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            GridLinkDbHelper.TABLE_CACHED_STATIONS,
            null,
            null,
            null,
            null,
            null,
            "${GridLinkDbHelper.COL_STATION_NAME} ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    MicrogridNodeDto(
                        id = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_STATION_ID)),
                        stationCode = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_STATION_CODE)) ?: "",
                        stationName = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_STATION_NAME)) ?: "",
                        address = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_STATION_ADDRESS)) ?: "",
                        latitude = it.getDouble(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_LATITUDE)),
                        longitude = it.getDouble(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_LONGITUDE)),
                        capacityKWh = it.getDouble(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_CAPACITY_KWH)),
                        totalBatterySlots = it.getInt(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_TOTAL_SLOTS)),
                        availableBatterySlots = it.getInt(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_AVAILABLE_SLOTS)),
                        operatingHours = it.getString(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_OPERATING_HOURS)) ?: "",
                        isActive = it.getInt(it.getColumnIndexOrThrow(GridLinkDbHelper.COL_IS_ACTIVE)) == 1
                    )
                )
            }
        }
        if (list.isEmpty()) {
            val defaults = listOf(
                MicrogridNodeDto("node-001", "ST-001", "Colombo Central Solar Hub", "100 Galle Road, Colombo 03", 6.9271, 79.8612, 120.0, 12, 8, "06:00 - 22:00", true),
                MicrogridNodeDto("node-002", "ST-002", "Kandy Solar Microgrid Gateway", "45 Peradeniya Road, Kandy", 7.2906, 80.6337, 85.0, 10, 5, "06:00 - 20:00", true),
                MicrogridNodeDto("node-003", "ST-003", "Galle Coastal Solar Terminal", "12 Matara Road, Galle", 6.0535, 80.2210, 60.0, 8, 6, "07:00 - 19:00", true)
            )
            cacheStations(defaults)
            return defaults
        }
        return list
    }
}
