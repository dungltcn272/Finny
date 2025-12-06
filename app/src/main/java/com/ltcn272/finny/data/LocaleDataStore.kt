package com.ltcn272.finny.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "app_prefs"
private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

object LocaleDataStore {
    private val LOCALE_KEY = stringPreferencesKey("app_locale")

    fun localeFlow(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[LOCALE_KEY] }

    suspend fun saveLocale(context: Context, localeTag: String?) {
        context.dataStore.edit { prefs ->
            if (localeTag == null) prefs.remove(LOCALE_KEY) else prefs[LOCALE_KEY] = localeTag
        }
    }
}

