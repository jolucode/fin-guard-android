package com.jose.holamundo.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.jose.holamundo.core.config.AppConfig
import com.jose.holamundo.core.config.CapturePreferences
import com.jose.holamundo.core.config.DeviceIdentifier
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
    private var deviceId: String? = null
    private var isConnected = false

    companion object {
        private const val TAG = "YAPE_LISTENER"
        private const val YAPE_PACKAGE = "com.bcp.innovacxion.yapeapp"
        
        // Static variable to check listener status from outside
        var isListenerConnected = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "★★★ SERVICE CREATED ★★★")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "★★★ SERVICE DESTROYED ★★★")
        isListenerConnected = false
        isConnected = false
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        isListenerConnected = true
        
        // Get the unique device ID for this installation
        deviceId = DeviceIdentifier.getDeviceId(applicationContext)
        
        Log.d(TAG, "★★★ LISTENER CONECTADO ★★★")
        Log.d(TAG, "Backend URL: ${AppConfig.notificationsEndpoint}")
        Log.d(TAG, "Device ID: $deviceId")
        Log.d(TAG, "Logs habilitados: ${AppConfig.enableLogs}")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
        isListenerConnected = false
        Log.d(TAG, "★★★ LISTENER DESCONECTADO ★★★")
    }

    /**
     * Gets deviceId safely, initializing if needed.
     */
    private fun getDeviceIdSafe(): String {
        if (deviceId == null) {
            deviceId = DeviceIdentifier.getDeviceId(applicationContext)
            Log.d(TAG, "DeviceId inicializado tardíamente: $deviceId")
        }
        return deviceId ?: "unknown"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d(TAG, ">>> onNotificationPosted llamado - isConnected: $isConnected")
        
        // Check if capture is enabled by user
        val isCaptureEnabled = CapturePreferences.isCaptureEnabled(applicationContext)
        if (!isCaptureEnabled) {
            Log.d(TAG, ">>> Captura deshabilitada por usuario, ignorando notificación")
            return
        }
        
        if (sbn == null) {
            Log.d(TAG, ">>> sbn es NULL, ignorando")
            return
        }

        val packageName = sbn.packageName
        Log.d(TAG, ">>> Package recibido: $packageName")
        
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Build the raw message for logging and backend
        val rawMessage = "package=$packageName | title=$title | text=$text"

        Log.d(TAG, ">>> Notificación: $rawMessage")

        // Filter for Yape notifications only (optional - can be configured)
        val isYapeNotification = packageName == YAPE_PACKAGE ||
                title.contains("Yape", ignoreCase = true) ||
                text.contains("Yape", ignoreCase = true)

        Log.d(TAG, ">>> Es notificación Yape: $isYapeNotification")
        Log.d(TAG, ">>> enableLogs: ${AppConfig.enableLogs}")

        if (isYapeNotification || AppConfig.enableLogs) {
            Log.d(TAG, ">>> Preparando envío al backend...")
            val currentDeviceId = getDeviceIdSafe()
            serviceScope.launch {
                sendToBackend(rawMessage, currentDeviceId)
            }
        } else {
            Log.d(TAG, ">>> Notificación ignorada (no es Yape y logs deshabilitados)")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d(TAG, ">>> Notificación removida de ${sbn?.packageName}")
    }

    /**
     * Sends the captured notification to the backend API with device ID.
     */
    private suspend fun sendToBackend(message: String, deviceId: String) {
        Log.d(TAG, ">>> sendToBackend iniciado")
        Log.d(TAG, ">>> URL: ${AppConfig.notificationsEndpoint}")
        
        try {
            // Escape quotes for JSON
            val safeMessage = message.replace("\"", "\\\"")
            val jsonBody = """{"message": "$safeMessage", "deviceId": "$deviceId"}"""
            
            Log.d(TAG, ">>> JSON Body: $jsonBody")

            val url = URL(AppConfig.notificationsEndpoint)
            val conn = url.openConnection() as HttpURLConnection

            conn.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            Log.d(TAG, ">>> Enviando request...")

            conn.outputStream.use { os ->
                val input = jsonBody.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val code = conn.responseCode
            Log.d(TAG, ">>> Response code: $code")

            if (code == 200) {
                Log.d(TAG, "✓✓✓ ENVIADO EXITOSAMENTE al backend ✓✓✓")
            } else {
                Log.e(TAG, "✗✗✗ ERROR: HTTP $code ✗✗✗")
            }

            conn.disconnect()

        } catch (e: Exception) {
            Log.e(TAG, "✗✗✗ EXCEPCIÓN al enviar al backend ✗✗✗", e)
            Log.e(TAG, "Error: ${e.message}")
        }
    }
}

