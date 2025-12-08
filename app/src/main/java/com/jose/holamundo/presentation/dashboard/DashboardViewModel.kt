package com.jose.holamundo.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.holamundo.data.repository.NotificationRepository
import com.jose.holamundo.domain.model.NotificationLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Data class for payment source distribution.
 */
data class PaymentSourceDistribution(
    val yapeAmount: Double = 0.0,
    val plinAmount: Double = 0.0,
    val otherAmount: Double = 0.0
) {
    val total: Double get() = yapeAmount + plinAmount + otherAmount
    val yapePercentage: Float get() = if (total > 0) (yapeAmount / total * 100).toFloat() else 0f
    val plinPercentage: Float get() = if (total > 0) (plinAmount / total * 100).toFloat() else 0f
    val otherPercentage: Float get() = if (total > 0) (otherAmount / total * 100).toFloat() else 0f
}

/**
 * Data class for daily transactions.
 */
data class DailyTransactions(
    val dayAmounts: Map<DayOfWeek, Double> = emptyMap()
) {
    val maxAmount: Double get() = dayAmounts.values.maxOrNull() ?: 1.0
    
    fun getAmountForDay(day: DayOfWeek): Double = dayAmounts[day] ?: 0.0
    
    fun getHeightFraction(day: DayOfWeek): Float {
        val amount = getAmountForDay(day)
        return if (maxAmount > 0) (amount / maxAmount).toFloat().coerceIn(0.05f, 1f) else 0.05f
    }
}

/**
 * UI State for the Dashboard screen.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationLog> = emptyList(),
    val error: String? = null,
    // Calculated stats from notifications
    val totalTransactions: Int = 0,
    val totalAmount: Double = 0.0,
    val transactionsToday: Int = 0,
    val amountToday: Double = 0.0,
    // Chart data
    val distribution: PaymentSourceDistribution = PaymentSourceDistribution(),
    val dailyTransactions: DailyTransactions = DailyTransactions()
)

/**
 * ViewModel for the Dashboard screen.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = repository.getNotificationLogs()

            result.fold(
                onSuccess = { notifications ->
                    // Calculate stats from notifications
                    val amounts = notifications.mapNotNull { it.parsedData?.amount }
                    val totalAmount = amounts.sum()
                    val totalTransactions = amounts.size

                    // Calculate distribution by payment source
                    val distribution = calculateDistribution(notifications)
                    
                    // Calculate daily transactions
                    val dailyTransactions = calculateDailyTransactions(notifications)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = notifications,
                            totalTransactions = totalTransactions,
                            totalAmount = totalAmount,
                            transactionsToday = totalTransactions,
                            amountToday = totalAmount,
                            distribution = distribution,
                            dailyTransactions = dailyTransactions
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudo conectar al servidor. Mostrando datos de demostración.",
                            notifications = emptyList(),
                            totalTransactions = 0,
                            totalAmount = 0.0,
                            transactionsToday = 0,
                            amountToday = 0.0,
                            distribution = PaymentSourceDistribution(),
                            dailyTransactions = DailyTransactions()
                        )
                    }
                }
            )
        }
    }
    
    private fun calculateDistribution(notifications: List<NotificationLog>): PaymentSourceDistribution {
        var yapeAmount = 0.0
        var plinAmount = 0.0
        var otherAmount = 0.0
        
        notifications.forEach { notification ->
            val amount = notification.parsedData?.amount ?: 0.0
            val packageName = notification.parsedData?.packageName?.lowercase() ?: ""
            
            when {
                packageName.contains("yape") -> yapeAmount += amount
                packageName.contains("plin") -> plinAmount += amount
                else -> otherAmount += amount
            }
        }
        
        return PaymentSourceDistribution(yapeAmount, plinAmount, otherAmount)
    }
    
    private fun calculateDailyTransactions(notifications: List<NotificationLog>): DailyTransactions {
        val dayAmounts = mutableMapOf<DayOfWeek, Double>()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        
        notifications.forEach { notification ->
            val amount = notification.parsedData?.amount ?: 0.0
            val dateString = notification.createdAt
            
            if (dateString != null && amount > 0) {
                try {
                    val dateTime = LocalDateTime.parse(dateString, formatter)
                    val dayOfWeek = dateTime.dayOfWeek
                    dayAmounts[dayOfWeek] = (dayAmounts[dayOfWeek] ?: 0.0) + amount
                } catch (e: Exception) {
                    // Ignore parsing errors
                }
            }
        }
        
        return DailyTransactions(dayAmounts)
    }

    fun refresh() {
        loadNotifications()
    }
}
