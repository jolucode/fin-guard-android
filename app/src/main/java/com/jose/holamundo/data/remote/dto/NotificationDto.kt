package com.jose.holamundo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for sending notifications to the backend.
 */
@Serializable
data class NotificationRequestDto(
    @SerialName("message")
    val message: String,
    @SerialName("deviceId")
    val deviceId: String? = null
)

/**
 * DTO for receiving notification logs from the backend.
 * Matches the NotificationResponse DTO in Spring Boot with parsed fields.
 */
@Serializable
data class NotificationLogDto(
    @SerialName("id")
    val id: Long,
    @SerialName("packageName")
    val packageName: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("text")
    val text: String? = null,
    @SerialName("deviceId")
    val deviceId: String? = null,
    @SerialName("createdAt")
    val createdAt: String? = null
)

/**
 * DTO for transaction data parsed from Yape notifications.
 */
@Serializable
data class TransactionDto(
    @SerialName("id")
    val id: Long,
    @SerialName("amount")
    val amount: Double,
    @SerialName("sender")
    val sender: String,
    @SerialName("timestamp")
    val timestamp: String,
    @SerialName("type")
    val type: String
)

/**
 * DTO for dashboard statistics (for future backend implementation).
 */
@Serializable
data class DashboardStatsDto(
    @SerialName("totalTransactions")
    val totalTransactions: Int = 0,
    @SerialName("totalAmount")
    val totalAmount: Double = 0.0,
    @SerialName("averageAmount")
    val averageAmount: Double = 0.0,
    @SerialName("transactionsToday")
    val transactionsToday: Int = 0,
    @SerialName("amountToday")
    val amountToday: Double = 0.0,
    @SerialName("newClientsToday")
    val newClientsToday: Int = 0
)
