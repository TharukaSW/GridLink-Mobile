package com.example.gridlink.app.data.remote

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GridLinkApi {

    // --- Authentication & Health ---
    @GET("api/auth/health")
    suspend fun healthCheck(): Response<ApiResponse<String>>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<ApiResponse<AuthResponseDto>>

    @POST("api/auth/register-prosumer")
    suspend fun registerProsumer(@Body body: ProsumerRegisterDto): Response<ApiResponse<UserDto>>

    // --- Prosumer Profile ---
    @GET("api/prosumers/{nic}")
    suspend fun getProsumerProfile(@Path("nic") nic: String): Response<ApiResponse<UserDto>>

    @PUT("api/prosumers/{nic}")
    suspend fun updateProsumerProfile(
        @Path("nic") nic: String,
        @Body body: ProsumerUpdateDto
    ): Response<ApiResponse<UserDto>>

    @PATCH("api/prosumers/{nic}/request-deactivation")
    suspend fun requestDeactivation(@Path("nic") nic: String): Response<ApiResponse<Any>>

    // --- Microgrid Stations ---
    @GET("api/stations")
    suspend fun getAllStations(
        @Query("searchTerm") searchTerm: String? = null,
        @Query("isActive") isActive: Boolean? = null
    ): Response<ApiResponse<List<MicrogridNodeDto>>>

    @GET("api/stations/nearby")
    suspend fun getNearbyStations(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusKm") radiusKm: Double = 50.0,
        @Query("activeOnly") activeOnly: Boolean = true
    ): Response<ApiResponse<List<MicrogridNodeDto>>>

    @GET("api/stations/{id}")
    suspend fun getStationById(@Path("id") id: String): Response<ApiResponse<MicrogridNodeDto>>

    @PATCH("api/stations/{id}/battery-slots")
    suspend fun updateBatterySlots(
        @Path("id") id: String,
        @Body body: UpdateBatterySlotsDto
    ): Response<ApiResponse<MicrogridNodeDto>>

    // --- Energy Slot Reservations ---
    @POST("api/reservations")
    suspend fun createReservation(@Body body: CreateReservationDto): Response<ApiResponse<ReservationResponseDto>>

    @PUT("api/reservations/{id}")
    suspend fun updateReservation(
        @Path("id") id: String,
        @Body body: UpdateReservationDto
    ): Response<ApiResponse<ReservationResponseDto>>

    @PUT("api/reservations/{id}/cancel")
    suspend fun cancelReservation(
        @Path("id") id: String,
        @Body body: CancelReservationDto
    ): Response<ApiResponse<ReservationResponseDto>>

    @GET("api/reservations/my-bookings")
    suspend fun getMyBookings(@Query("status") status: String? = null): Response<ApiResponse<List<ReservationResponseDto>>>

    @GET("api/reservations/{id}")
    suspend fun getReservationById(@Path("id") id: String): Response<ApiResponse<ReservationResponseDto>>

    @GET("api/reservations/dashboard/summary")
    suspend fun getDashboardSummary(@Query("nic") nic: String? = null): Response<ApiResponse<ReservationDashboardSummaryDto>>

    @GET("api/reservations")
    suspend fun getAllReservations(
        @Query("nic") nic: String? = null,
        @Query("stationId") stationId: String? = null,
        @Query("status") status: String? = null,
        @Query("tradingType") tradingType: String? = null,
        @Query("searchTerm") searchTerm: String? = null
    ): Response<ApiResponse<List<ReservationResponseDto>>>

    @POST("api/reservations/verify")
    suspend fun verifyQrCode(@Body qrData: RequestBody): Response<ApiResponse<ReservationResponseDto>>

    @PUT("api/reservations/{id}/finalize")
    suspend fun finalizeReservation(
        @Path("id") id: String,
        @Body body: FinalizeReservationDto
    ): Response<ApiResponse<ReservationResponseDto>>

    @PUT("api/reservations/{id}/approve")
    suspend fun approveReservation(@Path("id") id: String): Response<ApiResponse<ReservationResponseDto>>
}
