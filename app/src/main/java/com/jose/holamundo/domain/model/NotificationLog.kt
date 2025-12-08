package com.jose.holamundo.domain.model

/**
 * Domain model representing a notification log entry.
 * Now receives parsed fields directly from backend.
 */
data class NotificationLog(
    val id: Long,
    val packageName: String?,
    val title: String?,
    val text: String?,
    val createdAt: String?,
    val parsedData: ParsedYapeData? = null
)

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

