package com.example.gridlink.app.data.repository

import android.content.Context
import com.example.gridlink.app.data.local.LocalReservationCache
import com.example.gridlink.app.data.remote.ApiClient
import com.example.gridlink.app.data.remote.CancelReservationDto
import com.example.gridlink.app.data.remote.CreateReservationDto
import com.example.gridlink.app.data.remote.FinalizeReservationDto
import com.example.gridlink.app.data.remote.ReservationDashboardSummaryDto
import com.example.gridlink.app.data.remote.ReservationResponseDto
import com.example.gridlink.app.data.remote.UpdateReservationDto

class ReservationRepository(private val context: Context) {
    private val apiClient = ApiClient.getInstance(context)
    private val localCache = LocalReservationCache(context)

    suspend fun createReservation(dto: CreateReservationDto): Resource<ReservationResponseDto> {
        return try {
            val response = apiClient.getApi().createReservation(dto)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    val errMsg = body.message.ifBlank {
                        body.errors?.joinToString(", ") ?: "Failed to create reservation."
                    }
                    Resource.Error(errMsg)
                }
            } else {
                val errBody = response.errorBody()?.string() ?: "Server error (${response.code()})"
                Resource.Error(errBody)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error creating reservation.")
        }
    }

    suspend fun updateReservation(id: String, dto: UpdateReservationDto): Resource<ReservationResponseDto> {
        return try {
            val response = apiClient.getApi().updateReservation(id, dto)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    val errMsg = body.message.ifBlank {
                        body.errors?.joinToString(", ") ?: "Update reservation failed."
                    }
                    Resource.Error(errMsg)
                }
            } else {
                val errBody = response.errorBody()?.string() ?: "Failed to update reservation (${response.code()})"
                Resource.Error(errBody)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error updating reservation.")
        }
    }

    suspend fun cancelReservation(id: String, reason: String): Resource<ReservationResponseDto> {
        return try {
            val response = apiClient.getApi().cancelReservation(id, CancelReservationDto(reason.trim()))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    val errMsg = body.message.ifBlank {
                        body.errors?.joinToString(", ") ?: "Cancellation failed."
                    }
                    Resource.Error(errMsg)
                }
            } else {
                val errBody = response.errorBody()?.string() ?: "Failed to cancel reservation (${response.code()})"
                Resource.Error(errBody)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error cancelling reservation.")
        }
    }

    suspend fun getMyBookings(status: String? = null): Resource<List<ReservationResponseDto>> {
        return try {
            val response = apiClient.getApi().getMyBookings(status)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    localCache.cacheReservations(body.data)
                    Resource.Success(body.data, body.message)
                } else {
                    val cached = localCache.getCachedReservations()
                    if (cached.isNotEmpty()) {
                        Resource.Success(cached, "Loaded from local cache.")
                    } else {
                        Resource.Error(body.message.ifBlank { "Could not load bookings." })
                    }
                }
            } else {
                val cached = localCache.getCachedReservations()
                if (cached.isNotEmpty()) {
                    Resource.Success(cached, "Server error (${response.code()}). Loaded from local cache.")
                } else {
                    Resource.Error("Error loading bookings (${response.code()})")
                }
            }
        } catch (e: Exception) {
            val cached = localCache.getCachedReservations()
            if (cached.isNotEmpty()) {
                Resource.Success(cached, "Offline mode: Loaded from local SQLite cache.")
            } else {
                Resource.Error(e.localizedMessage ?: "Network error loading bookings.")
            }
        }
    }

    suspend fun getReservationById(id: String): Resource<ReservationResponseDto> {
        return try {
            val response = apiClient.getApi().getReservationById(id)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    val cached = localCache.getCachedReservations().find { it.id == id }
                    if (cached != null) Resource.Success(cached) else Resource.Error(body.message.ifBlank { "Reservation not found." })
                }
            } else {
                val cached = localCache.getCachedReservations().find { it.id == id }
                if (cached != null) Resource.Success(cached) else Resource.Error("Reservation not found (${response.code()})")
            }
        } catch (e: Exception) {
            val cached = localCache.getCachedReservations().find { it.id == id }
            if (cached != null) {
                Resource.Success(cached, "Offline mode: Loaded from local SQLite cache.")
            } else {
                Resource.Error(e.localizedMessage ?: "Network error loading reservation.")
            }
        }
    }

    suspend fun getDashboardSummary(nic: String? = null): Resource<ReservationDashboardSummaryDto> {
        return try {
            val response = apiClient.getApi().getDashboardSummary(nic)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    Resource.Error(body.message.ifBlank { "Could not fetch dashboard stats." })
                }
            } else {
                Resource.Error("Error fetching stats (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error fetching dashboard stats.")
        }
    }

    suspend fun getAllReservations(
        nic: String? = null,
        stationId: String? = null,
        status: String? = null,
        tradingType: String? = null,
        searchTerm: String? = null
    ): Resource<List<ReservationResponseDto>> {
        return try {
            val response = apiClient.getApi().getAllReservations(nic, stationId, status, tradingType, searchTerm)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    localCache.cacheReservations(body.data)
                    Resource.Success(body.data, body.message)
                } else {
                    val cached = localCache.getCachedReservations()
                    if (cached.isNotEmpty()) Resource.Success(cached) else Resource.Error(body.message.ifBlank { "Failed to load reservations." })
                }
            } else {
                val cached = localCache.getCachedReservations()
                if (cached.isNotEmpty()) Resource.Success(cached) else Resource.Error("Error loading reservations (${response.code()})")
            }
        } catch (e: Exception) {
            val cached = localCache.getCachedReservations()
            if (cached.isNotEmpty()) {
                Resource.Success(cached, "Offline mode: Loaded from local cache.")
            } else {
                Resource.Error(e.localizedMessage ?: "Network error loading reservations.")
            }
        }
    }

    suspend fun verifyQrCode(qrData: String): Resource<ReservationResponseDto> {
        return try {
            val requestBody = ApiClient.createStringRequestBody(qrData.trim())
            val response = apiClient.getApi().verifyQrCode(requestBody)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    Resource.Error(body.message.ifBlank { "Invalid or unverified QR code." })
                }
            } else {
                Resource.Error("QR verification failed (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error verifying QR code.")
        }
    }

    suspend fun finalizeReservation(
        id: String,
        actualEnergyKWh: Double,
        operatorNotes: String?
    ): Resource<ReservationResponseDto> {
        return try {
            val dto = FinalizeReservationDto(actualEnergyKWh, operatorNotes)
            val response = apiClient.getApi().finalizeReservation(id, dto)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    Resource.Error(body.message.ifBlank { "Failed to finalize energy transfer." })
                }
            } else {
                Resource.Error("Failed to finalize (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error finalizing energy transfer.")
        }
    }
}
