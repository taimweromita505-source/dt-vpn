package com.taimwe.vpn

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE)

    var isAdBlockerEnabled: Boolean
        get() = prefs.getBoolean("ad_blocker_enabled", true)
        set(value) = prefs.edit { putBoolean("ad_blocker_enabled", value) }

    var selectedServer: String?
        get() = prefs.getString("selected_server", "Auto")
        set(value) = prefs.edit { putString("selected_server", value) }

    var connectionCount: Int
        get() = prefs.getInt("connection_count", 0)
        set(value) = prefs.edit { putInt("connection_count", value) }

    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark_mode", false)
        set(value) = prefs.edit { putBoolean("dark_mode", value) }
}
