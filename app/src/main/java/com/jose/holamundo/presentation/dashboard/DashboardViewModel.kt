package com.jose.holamundo.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.holamundo.core.config.DeviceIdentifier
import com.jose.holamundo.core.event.RefreshEvent
import com.jose.holamundo.core.event.RefreshType
import com.jose.holamundo.data.repository.NotificationRepository
import com.jose.holamundo.domain.model.NotificationLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Filter mode for the chart (Week or Month).
 */
enum class FilterMode {
    WEEK, MONTH
}

/**
 * Search filter type for transactions.
 */
enum class SearchFilter {
    ALL, AMOUNT, DATE, SENDER
}

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
    val dailyTransactions: DailyTransactions = DailyTransactions(),
    
    // Filter state for chart
    val filterMode: FilterMode = FilterMode.WEEK,
    val selectedWeekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val selectedMonth: YearMonth = YearMonth.now(),
    
    // Search state for transactions
    val searchQuery: String = "",
    val searchFilter: SearchFilter = SearchFilter.ALL,
    val filteredNotifications: List<NotificationLog> = emptyList()
) {
    /**
     * Returns the end date of the selected week (Sunday).
     */
    val selectedWeekEnd: LocalDate get() = selectedWeekStart.plusDays(6)
    
    /**
     * Check if we can navigate to the next period (not future).
     */
    val canGoNext: Boolean get() {
        val today = LocalDate.now()
        return when (filterMode) {
            FilterMode.WEEK -> selectedWeekEnd.isBefore(today)
            FilterMode.MONTH -> selectedMonth.isBefore(YearMonth.now())
        }
    }
    
    /**
     * Returns a formatted label for the current period.
     */
    val periodLabel: String get() {
        return when (filterMode) {
            FilterMode.WEEK -> {
                val weekNumber = selectedWeekStart.get(java.time.temporal.WeekFields.ISO.weekOfYear())
                val startDay = selectedWeekStart.dayOfMonth
                val endDay = selectedWeekEnd.dayOfMonth
                val month = selectedWeekStart.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es"))
                "Sem $weekNumber ($startDay-$endDay $month)"
            }
            FilterMode.MONTH -> {
                val month = selectedMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es"))
                    .replaceFirstChar { it.uppercase() }
                "$month ${selectedMonth.year}"
            }
        }
    }
}

/**
 * ViewModel for the Dashboard screen.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: NotificationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Store all notifications for filtering
    private var allNotifications: List<NotificationLog> = emptyList()

    // Get the unique device ID for this installation
    private val deviceId: String = DeviceIdentifier.getDeviceId(context)

    init {
        // Listen for global refresh events
        observeRefreshEvents()
        
        // Load initial data
        loadNotifications()
    }
    
    /**
     * Observes global refresh events and refreshes data when triggered.
     */
    private fun observeRefreshEvents() {
        viewModelScope.launch {
            RefreshEvent.refreshTrigger.collect { refreshType ->
                if (refreshType == RefreshType.ALL || refreshType == RefreshType.DASHBOARD) {
                    loadNotifications()
                }
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Filter notifications by this device's ID
            val result = repository.getNotificationLogs(deviceId)

            result.fold(
                onSuccess = { notifications ->
                    allNotifications = notifications
                    updateDashboardWithFilters(notifications)
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
                            dailyTransactions = DailyTransactions(),
                            filteredNotifications = emptyList()
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Updates the dashboard with current filters applied.
     */
    private fun updateDashboardWithFilters(notifications: List<NotificationLog>) {
        val currentState = _uiState.value
        
        // Filter notifications by selected period for chart
        val filteredForChart = filterNotificationsByPeriod(
            notifications,
            currentState.filterMode,
            currentState.selectedWeekStart,
            currentState.selectedMonth
        )
        
        // Calculate stats from filtered notifications
        val amounts = filteredForChart.mapNotNull { it.parsedData?.amount }
        val totalAmount = amounts.sum()
        val totalTransactions = amounts.size

        // Calculate distribution by payment source
        val distribution = calculateDistribution(filteredForChart)
        
        // Calculate daily transactions for the selected week
        val dailyTransactions = calculateDailyTransactions(
            filteredForChart,
            currentState.selectedWeekStart
        )
        
        // Apply search filter for transaction list
        val searchFiltered = applySearchFilter(
            notifications,
            currentState.searchQuery,
            currentState.searchFilter
        )

        _uiState.update {
            it.copy(
                isLoading = false,
                notifications = notifications,
                totalTransactions = totalTransactions,
                totalAmount = totalAmount,
                transactionsToday = totalTransactions,
                amountToday = totalAmount,
                distribution = distribution,
                dailyTransactions = dailyTransactions,
                filteredNotifications = searchFiltered
            )
        }
    }
    
    /**
     * Filters notifications by the selected period (week or month).
     */
    private fun filterNotificationsByPeriod(
        notifications: List<NotificationLog>,
        filterMode: FilterMode,
        weekStart: LocalDate,
        month: YearMonth
    ): List<NotificationLog> {
        return notifications.filter { notification ->
            val dateTime = notification.getLocalDateTime() ?: return@filter false
            val date = dateTime.toLocalDate()
            
            when (filterMode) {
                FilterMode.WEEK -> {
                    val weekEnd = weekStart.plusDays(6)
                    !date.isBefore(weekStart) && !date.isAfter(weekEnd)
                }
                FilterMode.MONTH -> {
                    YearMonth.from(date) == month
                }
            }
        }
    }
    
    /**
     * Applies the search filter to the notifications list.
     */
    private fun applySearchFilter(
        notifications: List<NotificationLog>,
        query: String,
        filter: SearchFilter
    ): List<NotificationLog> {
        if (query.isBlank()) return notifications
        
        val lowerQuery = query.lowercase().trim()
        
        return notifications.filter { notification ->
            when (filter) {
                SearchFilter.ALL -> {
                    matchesAmount(notification, lowerQuery) ||
                    matchesDate(notification, lowerQuery) ||
                    matchesSender(notification, lowerQuery)
                }
                SearchFilter.AMOUNT -> matchesAmount(notification, lowerQuery)
                SearchFilter.DATE -> matchesDate(notification, lowerQuery)
                SearchFilter.SENDER -> matchesSender(notification, lowerQuery)
            }
        }
    }
    
    private fun matchesAmount(notification: NotificationLog, query: String): Boolean {
        val amount = notification.parsedData?.amount ?: return false
        val amountStr = amount.toString()
        val formattedAmount = "%.2f".format(amount)
        return amountStr.contains(query) || formattedAmount.contains(query)
    }
    
    private fun matchesDate(notification: NotificationLog, query: String): Boolean {
        val dateTime = notification.getLocalDateTime() ?: return false
        val dateStr = "%02d/%02d/%d".format(dateTime.dayOfMonth, dateTime.monthValue, dateTime.year)
        val shortDateStr = "%02d/%02d".format(dateTime.dayOfMonth, dateTime.monthValue)
        return dateStr.contains(query) || shortDateStr.contains(query)
    }
    
    private fun matchesSender(notification: NotificationLog, query: String): Boolean {
        val sender = notification.parsedData?.sender?.lowercase() ?: return false
        return sender.contains(query)
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
    
    /**
     * Calculates daily transactions for a specific week.
     * Always returns amounts for all 7 days of the week (Mon-Sun).
     */
    private fun calculateDailyTransactions(
        notifications: List<NotificationLog>,
        weekStart: LocalDate
    ): DailyTransactions {
        // Initialize all days with 0
        val dayAmounts = DayOfWeek.entries.associateWith { 0.0 }.toMutableMap()
        
        notifications.forEach { notification ->
            val amount = notification.parsedData?.amount ?: 0.0
            val dateTime = notification.getLocalDateTime()
            
            if (dateTime != null && amount > 0) {
                val date = dateTime.toLocalDate()
                val weekEnd = weekStart.plusDays(6)
                
                // Only count if within the selected week
                if (!date.isBefore(weekStart) && !date.isAfter(weekEnd)) {
                    val dayOfWeek = dateTime.dayOfWeek
                    dayAmounts[dayOfWeek] = (dayAmounts[dayOfWeek] ?: 0.0) + amount
                }
            }
        }
        
        return DailyTransactions(dayAmounts)
    }

    // ==================== PUBLIC ACTIONS ====================

    fun refresh() {
        loadNotifications()
    }
    
    /**
     * Changes the filter mode (Week or Month).
     */
    fun setFilterMode(mode: FilterMode) {
        _uiState.update { it.copy(filterMode = mode) }
        updateDashboardWithFilters(allNotifications)
    }
    
    /**
     * Navigate to the previous period.
     */
    fun previousPeriod() {
        _uiState.update { state ->
            when (state.filterMode) {
                FilterMode.WEEK -> state.copy(
                    selectedWeekStart = state.selectedWeekStart.minusWeeks(1)
                )
                FilterMode.MONTH -> state.copy(
                    selectedMonth = state.selectedMonth.minusMonths(1)
                )
            }
        }
        updateDashboardWithFilters(allNotifications)
    }
    
    /**
     * Navigate to the next period (if not future).
     */
    fun nextPeriod() {
        val currentState = _uiState.value
        if (!currentState.canGoNext) return
        
        _uiState.update { state ->
            when (state.filterMode) {
                FilterMode.WEEK -> state.copy(
                    selectedWeekStart = state.selectedWeekStart.plusWeeks(1)
                )
                FilterMode.MONTH -> state.copy(
                    selectedMonth = state.selectedMonth.plusMonths(1)
                )
            }
        }
        updateDashboardWithFilters(allNotifications)
    }
    
    /**
     * Updates the search query.
     */
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateDashboardWithFilters(allNotifications)
    }
    
    /**
     * Updates the search filter type.
     */
    fun setSearchFilter(filter: SearchFilter) {
        _uiState.update { it.copy(searchFilter = filter) }
        updateDashboardWithFilters(allNotifications)
    }
}
