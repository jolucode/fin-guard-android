package com.jose.holamundo.presentation.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jose.holamundo.domain.model.NotificationLog
import com.jose.holamundo.ui.components.FinGuardCard
import com.jose.holamundo.ui.components.LoadingIndicator
import com.jose.holamundo.ui.theme.ChartBlue
import com.jose.holamundo.ui.theme.ChartGreen
import com.jose.holamundo.ui.theme.ChartPurple
import com.jose.holamundo.ui.theme.ChartYellow
import com.jose.holamundo.ui.theme.PlinCyan
import com.jose.holamundo.ui.theme.Primary
import com.jose.holamundo.ui.theme.Warning
import com.jose.holamundo.ui.theme.YapePurple
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Dashboard screen showing KPIs and notification logs.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingIndicator(message = "Cargando datos...")
    } else {
        DashboardContent(
            uiState = uiState,
            onRefresh = { viewModel.refresh() }
        )
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "PE"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Control de Ventas",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refrescar",
                        tint = Primary
                    )
                }
            }
        }

        // Warning message if using demo data
        if (uiState.error != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Warning.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = uiState.error,
                            color = Warning,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Summary Card
        item {
            SummaryCard(
                newClients = uiState.transactionsToday,
                totalToday = currencyFormat.format(uiState.amountToday)
            )
        }

        // Sales Chart Card
        item {
            SalesChartCard(
                transactionsToday = uiState.totalTransactions,
                totalAmount = currencyFormat.format(uiState.totalAmount),
                dailyTransactions = uiState.dailyTransactions
            )
        }

        // Distribution Card
        item {
            DistributionCard(distribution = uiState.distribution)
        }

        // Notifications Section Header
        item {
            Text(
                text = "Transacciones Capturadas",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Notification logs or empty state
        if (uiState.notifications.isEmpty()) {
            item {
                EmptyNotificationsCard()
            }
        } else {
            items(uiState.notifications.take(10)) { notification ->
                NotificationLogCard(notification = notification)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    newClients: Int,
    totalToday: String
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Nuevos clientes: +$newClients",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Total recaudado hoy: $totalToday",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Primary, Primary.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("📈", fontSize = 24.sp)
            }
        }
    }
}

@Composable
private fun SalesChartCard(
    transactionsToday: Int,
    totalAmount: String,
    dailyTransactions: DailyTransactions
) {
    val daysOfWeek = listOf(
        DayOfWeek.MONDAY to "L",
        DayOfWeek.TUESDAY to "M",
        DayOfWeek.WEDNESDAY to "X",
        DayOfWeek.THURSDAY to "J",
        DayOfWeek.FRIDAY to "V",
        DayOfWeek.SATURDAY to "S",
        DayOfWeek.SUNDAY to "D"
    )
    
    // Find the day with max amount to highlight it
    val maxDay = dailyTransactions.dayAmounts.maxByOrNull { it.value }?.key
    
    FinGuardCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Ventas por día",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Resumen",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                Text(
                    "Transacciones: $transactionsToday",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    "Total: $totalAmount",
                    color = Primary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Bar chart with real data
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            daysOfWeek.forEach { (dayOfWeek, label) ->
                val heightFraction = dailyTransactions.getHeightFraction(dayOfWeek)
                val hasData = dailyTransactions.getAmountForDay(dayOfWeek) > 0
                val isMaxDay = dayOfWeek == maxDay && hasData
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height((100 * heightFraction).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                when {
                                    isMaxDay -> Primary
                                    hasData -> YapePurple
                                    else -> Color.Gray.copy(alpha = 0.3f)
                                }
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        color = if (isMaxDay) Primary else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (isMaxDay) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun DistributionCard(distribution: PaymentSourceDistribution) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
    
    FinGuardCard {
        Text(
            "Distribución de ingresos",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut chart with segments
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                // Draw segments based on distribution
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(100.dp)
                ) {
                    val total = distribution.total
                    if (total > 0) {
                        var startAngle = -90f
                        
                        // Yape segment
                        if (distribution.yapeAmount > 0) {
                            val sweepAngle = (distribution.yapeAmount / total * 360).toFloat()
                            drawArc(
                                color = YapePurple,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                        
                        // Plin segment
                        if (distribution.plinAmount > 0) {
                            val sweepAngle = (distribution.plinAmount / total * 360).toFloat()
                            drawArc(
                                color = PlinCyan,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                        
                        // Other segment
                        if (distribution.otherAmount > 0) {
                            val sweepAngle = (distribution.otherAmount / total * 360).toFloat()
                            drawArc(
                                color = ChartBlue,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                        }
                    } else {
                        // Empty state - gray circle
                        drawCircle(color = Color.Gray.copy(alpha = 0.3f))
                    }
                }
                
                // Inner circle (donut hole)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16213E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currencyFormat.format(distribution.total),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (distribution.yapeAmount > 0) {
                    LegendItemWithAmount(
                        "Yape", 
                        YapePurple, 
                        distribution.yapePercentage,
                        currencyFormat.format(distribution.yapeAmount)
                    )
                }
                if (distribution.plinAmount > 0) {
                    LegendItemWithAmount(
                        "Plin", 
                        PlinCyan, 
                        distribution.plinPercentage,
                        currencyFormat.format(distribution.plinAmount)
                    )
                }
                if (distribution.otherAmount > 0) {
                    LegendItemWithAmount(
                        "Otros", 
                        ChartBlue, 
                        distribution.otherPercentage,
                        currencyFormat.format(distribution.otherAmount)
                    )
                }
                if (distribution.total == 0.0) {
                    Text(
                        "Sin transacciones",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LegendItemWithAmount(label: String, color: Color, percentage: Float, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Row {
                Text(
                    label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${"%.1f".format(percentage)}%",
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                amount,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun EmptyNotificationsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
            Text("📭", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "No hay notificaciones",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Las notificaciones de Yape aparecerán aquí",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NotificationLogCard(notification: NotificationLog) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
    val parsed = notification.parsedData
    val isYape = parsed?.packageName?.contains("yape", ignoreCase = true) == true
    
    // Format date from ISO to dd/MM/yyyy - HH:mm
    val formattedDate = notification.createdAt?.let { dateString ->
        try {
            val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
            val dateTime = LocalDateTime.parse(dateString, inputFormatter)
            dateTime.format(outputFormatter)
        } catch (e: Exception) {
            dateString // fallback to original if parsing fails
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with Yape purple or Plin cyan
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isYape) YapePurple.copy(alpha = 0.3f)
                        else PlinCyan.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isYape) "💜" else "💙",
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Sender name or title
                Text(
                    text = parsed?.sender ?: parsed?.title ?: "Notificación",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                // Source label
                Text(
                    text = if (isYape) "Yape" else "Plin",
                    color = if (isYape) YapePurple else PlinCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                // Date
                formattedDate?.let { date ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = date,
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }

            // Amount if available
            parsed?.amount?.let { amount ->
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+${currencyFormat.format(amount)}",
                        color = ChartGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
