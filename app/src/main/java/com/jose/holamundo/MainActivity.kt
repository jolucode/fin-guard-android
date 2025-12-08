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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.compose.rememberNavController
import com.jose.holamundo.presentation.navigation.BottomNavBar
import com.jose.holamundo.presentation.navigation.NavGraph
import com.jose.holamundo.service.YapeNotificationListener
import com.jose.holamundo.ui.theme.HolaMundoAndroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val testChannelId = "test_channel"

    private var isServiceEnabled by mutableStateOf(false)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        askNotificationPermission()
        createNotificationChannel()

        setContent {
            HolaMundoAndroidTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavBar(navController = navController)
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        innerPadding = innerPadding,
                        isServiceEnabled = isServiceEnabled,
                        onOpenNotificationSettings = { openNotificationSettings() },
                        onSendTestNotification = { sendTestNotification() },
                        onCheckStatus = { checkServiceStatus() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isServiceEnabled = isNotificationServiceEnabled()
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

        Log.d("MainActivity", "Servicio habilitado: $enabled")

        if (!enabled) {
            Log.w("MainActivity", "El servicio NO está habilitado")
            Log.w("MainActivity", "Ve a Ajustes → Acceso a notificaciones → FinGuard")
        } else {
            Log.d("MainActivity", "El servicio está correctamente habilitado")
        }
    }

    private fun checkServiceStatus() {
        val enabled = isNotificationServiceEnabled()
        Toast.makeText(
            this,
            if (enabled) "Servicio habilitado" else "Servicio deshabilitado",
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
        Log.d("YAPE_LISTENER", "Enviando notificación de prueba desde MainActivity")

        val notification = NotificationCompat.Builder(this, testChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Yape - Notificación de prueba")
            .setContentText("Te han enviado S/ 50.00 por concepto de prueba.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(1001, notification)
        }
    }
}
