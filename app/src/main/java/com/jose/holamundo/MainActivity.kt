package com.jose.holamundo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
                    onOpenNotificationSettings = { openNotificationSettings() },
                    onSendTestNotification = { sendTestNotification() }
                )
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
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
    onOpenNotificationSettings: () -> Unit,
    onSendTestNotification: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Integración Yape - MVP", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "1) Da acceso para leer notificaciones.\n" +
                    "2) Envía notificación de prueba.\n" +
                    "3) Revisa Logcat (YAPE_LISTENER)."
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onOpenNotificationSettings) {
            Text("Dar acceso a notificaciones")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSendTestNotification) {
            Text("Enviar notificación de prueba")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppScreen() {
    HolaMundoAndroidTheme { AppScreen({}, {}) }
}