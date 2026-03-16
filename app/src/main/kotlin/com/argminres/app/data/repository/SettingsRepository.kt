package com.argminres.app.data.repository

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_ACTIVATED = "is_activated"
        private const val KEY_STORE_ID = "store_id"
        private const val KEY_DB_INITIALIZED = "db_initialized"
        private const val KEY_THEME_MODE = "theme_mode"
    }
    
    enum class ThemeMode {
        SYSTEM, LIGHT, DARK
    }
    
    private val _storeId = MutableStateFlow(prefs.getString("store_id", null))
    val storeId: StateFlow<String?> = _storeId.asStateFlow()

    fun saveStoreId(id: String) {
        prefs.edit { putString("store_id", id) }
        _storeId.value = id
    }

    fun getAppId(): String {
        val existingId = prefs.getString("app_id", null)
        if (existingId != null) return existingId
        
        val newId = java.util.UUID.randomUUID().toString().substring(0, 8).uppercase()
        prefs.edit { putString("app_id", newId) }
        return newId
    }

    fun setActivated(activated: Boolean) {
        prefs.edit { putBoolean(KEY_ACTIVATED, activated) }
    }
    
    fun isActivated(): Boolean {
        return prefs.getBoolean(KEY_ACTIVATED, false)
    }

    fun isDatabaseInitialized(): Boolean {
        return prefs.getBoolean(KEY_DB_INITIALIZED, false)
    }

    fun setDatabaseInitialized(initialized: Boolean) {
        prefs.edit { putBoolean(KEY_DB_INITIALIZED, initialized) }
    }
    
    // Theme preference methods
    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    fun getThemeMode(): ThemeMode {
        val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name)
        return try {
            ThemeMode.valueOf(modeName ?: ThemeMode.LIGHT.name)
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _themeMode.value = mode
    }
}
