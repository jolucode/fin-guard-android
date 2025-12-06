package com.jose.holamundo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jose.holamundo.ui.theme.HolaMundoAndroidTheme

class MainActivity : ComponentActivity() {

    private val testChannelId = "test_channel"

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        askNotificationPermission()
        createNotificationChannel()

        setContent {
            HolaMundoAndroidTheme {
                AppScreen(
                    isServiceEnabled = isNotificationServiceEnabled(),
                    onOpenNotificationSettings = { openNotificationSettings() },
                    onSendTestNotification = { sendTestNotification() },
                    onCheckStatus = { checkServiceStatus() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNotificationListenerPermission()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: ""

        val myListener = ComponentName(this, YapeNotificationListener::class.java)
        return enabledListeners.contains(myListener.flattenToString())
    }

    private fun checkNotificationListenerPermission() {
        val enabled = isNotificationServiceEnabled()

        Log.d("MainActivity", "🔔 Servicio habilitado: $enabled")

        if (!enabled) {
            Log.w("MainActivity", "⚠️ El servicio NO está habilitado")
            Log.w("MainActivity", "💡 Ve a Ajustes → Acceso a notificaciones → Hola Mundo")
        } else {
            Log.d("MainActivity", "✅ El servicio está correctamente habilitado")
        }
    }

    private fun checkServiceStatus() {
        val enabled = isNotificationServiceEnabled()
        Toast.makeText(
            this,
            if (enabled) "✅ Servicio habilitado" else "❌ Servicio deshabilitado",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openNotificationSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error abriendo configuración", e)
            Toast.makeText(
                this,
                "No se pudo abrir la configuración",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                testChannelId,
                "Test Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun sendTestNotification() {
        Log.d("YAPE_LISTENER", "🚀 Enviando notificación de prueba desde MainActivity")

        val notification = NotificationCompat.Builder(this, testChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Notificación de prueba")
            .setContentText("Hola, esto es una notificación enviada desde mi app.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(1001, notification)
        }
    }
}

@Composable
fun AppScreen(
    isServiceEnabled: Boolean,
    onOpenNotificationSettings: () -> Unit,
    onSendTestNotification: () -> Unit,
    onCheckStatus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Integración Yape - MVP", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(8.dp))

        Text(
            YapeNotificationListener.BACKEND_URL,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        // Estado del servicio
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isServiceEnabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isServiceEnabled) "✅ Servicio Activo" else "❌ Servicio Inactivo",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isServiceEnabled)
                        "Capturando notificaciones"
                    else
                        "Necesitas dar acceso",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "1) Da acceso para leer notificaciones.\n" +
                    "2) Envía notificación de prueba.\n" +
                    "3) Revisa Logcat (YAPE_LISTENER).",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        // Botón principal según el estado
        if (!isServiceEnabled) {
            Button(
                onClick = onOpenNotificationSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dar acceso a notificaciones")
            }
        } else {
            OutlinedButton(
                onClick = onOpenNotificationSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configurar acceso a notificaciones")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botón de prueba
        Button(
            onClick = onSendTestNotification,
            modifier = Modifier.fillMaxWidth(),
            enabled = isServiceEnabled
        ) {
            Text("Enviar notificación de prueba")
        }

        Spacer(Modifier.height(16.dp))

        // Botón verificar estado
        OutlinedButton(
            onClick = onCheckStatus,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verificar estado")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppScreen() {
    HolaMundoAndroidTheme {
        AppScreen(
            isServiceEnabled = false,
            onOpenNotificationSettings = {},
            onSendTestNotification = {},
            onCheckStatus = {}
        )
    }
}