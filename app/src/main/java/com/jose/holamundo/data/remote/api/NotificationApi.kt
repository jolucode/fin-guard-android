package com.jose.holamundo.data.remote.api

import com.jose.holamundo.core.config.AppConfig
import com.jose.holamundo.data.remote.dto.NotificationLogDto
import com.jose.holamundo.data.remote.dto.NotificationRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import javax.inject.Inject

/**
 * API interface for notification-related endpoints.
 * Updated for WebFlux backend with MongoDB (returns String ObjectId).
 */
interface NotificationApi {
    suspend fun sendNotification(message: String, deviceId: String? = null): Result<String>
    suspend fun getNotificationLogs(deviceId: String? = null): Result<List<NotificationLogDto>>
}

/**
 * Implementation of NotificationApi using Ktor.
 * Compatible with Spring WebFlux reactive backend.
 */
class NotificationApiImpl @Inject constructor(
    private val client: HttpClient
) : NotificationApi {

    override suspend fun sendNotification(message: String, deviceId: String?): Result<String> {
        return runCatching {
            val response = client.post(AppConfig.notificationsEndpoint) {
                setBody(NotificationRequestDto(message, deviceId))
            }
            if (response.status.isSuccess()) {
                // Backend returns MongoDB ObjectId as String
                response.bodyAsText().trim().removeSurrounding("\"")
            } else {
                throw Exception("Error sending notification: ${response.status}")
            }
        }
    }

    override suspend fun getNotificationLogs(deviceId: String?): Result<List<NotificationLogDto>> {
        return runCatching {
            val url = if (deviceId != null) {
                "${AppConfig.notificationsEndpoint}?deviceId=$deviceId"
            } else {
                AppConfig.notificationsEndpoint
            }
            client.get(url).body()
        }
    }
}
