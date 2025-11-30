package com.jose.holamundo

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

class YapeNotificationListener : NotificationListenerService() {

    companion object {
        // 👇 CAMBIA ESTA URL SEGÚN DONDE ESTÉ TU BACKEND
        // Si usas EMULADOR:  "http://10.0.2.2:8080/api/notifications"
        // Si usas CELULAR REAL (misma WiFi): "http://192.168.0.9:8080/api/notifications"
        private const val BACKEND_URL =
            "http://192.168.0.9:8080/api/notifications"

    }

    // Se llama cuando el sistema conecta el listener (después de dar el permiso)
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("YAPE_LISTENER", "✅ Listener conectado")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName
        val extras = sbn.notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Armas la cadena completa que quieres guardar en la BD
        val rawMessage = "package=$packageName | title=$title | text=$text"

        Log.d(
            "YAPE_LISTENER",
            "📩 Notificación capturada -> $rawMessage"
        )

        // Enviar al backend (en un hilo aparte, para no bloquear el hilo principal)
        sendToBackend(rawMessage)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Si quieres loguear la eliminación:
        // Log.d("YAPE_LISTENER", "🗑 Notificación removida de ${sbn?.packageName}")
    }

    /**
     * Envía la notificación capturada a tu API REST en Spring Boot:
     * POST /api/notifications
     * Body: { "message": "texto completo..." }
     */
    private fun sendToBackend(message: String) {
        Thread {
            try {
                // Escapar comillas por si acaso
                val safeMessage = message.replace("\"", "\\\"")
                val jsonBody = """{"message": "$safeMessage"}"""

                val url = URL(BACKEND_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.doOutput = true

                conn.outputStream.use { os ->
                    val input = jsonBody.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val code = conn.responseCode
                Log.d("YAPE_LISTENER", "🌐 Enviado al backend. HTTP $code")

            } catch (e: Exception) {
                Log.e("YAPE_LISTENER", "❌ Error al enviar al backend", e)
            }
        }.start()
    }
}