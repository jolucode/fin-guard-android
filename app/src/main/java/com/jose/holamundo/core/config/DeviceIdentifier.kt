package com.jose.holamundo.core.config

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.security.MessageDigest

/**
 * Utility object for generating and persisting a unique device identifier.
 * Uses ANDROID_ID as the base to ensure persistence across app reinstalls.
 * The ID will only change if the device is factory reset.
 */
object DeviceIdentifier {

    private const val PREF_NAME = "fin_guard_device_prefs"
    private const val KEY_DEVICE_ID = "device_id"
    private const val APP_SALT = "PayControlCenter_2024"

    /**
     * Gets the unique device ID for this installation.
     * Uses ANDROID_ID as base, which persists across reinstalls.
     * Falls back to SharedPreferences if ANDROID_ID is unavailable.
     *
     * @param context Application context
     * @return The unique device identifier (consistent across updates)
     */
    fun getDeviceId(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // First, try to get the stable device ID based on ANDROID_ID
        val stableId = getStableDeviceId(context)
        
        if (stableId != null) {
            // Store it in SharedPreferences for consistency
            val storedId = prefs.getString(KEY_DEVICE_ID, null)
            if (storedId != stableId) {
                prefs.edit().putString(KEY_DEVICE_ID, stableId).apply()
            }
            return stableId
        }
        
        // Fallback: use SharedPreferences (legacy behavior)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = generateFallbackId()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }

        return deviceId
    }

    /**
     * Generates a stable device ID based on ANDROID_ID.
     * This ID persists across app reinstalls but changes on factory reset.
     */
    private fun getStableDeviceId(context: Context): String? {
        return try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            if (androidId.isNullOrBlank() || androidId == "9774d56d682e549c") {
                // "9774d56d682e549c" is a known bug value on some devices
                null
            } else {
                // Create a hash of ANDROID_ID + salt for privacy
                hashString("$androidId$APP_SALT")
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates a SHA-256 hash of the input string.
     */
    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(32)
    }

    /**
     * Generates a fallback ID if ANDROID_ID is unavailable.
     */
    private fun generateFallbackId(): String {
        return "fallback-${System.currentTimeMillis()}-${(1000..9999).random()}"
    }

    /**
     * Clears the stored device ID (useful for testing).
     *
     * @param context Application context
     */
    fun clearDeviceId(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_DEVICE_ID).apply()
    }
}

