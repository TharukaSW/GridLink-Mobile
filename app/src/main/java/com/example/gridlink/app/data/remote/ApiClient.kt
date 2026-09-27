package com.example.gridlink.app.data.remote

import android.content.Context
import com.example.gridlink.app.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient private constructor(private val context: Context) {

    private val sessionManager = SessionManager(context)
    private var cachedBaseUrl: String? = null
    private var cachedApi: GridLinkApi? = null

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()

        // Attach Authorization header if JWT token is stored in SQLite
        sessionManager.getToken()?.let { token ->
            if (token.isNotBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        builder.header("Accept", "application/json")
        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    @Synchronized
    fun getApi(): GridLinkApi {
        val currentUrl = sessionManager.getApiBaseUrl()
        if (cachedApi == null || cachedBaseUrl != currentUrl) {
            cachedBaseUrl = currentUrl
            val retrofit = Retrofit.Builder()
                .baseUrl(currentUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            cachedApi = retrofit.create(GridLinkApi::class.java)
        }
        return cachedApi!!
    }

    suspend fun testConnection(): Boolean {
        return try {
            val response = getApi().healthCheck()
            response.isSuccessful || response.code() in 200..499
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        @Volatile
        private var instance: ApiClient? = null

        fun getInstance(context: Context): ApiClient {
            return instance ?: synchronized(this) {
                instance ?: ApiClient(context.applicationContext).also { instance = it }
            }
        }

        fun createStringRequestBody(content: String): RequestBody {
            return "\"$content\"".toRequestBody("application/json".toMediaTypeOrNull())
        }
    }
}
