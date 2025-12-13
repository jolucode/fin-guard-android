package com.jose.holamundo.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.holamundo.R
import com.jose.holamundo.domain.model.NotificationLog
import com.jose.holamundo.ui.components.LoadingIndicator
import com.jose.holamundo.ui.theme.ChartGreen
import com.jose.holamundo.ui.theme.PlinCyan
import com.jose.holamundo.ui.theme.Primary
import com.jose.holamundo.ui.theme.YapePurple
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Home screen showing app control center with cloud status, capture toggle, and daily metrics.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    isServiceEnabled: Boolean,
    onOpenNotificationSettings: () -> Unit,
    onSendTestNotification: () -> Unit,
    onCheckStatus: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading && uiState.isCheckingCloud) {
        LoadingIndicator(message = "Conectando...")
    } else {
        HomeContent(
            uiState = uiState,
            isServiceEnabled = isServiceEnabled,
            onToggleCapture = { viewModel.toggleCapture() },
            onRefresh = { viewModel.refresh() },
            onOpenNotificationSettings = onOpenNotificationSettings
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    isServiceEnabled: Boolean,
    onToggleCapture: () -> Unit,
    onRefresh: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            HeaderSection(
                onRefresh = onRefresh,
                onOpenSettings = onOpenNotificationSettings
            )
        }

        // Cloud Service Status
        item {
            CloudServiceCard(
                isActive = uiState.isCloudServiceActive,
                isChecking = uiState.isCheckingCloud
            )
        }

        // Notification Access Status (only show if not enabled)
        if (!isServiceEnabled) {
            item {
                NotificationAccessCard(onOpenSettings = onOpenNotificationSettings)
            }
        }

        // Capture Toggle Card
        item {
            CaptureToggleCard(
                isCaptureEnabled = uiState.isCaptureEnabled,
                onToggle = onToggleCapture,
                isServiceEnabled = isServiceEnabled
            )
        }

        // Today's Metrics Header
        item {
            Text(
                text = "Hoy",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Metrics Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Transacciones",
                    value = uiState.transactionsToday.toString(),
                    icon = "💳"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Total",
                    value = currencyFormat.format(uiState.amountToday),
                    icon = "💰",
                    valueColor = ChartGreen
                )
            }
        }

        // Last Transaction
        item {
        Text(
                text = "Última transacción",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            if (uiState.lastTransaction != null) {
                LastTransactionCard(transaction = uiState.lastTransaction)
            } else {
                EmptyTransactionCard()
            }
        }
    }
}

@Composable
private fun HeaderSection(
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Pay Control Center",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
        Text(
                text = "Centro de control de pagos",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
        
        Row {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar",
                    tint = Primary
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun CloudServiceCard(
    isActive: Boolean,
    isChecking: Boolean
) {
        Card(
            modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) ChartGreen.copy(alpha = 0.2f)
                            else Color.Gray.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isActive) R.drawable.cloud else R.drawable.cloud_off
                        ),
                        contentDescription = null,
                        tint = if (isActive) ChartGreen else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Servicio Cloud",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isChecking) "Verificando..." 
                               else if (isActive) "Conectado" 
                               else "Sin conexión",
                        color = if (isActive) ChartGreen else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (isChecking) Color.Yellow
                        else if (isActive) ChartGreen
                        else Color.Red.copy(alpha = 0.7f)
                    )
            )
        }
    }
}

@Composable
private fun NotificationAccessCard(
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF6B6B).copy(alpha = 0.15f)
        ),
        onClick = onOpenSettings
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Acceso a notificaciones requerido",
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Toca aquí para configurar",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CaptureToggleCard(
    isCaptureEnabled: Boolean,
    onToggle: () -> Unit,
    isServiceEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        )
    ) {
        Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
            ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Captura de Transacciones",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isCaptureEnabled) 
                        "Las notificaciones de pago se guardarán automáticamente"
                    else
                        "Pausado - las transacciones no se registrarán",
                    color = if (isCaptureEnabled) Color.Gray else Color(0xFFFF6B6B).copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Switch(
                checked = isCaptureEnabled,
                onCheckedChange = { onToggle() },
                enabled = isServiceEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f),
                    disabledCheckedThumbColor = Color.Gray,
                    disabledCheckedTrackColor = Color.Gray.copy(alpha = 0.3f),
                    disabledUncheckedThumbColor = Color.Gray.copy(alpha = 0.5f),
                    disabledUncheckedTrackColor = Color.Gray.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: String,
    valueColor: Color = Color.White
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
        }

            Spacer(Modifier.height(8.dp))

        Text(
                text = value,
                color = valueColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LastTransactionCard(transaction: NotificationLog) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
    val parsed = transaction.parsedData
    val isYape = parsed?.packageName?.contains("yape", ignoreCase = true) == true
    
    // Format date using the getLocalDateTime() method
    val formattedTime = transaction.getLocalDateTime()?.let { dateTime ->
        try {
            val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")
            dateTime.format(outputFormatter)
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (isYape) 
                                listOf(YapePurple, YapePurple.copy(alpha = 0.6f))
                            else 
                                listOf(PlinCyan, PlinCyan.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isYape) "💜" else "💙",
                    fontSize = 22.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parsed?.sender ?: parsed?.title ?: "Transacción",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row {
                    Text(
                        text = if (isYape) "Yape" else "Plin",
                        color = if (isYape) YapePurple else PlinCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    formattedTime?.let {
                        Text(
                            text = " • $it",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Amount
            parsed?.amount?.let { amount ->
                Text(
                    text = "+${currencyFormat.format(amount)}",
                    color = ChartGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionCard() {
    Card(
            modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📭", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Sin transacciones hoy",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
