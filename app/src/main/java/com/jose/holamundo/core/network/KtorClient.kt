package com.jose.holamundo.core.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor HTTP Client factory with configurable options.
 */
object KtorClient {

    private const val TAG = "KtorClient"
    private const val TIMEOUT_MS = 15_000

    /**
     * Creates a configured HttpClient instance.
     *
     * @param baseUrl The base URL for API requests
     * @param enableLogging Whether to enable request/response logging
     * @return Configured HttpClient
     */
    fun create(
        baseUrl: String,
        enableLogging: Boolean = false
    ): HttpClient {
        return HttpClient(Android) {
            // Engine configuration
            engine {
                connectTimeout = TIMEOUT_MS
                socketTimeout = TIMEOUT_MS
            }

            // JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            // Default request configuration
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            // Logging (only in debug builds)
            if (enableLogging) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d(TAG, message)
                        }
                    }
                    level = LogLevel.ALL
                }
            }
        }
    }
}

