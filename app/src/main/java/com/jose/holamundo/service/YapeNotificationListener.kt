package com.jose.holamundo.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.jose.holamundo.core.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

/**
 * Notification listener service that captures Yape notifications
 * and sends them to the backend for processing.
 */
class YapeNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "YAPE_LISTENER"
        private const val YAPE_PACKAGE = "com.bcp.innovacxion.yapeapp"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Listener conectado - Backend: ${AppConfig.notificationsEndpoint}")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Listener desconectado")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName
        val extras = sbn.notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Build the raw message for logging and backend
        val rawMessage = "package=$packageName | title=$title | text=$text"

        if (AppConfig.enableLogs) {
            Log.d(TAG, "Notificación capturada -> $rawMessage")
        }

        // Filter for Yape notifications only (optional - can be configured)
        val isYapeNotification = packageName == YAPE_PACKAGE ||
                title.contains("Yape", ignoreCase = true) ||
                text.contains("Yape", ignoreCase = true)

        if (isYapeNotification || AppConfig.enableLogs) {
            // Send to backend using coroutines
            serviceScope.launch {
                sendToBackend(rawMessage)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (AppConfig.enableLogs) {
            Log.d(TAG, "Notificación removida de ${sbn?.packageName}")
        }
    }

    /**
     * Sends the captured notification to the backend API.
     */
    private suspend fun sendToBackend(message: String) {
        try {
            // Escape quotes for JSON
            val safeMessage = message.replace("\"", "\\\"")
            val jsonBody = """{"message": "$safeMessage"}"""

            val url = URL(AppConfig.notificationsEndpoint)
            val conn = url.openConnection() as HttpURLConnection

            conn.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            conn.outputStream.use { os ->
                val input = jsonBody.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val code = conn.responseCode

            if (AppConfig.enableLogs) {
                Log.d(TAG, "Enviado al backend. HTTP $code")
            }

            conn.disconnect()

        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar al backend", e)
        }
    }
}

