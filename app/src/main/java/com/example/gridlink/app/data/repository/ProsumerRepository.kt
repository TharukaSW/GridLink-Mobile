package com.example.gridlink.app.data.repository

import android.content.Context
import com.example.gridlink.app.data.local.SessionManager
import com.example.gridlink.app.data.remote.ApiClient
import com.example.gridlink.app.data.remote.ProsumerUpdateDto
import com.example.gridlink.app.data.remote.UserDto

class ProsumerRepository(private val context: Context) {
    private val apiClient = ApiClient.getInstance(context)
    private val sessionManager = SessionManager(context)

    suspend fun getProfile(nic: String): Resource<UserDto> {
        return try {
            val response = apiClient.getApi().getProsumerProfile(nic)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Resource.Success(body.data, body.message)
                } else {
                    Resource.Error(body.message.ifBlank { "Could not fetch profile." })
                }
            } else {
                Resource.Error("Error fetching profile: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error fetching profile.")
        }
    }

    suspend fun updateProfile(
        nic: String,
        fullName: String,
        email: String,
        phone: String,
        address: String
    ): Resource<UserDto> {
        return try {
            val dto = ProsumerUpdateDto(
                fullName = fullName.trim(),
                email = email.trim(),
                phoneNumber = phone.trim(),
                address = address.trim()
            )
            val response = apiClient.getApi().updateProsumerProfile(nic, dto)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    // Update local SQLite user session with the new profile info
                    sessionManager.updateProfile(fullName, phone, address)
                    Resource.Success(body.data, body.message)
                } else {
                    Resource.Error(body.message.ifBlank { "Profile update failed." })
                }
            } else {
                Resource.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update profile.")
        }
    }

    suspend fun requestDeactivation(nic: String): Resource<String> {
        return try {
            val response = apiClient.getApi().requestDeactivation(nic)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    sessionManager.updateStatus("DeactivationRequested")
                    Resource.Success(body.message, body.message)
                } else {
                    Resource.Error(body.message.ifBlank { "Failed to request deactivation." })
                }
            } else {
                Resource.Error("Deactivation request failed (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error submitting deactivation request.")
        }
    }
}
