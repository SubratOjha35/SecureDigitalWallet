package com.faith.securedigitalwallet.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// Extension property to get DataStore instance
val Context.dataStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "secure_prefs")

object PasswordManager {

    private val MASTER_PASSWORD_KEY = stringPreferencesKey("master_password")

    // Get the master password (nullable String)
    suspend fun getMasterPassword(context: Context): String? {
        return context.dataStore.data.first()[MASTER_PASSWORD_KEY]
    }

    // Save the master password
    suspend fun saveMasterPassword(context: Context, password: String) {
        context.dataStore.edit { prefs ->
            prefs[MASTER_PASSWORD_KEY] = password
        }
    }

    // Optional: Reset/change master password (just call saveMasterPassword)
    suspend fun resetMasterPassword(context: Context, newPassword: String) {
        saveMasterPassword(context, newPassword)
    }
}
