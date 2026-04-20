@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.aropi.app.logic.bundle

import android.content.Context
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Centralised Retrofit/OkHttp factory for the Aropi backend.
 *
 * Base URL is configurable via SharedPreferences key [BASE_URL_KEY] so
 * the app can be pointed at `http://10.0.2.2:5000` (emulator), a LAN
 * host, or a production HTTPS endpoint without rebuilding.
 */
object AropiNetwork {

    const val PREFS = "aropi_network"
    const val BASE_URL_KEY = "backend_base_url"

    /** Default points at the Android-emulator alias for the host machine. */
    const val DEFAULT_BASE_URL = "http://10.0.2.2:5000/"

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    fun getBaseUrl(context: Context): String {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(BASE_URL_KEY, null)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_BASE_URL
        return if (raw.endsWith("/")) raw else "$raw/"
    }

    fun setBaseUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(BASE_URL_KEY, url)
            .apply()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun api(context: Context): AropiApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(getBaseUrl(context))
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AropiApi::class.java)
    }
}
