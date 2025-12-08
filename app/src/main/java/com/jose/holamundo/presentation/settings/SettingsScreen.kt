package com.jose.holamundo.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jose.holamundo.BuildConfig
import com.jose.holamundo.R
import com.jose.holamundo.core.config.AppConfig
import com.jose.holamundo.core.config.DeviceIdentifier
import com.jose.holamundo.ui.theme.Primary

/**
 * Settings screen with app configuration options.
 */
@Composable
fun SettingsScreen(
    onOpenNotificationSettings: () -> Unit
) {
    val context = LocalContext.current
    val deviceId = remember { DeviceIdentifier.getDeviceId(context) }

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

        // Device ID Card - Para identificar este dispositivo
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
                    "Identificador del Dispositivo",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Este ID único identifica tu dispositivo. Compártelo con el administrador para registrar tu negocio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(Modifier.height(12.dp))

                // Device ID display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF0D1B2A),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = deviceId,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Copy button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Device ID", deviceId)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "ID copiado al portapapeles", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.copy),
                        contentDescription = "Copiar",
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Copiar ID",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

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

