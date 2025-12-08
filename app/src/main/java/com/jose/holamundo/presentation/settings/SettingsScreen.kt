package com.jose.holamundo.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jose.holamundo.BuildConfig
import com.jose.holamundo.core.config.AppConfig

/**
 * Settings screen with app configuration options.
 */
@Composable
fun SettingsScreen(
    onOpenNotificationSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Ajustes",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(Modifier.height(24.dp))

        // App Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Información de la App",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(Modifier.height(12.dp))

                SettingsItem("Versión", BuildConfig.VERSION_NAME)
                SettingsItem("Backend URL", AppConfig.baseUrl)
                SettingsItem("API Version", AppConfig.apiVersion)
                SettingsItem("Logs habilitados", if (AppConfig.enableLogs) "Sí" else "No")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Permissions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Permisos",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onOpenNotificationSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configurar acceso a notificaciones")
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

