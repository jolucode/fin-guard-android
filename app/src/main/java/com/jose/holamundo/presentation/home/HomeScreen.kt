package com.jose.holamundo.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jose.holamundo.core.config.AppConfig

/**
 * Home screen showing notification service status and controls.
 */
@Composable
fun HomeScreen(
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
        Text(
            "FinGuard - Control de Ventas",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            AppConfig.fullApiUrl,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF00D09E)
        )

        Spacer(Modifier.height(24.dp))

        // Service status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isServiceEnabled)
                    Color(0xFF00D09E).copy(alpha = 0.2f)
                else
                    Color(0xFFFF6B6B).copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isServiceEnabled) "Servicio Activo" else "Servicio Inactivo",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isServiceEnabled) Color(0xFF00D09E) else Color(0xFFFF6B6B)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isServiceEnabled)
                        "Capturando notificaciones de Yape"
                    else
                        "Necesitas dar acceso a notificaciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "1) Da acceso para leer notificaciones.\n" +
                    "2) Envía notificación de prueba.\n" +
                    "3) Revisa el dashboard para ver estadísticas.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(24.dp))

        // Primary button based on service status
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

        // Test notification button
        Button(
            onClick = onSendTestNotification,
            modifier = Modifier.fillMaxWidth(),
            enabled = isServiceEnabled
        ) {
            Text("Enviar notificación de prueba")
        }

        Spacer(Modifier.height(16.dp))

        // Check status button
        OutlinedButton(
            onClick = onCheckStatus,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verificar estado")
        }
    }
}

