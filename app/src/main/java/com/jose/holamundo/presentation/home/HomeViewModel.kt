package com.jose.holamundo.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jose.holamundo.core.config.CapturePreferences
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
import java.time.LocalDate
import javax.inject.Inject

/**
 * UI State for the Home screen.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isCaptureEnabled: Boolean = true,
    val isCloudServiceActive: Boolean = false,
    val isCheckingCloud: Boolean = true,
    
    // Daily metrics
    val transactionsToday: Int = 0,
    val amountToday: Double = 0.0,
    val lastTransaction: NotificationLog? = null,
    
    // Error state
    val error: String? = null
)

/**
 * ViewModel for the Home screen.
 * Manages capture state, cloud service status, and daily metrics.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NotificationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Get the unique device ID for this installation
    private val deviceId: String = DeviceIdentifier.getDeviceId(context)

    init {
        // Initialize capture preferences
        CapturePreferences.initialize(context)
        
        // Load initial state
        _uiState.update { 
            it.copy(isCaptureEnabled = CapturePreferences.isCaptureEnabled(context)) 
        }
        
        // Listen for global refresh events
        observeRefreshEvents()
        
        // Check cloud service and load metrics
        checkCloudServiceAndLoadMetrics()
    }
    
    /**
     * Observes global refresh events and refreshes data when triggered.
     */
    private fun observeRefreshEvents() {
        viewModelScope.launch {
            RefreshEvent.refreshTrigger.collect { refreshType ->
                if (refreshType == RefreshType.ALL || refreshType == RefreshType.HOME) {
                    checkCloudServiceAndLoadMetrics()
                }
            }
        }
    }

    /**
     * Checks if the cloud service is active and loads today's metrics.
     */
    fun checkCloudServiceAndLoadMetrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isCheckingCloud = true, error = null) }

            val result = repository.getNotificationLogs(deviceId)

            result.fold(
                onSuccess = { notifications ->
                    // Cloud service is active if we got a successful response
                    val todayMetrics = calculateTodayMetrics(notifications)
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCheckingCloud = false,
                            isCloudServiceActive = true,
                            transactionsToday = todayMetrics.transactionCount,
                            amountToday = todayMetrics.totalAmount,
                            lastTransaction = todayMetrics.lastTransaction,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCheckingCloud = false,
                            isCloudServiceActive = false,
                            transactionsToday = 0,
                            amountToday = 0.0,
                            lastTransaction = null,
                            error = "No se pudo conectar al servicio cloud"
                        )
                    }
                }
            )
        }
    }

    /**
     * Toggles the capture enabled state.
     */
    fun toggleCapture() {
        val newState = CapturePreferences.toggleCapture(context)
        _uiState.update { it.copy(isCaptureEnabled = newState) }
    }

    /**
     * Sets the capture enabled state directly.
     */
    fun setCaptureEnabled(enabled: Boolean) {
        CapturePreferences.setCaptureEnabled(context, enabled)
        _uiState.update { it.copy(isCaptureEnabled = enabled) }
    }

    /**
     * Triggers a global refresh for all screens.
     */
    fun refresh() {
        viewModelScope.launch {
            RefreshEvent.triggerRefresh(RefreshType.ALL)
        }
    }
    
    /**
     * Refreshes only this screen's data (local refresh).
     */
    fun refreshLocal() {
        checkCloudServiceAndLoadMetrics()
    }

    /**
     * Calculates metrics for today only.
     */
    private fun calculateTodayMetrics(notifications: List<NotificationLog>): TodayMetrics {
        val today = LocalDate.now()
        
        val todayNotifications = notifications.filter { notification ->
            notification.getLocalDateTime()?.toLocalDate() == today
        }
        
        val amounts = todayNotifications.mapNotNull { it.parsedData?.amount }
        val totalAmount = amounts.sum()
        val transactionCount = amounts.size
        
        // Get the most recent transaction (first in the list, assuming sorted by date desc)
        val lastTransaction = todayNotifications.firstOrNull()
        
        return TodayMetrics(
            transactionCount = transactionCount,
            totalAmount = totalAmount,
            lastTransaction = lastTransaction
        )
    }

    /**
     * Data class for today's metrics.
     */
    private data class TodayMetrics(
        val transactionCount: Int,
        val totalAmount: Double,
        val lastTransaction: NotificationLog?
    )
}

