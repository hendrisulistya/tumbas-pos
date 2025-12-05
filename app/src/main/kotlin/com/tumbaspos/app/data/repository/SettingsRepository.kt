package com.tumbaspos.app.data.repository

import android.content.Context
import androidx.core.content.edit
import com.tumbaspos.app.domain.model.R2Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    private val _r2Config = MutableStateFlow(loadR2Config())
    val r2Config: StateFlow<R2Config?> = _r2Config.asStateFlow()

    private fun loadR2Config(): R2Config? {
        val accountId = prefs.getString("r2_account_id", null)
        val accessKey = prefs.getString("r2_access_key", null)
        val secretKey = prefs.getString("r2_secret_key", null)
        val bucketName = prefs.getString("r2_bucket_name", null)

        return if (accountId != null && accessKey != null && secretKey != null && bucketName != null) {
            R2Config(accountId, accessKey, secretKey, bucketName)
        } else {
            null
        }
    }

    fun saveR2Config(config: R2Config) {
        prefs.edit {
            putString("r2_account_id", config.accountId)
            putString("r2_access_key", config.accessKeyId)
            putString("r2_secret_key", config.secretAccessKey)
            putString("r2_bucket_name", config.bucketName)
        }
        _r2Config.value = config
    }
    
    fun clearR2Config() {
        prefs.edit {
            remove("r2_account_id")
            remove("r2_access_key")
            remove("r2_secret_key")
            remove("r2_bucket_name")
        }
        _r2Config.value = null
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
        prefs.edit { putBoolean("is_activated", activated) }
    }
    
    fun isActivated(): Boolean {
        return prefs.getBoolean("is_activated", false)
    }

    fun isDatabaseInitialized(): Boolean {
        return prefs.getBoolean("is_database_initialized", false)
    }

    fun setDatabaseInitialized(initialized: Boolean) {
        prefs.edit { putBoolean("is_database_initialized", initialized) }
    }
}
