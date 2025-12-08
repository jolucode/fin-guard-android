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
 */
interface NotificationApi {
    suspend fun sendNotification(message: String): Result<Long>
    suspend fun getNotificationLogs(): Result<List<NotificationLogDto>>
}

/**
 * Implementation of NotificationApi using Ktor.
 */
class NotificationApiImpl @Inject constructor(
    private val client: HttpClient
) : NotificationApi {

    override suspend fun sendNotification(message: String): Result<Long> {
        return runCatching {
            val response = client.post(AppConfig.notificationsEndpoint) {
                setBody(NotificationRequestDto(message))
            }
            if (response.status.isSuccess()) {
                response.bodyAsText().toLongOrNull() ?: 0L
            } else {
                throw Exception("Error sending notification: ${response.status}")
            }
        }
    }

    override suspend fun getNotificationLogs(): Result<List<NotificationLogDto>> {
        return runCatching {
            client.get(AppConfig.notificationsEndpoint).body()
        }
    }
}
