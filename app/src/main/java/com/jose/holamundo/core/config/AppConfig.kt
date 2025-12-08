package com.jose.holamundo.core.config

import com.jose.holamundo.BuildConfig

/**
 * Centralized configuration object for the application.
 * Provides access to BuildConfig values in a type-safe way.
 */
object AppConfig {
    val baseUrl: String = BuildConfig.BASE_URL
    val apiVersion: String = BuildConfig.API_VERSION
    val enableLogs: Boolean = BuildConfig.ENABLE_LOGS

    val fullApiUrl: String get() = "$baseUrl/api"
    val notificationsEndpoint: String get() = "$fullApiUrl/notifications"
}

