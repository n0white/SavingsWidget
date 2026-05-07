package com.n0white.n0widgets.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

class AppSettingsRepository(private val context: Context) {

    companion object {
        val KEY_THEME_BACKGROUND = booleanPreferencesKey("theme_background_enabled")
        val KEY_WEATHER_CITY = stringPreferencesKey("weather_city")
        val KEY_USE_FAHRENHEIT = booleanPreferencesKey("use_fahrenheit")
        val KEY_FIRST_DAY_MONDAY = booleanPreferencesKey("first_day_monday")
    }

    val isThemeBackgroundEnabled: Flow<Boolean> = context.appSettingsDataStore.data.map { prefs ->
        prefs[KEY_THEME_BACKGROUND] ?: false
    }

    val weatherCity: Flow<String?> = context.appSettingsDataStore.data.map { prefs ->
        prefs[KEY_WEATHER_CITY]
    }

    val useFahrenheit: Flow<Boolean> = context.appSettingsDataStore.data.map { prefs ->
        prefs[KEY_USE_FAHRENHEIT] ?: false
    }

    val isFirstDayMonday: Flow<Boolean> = context.appSettingsDataStore.data.map { prefs ->
        prefs[KEY_FIRST_DAY_MONDAY] ?: true
    }

    suspend fun setThemeBackgroundEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[KEY_THEME_BACKGROUND] = enabled
        }
    }

    suspend fun setWeatherCity(city: String) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[KEY_WEATHER_CITY] = city
        }
    }

    suspend fun setUseFahrenheit(enabled: Boolean) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[KEY_USE_FAHRENHEIT] = enabled
        }
    }

    suspend fun setFirstDayMonday(enabled: Boolean) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[KEY_FIRST_DAY_MONDAY] = enabled
        }
    }
}
