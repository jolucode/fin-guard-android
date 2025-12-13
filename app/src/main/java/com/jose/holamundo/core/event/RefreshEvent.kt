package com.jose.holamundo.core.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global event manager for triggering refresh across all ViewModels.
 * Uses SharedFlow to emit refresh events that multiple subscribers can observe.
 */
object RefreshEvent {

    private val _refreshTrigger = MutableSharedFlow<RefreshType>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val refreshTrigger: SharedFlow<RefreshType> = _refreshTrigger.asSharedFlow()

    /**
     * Triggers a global refresh event.
     * All ViewModels observing this flow will refresh their data.
     */
    suspend fun triggerRefresh(type: RefreshType = RefreshType.ALL) {
        _refreshTrigger.emit(type)
    }

    /**
     * Non-suspend version for use in callbacks.
     */
    fun triggerRefreshSync(type: RefreshType = RefreshType.ALL) {
        _refreshTrigger.tryEmit(type)
    }
}

/**
 * Types of refresh that can be triggered.
 */
enum class RefreshType {
    ALL,        // Refresh everything
    HOME,       // Refresh home screen only
    DASHBOARD   // Refresh dashboard only
}

