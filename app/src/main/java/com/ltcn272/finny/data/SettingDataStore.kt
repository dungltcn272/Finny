package com.ltcn272.finny.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val selectedCurrencyKey = stringPreferencesKey("selected_currency")
    private val usernameKey = stringPreferencesKey("username")
    private val enableNotificationsKey = booleanPreferencesKey("enable_notifications")
    private val authenticationEnabledKey = booleanPreferencesKey("authentication_enabled")

    val getSelectedCurrency: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[selectedCurrencyKey] ?: "VND"
        }

    // Expose username as nullable string flow. If not set, returns null.
    val getUsername: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[usernameKey]
        }

    // Boolean flows
    val getEnableNotifications: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[enableNotificationsKey] ?: true
        }

    val getAuthenticationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[authenticationEnabledKey] ?: false
        }

    suspend fun saveSelectedCurrency(currency: String) {
        context.dataStore.edit { settings ->
            settings[selectedCurrencyKey] = currency
        }
    }

    suspend fun saveUsername(name: String) {
        context.dataStore.edit { settings ->
            settings[usernameKey] = name
        }
    }

    suspend fun clearUsername() {
        context.dataStore.edit { settings ->
            settings.remove(usernameKey)
        }
    }

    suspend fun saveEnableNotifications(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[enableNotificationsKey] = enabled
        }
    }

    suspend fun saveAuthenticationEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[authenticationEnabledKey] = enabled
        }
    }
}
