package com.n0white.n0widgets.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.SizeMode
import androidx.glance.state.GlanceStateDefinition
import com.n0white.n0widgets.R
import com.n0white.n0widgets.data.AppSettingsRepository
import kotlinx.coroutines.flow.first
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.currentState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

class CalendarWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appSettings = AppSettingsRepository(context)
        
        provideContent {
            val useFahrenheit by appSettings.useFahrenheit.collectAsState(initial = false)
            val isFirstDayMonday by appSettings.isFirstDayMonday.collectAsState(initial = true)
            val events = getTodayEvents(context)

            val prefs = currentState<Preferences>()
            val temp = prefs[intPreferencesKey("temp")]
            val iconResId = prefs[intPreferencesKey("iconResId")]
            
            val weatherInfo = if (temp != null && iconResId != null) {
                WeatherInfo(temp, iconResId)
            } else null

            CalendarWidgetContent(
                weatherInfo = weatherInfo,
                events = events,
                useFahrenheit = useFahrenheit,
                isMondayFirst = isFirstDayMonday
            )
        }
    }

    suspend fun fetchWeather(context: Context): WeatherInfo? {
        val apiKey = "5dbfcfc9c946dc7aa07acaf16b36abd8"
        val appSettings = AppSettingsRepository(context)
        val city = appSettings.weatherCity.first()
        val useFahrenheit = appSettings.useFahrenheit.first()
        val units = if (useFahrenheit) "imperial" else "metric"

        if (city.isNullOrBlank()) {
            return null
        }

        return try {
            val response = WeatherService.api.getWeatherByCity(
                cityName = city,
                apiKey = apiKey,
                units = units
            )

            WeatherInfo(
                temp = response.main.temp.toInt(),
                iconResId = mapIconToDrawable(response.weather.firstOrNull()?.icon ?: "")
            )
        } catch (e: Exception) {
            Log.e("CalendarWidget", "Error fetching weather for $city", e)
            null
        }
    }

    private fun mapIconToDrawable(icon: String): Int {
        return when (icon) {
            "01d" -> R.drawable.sunny
            "01n" -> R.drawable.clear_night
            "02d" -> R.drawable.mostly_sunny
            "02n" -> R.drawable.mostly_clear_night
            "03d", "03n", "04d", "04n" -> R.drawable.cloudy
            "09d", "09n", "10d", "10n" -> R.drawable.heavy_rain
            "11d", "11n" -> R.drawable.strong_thunderstorms
            "13d", "13n" -> R.drawable.heavy_snow
            "50d", "50n" -> R.drawable.windy
            else -> R.drawable.cloudy
        }
    }
}
