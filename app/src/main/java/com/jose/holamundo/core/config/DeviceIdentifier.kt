package com.jose.holamundo.core.config

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Utility object for generating and persisting a unique device identifier.
 * The ID is generated once on first app launch and persisted in SharedPreferences.
 */
object DeviceIdentifier {

    private const val PREF_NAME = "fin_guard_device_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    /**
     * Gets the unique device ID for this installation.
     * If no ID exists, generates a new UUID and stores it.
     *
     * @param context Application context
     * @return The unique device identifier
     */
    fun getDeviceId(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }

        return deviceId
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

