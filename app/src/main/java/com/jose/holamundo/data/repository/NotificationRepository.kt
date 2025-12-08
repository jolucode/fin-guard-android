package com.jose.holamundo.data.repository

import com.jose.holamundo.data.remote.api.NotificationApi
import com.jose.holamundo.domain.model.NotificationLog
import com.jose.holamundo.domain.model.ParsedYapeData
import javax.inject.Inject

/**
 * Repository interface for notification data.
 */
interface NotificationRepository {
    suspend fun sendNotification(message: String): Result<Long>
    suspend fun getNotificationLogs(): Result<List<NotificationLog>>
}

/**
 * Implementation of NotificationRepository.
 */
class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi
) : NotificationRepository {

    override suspend fun sendNotification(message: String): Result<Long> {
        return api.sendNotification(message)
    }

    override suspend fun getNotificationLogs(): Result<List<NotificationLog>> {
        return api.getNotificationLogs().map { dtoList ->
            dtoList.map { dto ->
                NotificationLog(
                    id = dto.id,
                    packageName = dto.packageName,
                    title = dto.title,
                    text = dto.text,
                    createdAt = dto.createdAt,
                    parsedData = try {
                        ParsedYapeData.fromParsedFields(dto.packageName, dto.title, dto.text)
                    } catch (e: Exception) {
                        null
                    }
                )
            }
        }
    }
}
