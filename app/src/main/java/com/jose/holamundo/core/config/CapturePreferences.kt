package com.jose.holamundo.core.config

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Utility object for managing the notification capture enabled/disabled state.
 * When disabled, notifications will not be sent to the backend.
 */
object CapturePreferences {

    private const val PREF_NAME = "fin_guard_capture_prefs"
    private const val KEY_CAPTURE_ENABLED = "capture_enabled"

    // StateFlow to observe capture state changes
    private val _captureEnabledFlow = MutableStateFlow(true)
    val captureEnabledFlow: StateFlow<Boolean> = _captureEnabledFlow.asStateFlow()

    /**
     * Checks if notification capture is enabled.
     *
     * @param context Application context
     * @return true if capture is enabled, false otherwise
     */
    fun isCaptureEnabled(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CAPTURE_ENABLED, true) // Default: enabled
    }

    /**
     * Sets the notification capture enabled state.
     *
     * @param context Application context
     * @param enabled true to enable capture, false to disable
     */
    fun setCaptureEnabled(context: Context, enabled: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CAPTURE_ENABLED, enabled).apply()
        _captureEnabledFlow.value = enabled
    }

    /**
     * Initializes the StateFlow with the current persisted value.
     * Should be called when the app starts.
     *
     * @param context Application context
     */
    fun initialize(context: Context) {
        _captureEnabledFlow.value = isCaptureEnabled(context)
    }

    /**
     * Toggles the capture state and returns the new state.
     *
     * @param context Application context
     * @return The new capture enabled state
     */
    fun toggleCapture(context: Context): Boolean {
        val newState = !isCaptureEnabled(context)
        setCaptureEnabled(context, newState)
        return newState
    }
}

