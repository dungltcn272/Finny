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
import kotlinx.coroutines.flow.first
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
    private val pendingNotificationsKey = stringPreferencesKey("pending_notifications_queue")
    // --- KEY MỚI CHO HỘP THƯ ĐẾN ---
    private val bankNotificationInboxKey = stringPreferencesKey("bank_notification_inbox")


    val getSelectedCurrency: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[selectedCurrencyKey] ?: "VND"
        }

    val usernameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[usernameKey]
        }

    // --- FLOW MỚI ĐỂ LẮNG NGHE HỘP THƯ ĐẾN ---
    val bankNotificationInboxFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[bankNotificationInboxKey] ?: ""
        }


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

    suspend fun addPendingNotification(prompt: String) {
        context.dataStore.edit { settings ->
            val currentQueue = settings[pendingNotificationsKey] ?: ""
            val newQueue = if (currentQueue.isEmpty()) prompt else "$currentQueue|||$prompt"
            settings[pendingNotificationsKey] = newQueue
        }
    }

    suspend fun getAndClearPendingNotifications(): String {
        val pending = context.dataStore.data.map { it[pendingNotificationsKey] ?: "" }.first()
        if (pending.isNotEmpty()) {
            context.dataStore.edit { settings ->
                settings.remove(pendingNotificationsKey)
            }
        }
        return pending
    }

    suspend fun addRawBankNotification(rawNotification: String) {
        context.dataStore.edit { settings ->
            val currentInbox = settings[bankNotificationInboxKey] ?: ""
            val newInbox = if (currentInbox.isEmpty()) rawNotification else "$currentInbox|||$rawNotification"
            settings[bankNotificationInboxKey] = newInbox
        }
    }

    suspend fun saveBankNotificationInbox(newInbox: String) {
        context.dataStore.edit { settings ->
            if (newInbox.isEmpty()) {
                settings.remove(bankNotificationInboxKey)
            } else {
                settings[bankNotificationInboxKey] = newInbox
            }
        }
    }
}

