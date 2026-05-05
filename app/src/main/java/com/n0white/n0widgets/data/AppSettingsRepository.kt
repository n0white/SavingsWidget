package com.n0white.n0widgets.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

class AppSettingsRepository(private val context: Context) {

    companion object {
        val KEY_THEME_BACKGROUND = booleanPreferencesKey("theme_background_enabled")
    }

    val isThemeBackgroundEnabled: Flow<Boolean> = context.appSettingsDataStore.data.map { prefs ->
        prefs[KEY_THEME_BACKGROUND] ?: false
    }

    suspend fun setThemeBackgroundEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[KEY_THEME_BACKGROUND] = enabled
        }
    }
}
