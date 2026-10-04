package com.example.gridlink.app.data.repository

import android.content.Context
import com.example.gridlink.app.data.local.LocalStationCache
import com.example.gridlink.app.data.remote.ApiClient
import com.example.gridlink.app.data.remote.MicrogridNodeDto
import com.example.gridlink.app.data.remote.UpdateBatterySlotsDto

class StationRepository(private val context: Context) {
    private val apiClient = ApiClient.getInstance(context)
    private val localCache = LocalStationCache(context)

    suspend fun getAllStations(searchTerm: String? = null, isActive: Boolean? = true): Resource<List<MicrogridNodeDto>> {
        return try {
            val response = apiClient.getApi().getAllStations(searchTerm, isActive)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    // Cache in SQLite
                    localCache.cacheStations(body.data)
                    Resource.Success(body.data, body.message)
                } else {
                    val cached = localCache.getCachedStations()
                    if (cached.isNotEmpty()) {
                        Resource.Success(cached, "Loaded from local SQLite cache.")
                    } else {
                        Resource.Error(body.message.ifBlank { "Failed to load stations." })
                    }
                }
            } else {
                val cached = localCache.getCachedStations()
                if (cached.isNotEmpty()) {
                    Resource.Success(cached, "Server error (${response.code()}). Loaded from local cache.")
                } else {
                    Resource.Error("Error loading stations: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            val cached = localCache.getCachedStations()
            if (cached.isNotEmpty()) {
                Resource.Success(cached, "Offline mode: Loaded from local SQLite cache.")
            } else {
                Resource.Error(e.localizedMessage ?: "Network error loading stations.")
            }
        }
    }

    suspend fun getNearbyStations(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 50.0,
        activeOnly: Boolean = true
    ): Resource<List<MicrogridNodeDto>> {
        return try {
            val response = apiClient.getApi().getNearbyStations(latitude, longitude, radiusKm, activeOnly)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    Resource.Error(body.message.ifBlank { "No nearby stations found." })
                }
            } else {
                Resource.Error("Error querying nearby stations: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to query nearby stations.")
        }
    }

    suspend fun updateBatterySlots(stationId: String, availableSlots: Int): Resource<MicrogridNodeDto> {
        return try {
            val response = apiClient.getApi().updateBatterySlots(stationId, UpdateBatterySlotsDto(availableSlots))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    Resource.Error(body.message.ifBlank { "Could not update battery slots." })
                }
            } else {
                Resource.Error("Failed to update battery slots: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error updating battery slots.")
        }
    }

    fun getLocalCachedStations(): List<MicrogridNodeDto> {
        return localCache.getCachedStations()
    }
}
