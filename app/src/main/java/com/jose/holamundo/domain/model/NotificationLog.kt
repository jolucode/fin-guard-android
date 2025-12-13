package com.jose.holamundo.domain.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Domain model representing a notification log entry.
 * Now receives parsed fields directly from backend.
 * Note: id is String (MongoDB ObjectId), createdAt comes as Instant (ISO-8601 with Z)
 */
data class NotificationLog(
    val id: String,
    val packageName: String?,
    val title: String?,
    val text: String?,
    val createdAt: String?,
    val parsedData: ParsedYapeData? = null
) {
    /**
     * Parses createdAt string (Instant format) to LocalDateTime.
     * Supports both ISO_INSTANT (with Z) and ISO_LOCAL_DATE_TIME formats.
     */
    fun getLocalDateTime(): LocalDateTime? {
        return createdAt?.let { dateString ->
            try {
                // Try parsing as Instant first (format: 2025-12-13T10:30:00Z)
                val instant = Instant.parse(dateString)
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            } catch (e: Exception) {
                try {
                    // Fallback to ISO_LOCAL_DATE_TIME (format: 2025-12-13T10:30:00)
                    LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e2: Exception) {
                    null
                }
            }
        }
    }
}

/**
 * Parsed data from a Yape notification.
 */
data class ParsedYapeData(
    val packageName: String,
    val title: String,
    val text: String,
    val amount: Double? = null,
    val sender: String? = null
) {
    companion object {
        /**
         * Creates ParsedYapeData from already-parsed fields (from backend).
         */
        fun fromParsedFields(packageName: String?, title: String?, text: String?): ParsedYapeData {
            val textValue = text ?: ""
            
            // Extract amount from text (e.g., "S/ 50.00" or "S/ 0.1")
            val amountRegex = """S/?\s*(\d+(?:[.,]\d+)?)""".toRegex()
            val amount = amountRegex.find(textValue)?.groupValues?.get(1)
                ?.replace(",", ".")
                ?.toDoubleOrNull()

            return ParsedYapeData(
                packageName = packageName ?: "",
                title = title ?: "",
                text = textValue,
                amount = amount,
                sender = extractSender(textValue)
            )
        }

        private fun extractSender(text: String): String? {
            // Pattern: "Juan Perez te envió"
            val pattern = """([A-Za-záéíóúñÁÉÍÓÚÑ.\s]+)\s+te\s+envió""".toRegex(RegexOption.IGNORE_CASE)
            return pattern.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
        }
    }
}

