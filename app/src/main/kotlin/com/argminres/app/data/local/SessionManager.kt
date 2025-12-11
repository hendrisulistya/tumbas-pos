package com.argminres.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class SessionManager(private val context: Context) {
    
    companion object {
        private val EMPLOYER_ID_KEY = longPreferencesKey("employer_id")
    }
    
    /**
     * Save the logged-in employer ID
     */
    suspend fun saveEmployerId(employerId: Long) {
        context.dataStore.edit { preferences ->
            preferences[EMPLOYER_ID_KEY] = employerId
        }
    }
    
    /**
     * Get the saved employer ID as a Flow
     */
    fun getEmployerId(): Flow<Long?> {
        return context.dataStore.data.map { preferences ->
            preferences[EMPLOYER_ID_KEY]
        }
    }
    
    /**
     * Clear the saved session (logout)
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(EMPLOYER_ID_KEY)
        }
    }
}
