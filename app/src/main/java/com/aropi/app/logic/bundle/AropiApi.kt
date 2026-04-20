package com.aropi.app.logic.bundle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Retrofit contract for the Aropi backend.
 *
 * Base URL is `http://<host>:5000/` (trailing slash required by Retrofit).
 * See `AropiBackend/ARCHITECTURE.md` §4 for full endpoint documentation.
 */
interface AropiApi {

    @GET("bundle/latest")
    suspend fun latestBundle(): BundlePointer

    /** Streaming download — caller must close the body. */
    @GET
    @Streaming
    suspend fun downloadBundle(@Url url: String): ResponseBody

    @GET("health")
    suspend fun health(): Map<String, String>

    @POST("compose")
    suspend fun compose(@Body request: ComposeRequest): ComposeResponse
}

@Serializable
data class BundlePointer(
    val version: String,
    val filename: String,
    val url: String,
    val size: Long,
    val sha256: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class ComposeRequest(
    val pictos: List<String>,
    val language: String = "ca"
)

@Serializable
data class ComposeResponse(
    val input: String? = null,
    val output: String,
    val pictos: List<String> = emptyList(),
    val language: String = "ca",
    val cached: Boolean = false
)
