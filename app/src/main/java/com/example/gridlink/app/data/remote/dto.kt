package com.example.gridlink.app.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Standard API wrapper matching ASP.NET Core ApiResponse<T>
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: T? = null,
    @SerializedName("errors") val errors: List<String>? = null
)

// Authentication DTOs
data class LoginRequestDto(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("password") val password: String
)

data class AuthResponseDto(
    @SerializedName("token") val token: String = "",
    @SerializedName("expiration") val expiration: String = "",
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("nic") val nic: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phoneNumber") val phoneNumber: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("role") val role: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// Prosumer DTOs
data class ProsumerRegisterDto(
    @SerializedName("nic") val nic: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("address") val address: String,
    @SerializedName("password") val password: String
)

data class ProsumerUpdateDto(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("address") val address: String
)

// Microgrid Node (Solar Station) DTOs
data class MicrogridNodeDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("stationCode") val stationCode: String = "",
    @SerializedName("stationName") val stationName: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
    @SerializedName("capacityKWh") val capacityKWh: Double = 0.0,
    @SerializedName("totalBatterySlots") val totalBatterySlots: Int = 0,
    @SerializedName("availableBatterySlots") val availableBatterySlots: Int = 0,
    @SerializedName("operatingHours") val operatingHours: String = "",
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("distanceKm") val distanceKm: Double? = null,
    @SerializedName("activeReservationCount") val activeReservationCount: Int = 0
)

data class UpdateBatterySlotsDto(
    @SerializedName("availableBatterySlots") val availableBatterySlots: Int
)

// Reservation DTOs
data class CreateReservationDto(
    @SerializedName("stationId") val stationId: String,
    @SerializedName("slotId") val slotId: String? = null,
    @SerializedName("slotNumber") val slotNumber: Int? = null,
    @SerializedName("reservationDateTime") val reservationDateTime: String,
    @SerializedName("durationMinutes") val durationMinutes: Int = 60,
    @SerializedName("tradingType") val tradingType: String, // "DropOff" or "Charging"
    @SerializedName("requestedEnergyKWh") val requestedEnergyKWh: Double
)

data class UpdateReservationDto(
    @SerializedName("reservationDateTime") val reservationDateTime: String,
    @SerializedName("durationMinutes") val durationMinutes: Int = 60,
    @SerializedName("tradingType") val tradingType: String,
    @SerializedName("requestedEnergyKWh") val requestedEnergyKWh: Double,
    @SerializedName("stationId") val stationId: String? = null
)

data class CancelReservationDto(
    @SerializedName("reason") val reason: String
)

data class FinalizeReservationDto(
    @SerializedName("actualEnergyTransferredKWh") val actualEnergyTransferredKWh: Double,
    @SerializedName("operatorNotes") val operatorNotes: String? = null
)

data class ReservationResponseDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("reservationNumber") val reservationNumber: String = "",
    @SerializedName("userId") val userId: String = "",
    @SerializedName("nic") val nic: String = "",
    @SerializedName("prosumerName") val prosumerName: String = "",
    @SerializedName("prosumerContact") val prosumerContact: String = "",
    @SerializedName("stationId") val stationId: String = "",
    @SerializedName("stationName") val stationName: String = "",
    @SerializedName("slotId") val slotId: String? = null,
    @SerializedName("slotNumber") val slotNumber: Int? = null,
    @SerializedName("reservationDateTime") val reservationDateTime: String = "",
    @SerializedName("durationMinutes") val durationMinutes: Int = 60,
    @SerializedName("tradingType") val tradingType: String = "",
    @SerializedName("requestedEnergyKWh") val requestedEnergyKWh: Double = 0.0,
    @SerializedName("status") val status: String = "",
    @SerializedName("qrCodeData") val qrCodeData: String = "",
    @SerializedName("qrCodeImageBase64") val qrCodeImageBase64: String? = null,
    @SerializedName("cancellationReason") val cancellationReason: String? = null,
    @SerializedName("cancelledAt") val cancelledAt: String? = null,
    @SerializedName("cancelledBy") val cancelledBy: String? = null,
    @SerializedName("finalizedAt") val finalizedAt: String? = null,
    @SerializedName("finalizedBy") val finalizedBy: String? = null,
    @SerializedName("actualEnergyTransferredKWh") val actualEnergyTransferredKWh: Double? = null,
    @SerializedName("operatorNotes") val operatorNotes: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class ReservationDashboardSummaryDto(
    @SerializedName("totalReservations") val totalReservations: Long = 0,
    @SerializedName("pendingCount") val pendingCount: Long = 0,
    @SerializedName("approvedFutureCount") val approvedFutureCount: Long = 0,
    @SerializedName("completedCount") val completedCount: Long = 0,
    @SerializedName("cancelledCount") val cancelledCount: Long = 0,
    @SerializedName("activeCount") val activeCount: Long = 0
)
