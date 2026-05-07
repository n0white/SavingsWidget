package com.n0white.n0widgets.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.n0white.n0widgets.data.model.Counter
import com.n0white.n0widgets.data.model.CounterFormat
import com.n0white.n0widgets.ui.widget.CounterWidget
import com.n0white.n0widgets.ui.widget.WidgetPreviewManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.counterDataStore by preferencesDataStore(name = "settings_counter")

class CounterRepository(val context: Context) {

    companion object {
        val KEY_NAME = stringPreferencesKey("counter_name")
        val KEY_EMOJI = stringPreferencesKey("counter_emoji")
        val KEY_START_DATE = stringPreferencesKey("counter_start_date")
        val KEY_TARGET_DATE = stringPreferencesKey("counter_target_date")
        val KEY_FORMAT_MODE = stringPreferencesKey("counter_format_mode")
        val KEY_IS_INFINITE = booleanPreferencesKey("counter_is_infinite")
        val KEY_IS_WAVY = booleanPreferencesKey("counter_is_wavy")
        val KEY_BG_IMAGE = stringPreferencesKey("counter_bg_image_path")
        val KEY_IS_BLUR_ENABLED = booleanPreferencesKey("counter_is_blur_enabled")
        val KEY_COLOR_PRIMARY = intPreferencesKey("counter_color_primary")
        val KEY_COLOR_PRIMARY_INVERSE = intPreferencesKey("counter_color_primary_inverse")
        val KEY_COLOR_ON_SURFACE = intPreferencesKey("counter_color_on_surface")
        val KEY_COLOR_ON_SURFACE_INVERSE = intPreferencesKey("counter_color_on_surface_inverse")
        val KEY_COLOR_SECONDARY_CONTAINER = intPreferencesKey("counter_color_secondary_container")

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
            emoji = prefs[KEY_EMOJI] ?: "⏳",
            startDate = startDate,
            targetDate = targetDate,
            formatMode = formatMode,
            isInfinite = prefs[KEY_IS_INFINITE] ?: false,
            isWavy = prefs[KEY_IS_WAVY] ?: false,
            backgroundImagePath = prefs[KEY_BG_IMAGE],
            isBlurEnabled = prefs[KEY_IS_BLUR_ENABLED] ?: false,
            customPrimary = prefs[KEY_COLOR_PRIMARY],
            customPrimaryInverse = prefs[KEY_COLOR_PRIMARY_INVERSE],
            customOnSurface = prefs[KEY_COLOR_ON_SURFACE],
            customOnSurfaceInverse = prefs[KEY_COLOR_ON_SURFACE_INVERSE],
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
            prefs[KEY_IS_INFINITE] = counter.isInfinite
            prefs[KEY_IS_WAVY] = counter.isWavy
            prefs[KEY_BG_IMAGE] = counter.backgroundImagePath ?: ""
            prefs[KEY_IS_BLUR_ENABLED] = counter.isBlurEnabled
            
            counter.customPrimary?.let { prefs[KEY_COLOR_PRIMARY] = it } ?: prefs.remove(KEY_COLOR_PRIMARY)
            counter.customPrimaryInverse?.let { prefs[KEY_COLOR_PRIMARY_INVERSE] = it } ?: prefs.remove(KEY_COLOR_PRIMARY_INVERSE)
            counter.customOnSurface?.let { prefs[KEY_COLOR_ON_SURFACE] = it } ?: prefs.remove(KEY_COLOR_ON_SURFACE)
            counter.customOnSurfaceInverse?.let { prefs[KEY_COLOR_ON_SURFACE_INVERSE] = it } ?: prefs.remove(KEY_COLOR_ON_SURFACE_INVERSE)
            counter.customSecondaryContainer?.let { prefs[KEY_COLOR_SECONDARY_CONTAINER] = it } ?: prefs.remove(KEY_COLOR_SECONDARY_CONTAINER)
        }

        // Trigger immediate widget update
        CounterWidget().updateAll(context)
        WidgetPreviewManager.updateWidgetPreviews(context)
    }
}
