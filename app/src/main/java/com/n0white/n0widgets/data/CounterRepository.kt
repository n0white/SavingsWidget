package com.n0white.n0widgets.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.n0white.n0widgets.data.model.Counter
import com.n0white.n0widgets.data.model.CounterFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.counterDataStore by preferencesDataStore(name = "counter")

class CounterRepository(val context: Context) {

    companion object {
        val KEY_NAME = stringPreferencesKey("counter_name")
        val KEY_EMOJI = stringPreferencesKey("counter_emoji")
        val KEY_START_DATE = stringPreferencesKey("counter_start_date")
        val KEY_TARGET_DATE = stringPreferencesKey("counter_target_date")
        val KEY_FORMAT_MODE = stringPreferencesKey("counter_format_mode")
        val KEY_IS_WAVY = booleanPreferencesKey("counter_is_wavy")
        val KEY_BG_IMAGE = stringPreferencesKey("bg_image_path")
        val KEY_IS_BLUR_ENABLED = booleanPreferencesKey("is_blur_enabled")
        val KEY_COLOR_PRIMARY = intPreferencesKey("color_primary")
        val KEY_COLOR_ON_SURFACE = intPreferencesKey("color_on_surface")
        val KEY_COLOR_SECONDARY_CONTAINER = intPreferencesKey("color_secondary_container")
        
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    }

    val counterFlow: Flow<Counter> = context.counterDataStore.data.map { prefs ->
        val startDateStr = prefs[KEY_START_DATE]
        val targetDateStr = prefs[KEY_TARGET_DATE]
        
        val startDate = startDateStr?.let { LocalDate.parse(it, formatter) } ?: LocalDate.now()
        val targetDate = targetDateStr?.let { LocalDate.parse(it, formatter) } ?: LocalDate.now().plusMonths(1)
        
        val formatModeStr = prefs[KEY_FORMAT_MODE] ?: CounterFormat.DAYS_ONLY.name
        val formatMode = try { CounterFormat.valueOf(formatModeStr) } catch (e: Exception) { CounterFormat.DAYS_ONLY }

        Counter(
            name = prefs[KEY_NAME] ?: "",
            emoji = prefs[KEY_EMOJI] ?: "📅",
            startDate = startDate,
            targetDate = targetDate,
            formatMode = formatMode,
            isWavy = prefs[KEY_IS_WAVY] ?: false,
            backgroundImagePath = prefs[KEY_BG_IMAGE],
            isBlurEnabled = prefs[KEY_IS_BLUR_ENABLED] ?: false,
            customPrimary = prefs[KEY_COLOR_PRIMARY],
            customOnSurface = prefs[KEY_COLOR_ON_SURFACE],
            customSecondaryContainer = prefs[KEY_COLOR_SECONDARY_CONTAINER]
        )
    }

    suspend fun resetCounter() {
        context.counterDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun updateCounter(counter: Counter) {
        context.counterDataStore.edit { prefs ->
            prefs[KEY_NAME] = counter.name
            prefs[KEY_EMOJI] = counter.emoji
            prefs[KEY_START_DATE] = counter.startDate.format(formatter)
            prefs[KEY_TARGET_DATE] = counter.targetDate.format(formatter)
            prefs[KEY_FORMAT_MODE] = counter.formatMode.name
            prefs[KEY_IS_WAVY] = counter.isWavy
            prefs[KEY_BG_IMAGE] = counter.backgroundImagePath ?: ""
            prefs[KEY_IS_BLUR_ENABLED] = counter.isBlurEnabled
            
            counter.customPrimary?.let { prefs[KEY_COLOR_PRIMARY] = it } ?: prefs.remove(KEY_COLOR_PRIMARY)
            counter.customOnSurface?.let { prefs[KEY_COLOR_ON_SURFACE] = it } ?: prefs.remove(KEY_COLOR_ON_SURFACE)
            counter.customSecondaryContainer?.let { prefs[KEY_COLOR_SECONDARY_CONTAINER] = it } ?: prefs.remove(KEY_COLOR_SECONDARY_CONTAINER)
        }
    }
}
