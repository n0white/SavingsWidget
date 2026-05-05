package com.n0white.n0widgets.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.n0white.n0widgets.data.model.Goal
import com.n0white.n0widgets.ui.widget.SavingsWidget
import com.n0white.n0widgets.ui.widget.WidgetPreviewManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.goalDataStore by preferencesDataStore(name = "settings_savings")

class GoalRepository(val context: Context) {

    companion object {
        val KEY_NAME = stringPreferencesKey("goal_name")
        val KEY_EMOJI = stringPreferencesKey("goal_emoji")
        val KEY_SAVED = doublePreferencesKey("goal_saved")
        val KEY_TARGET = doublePreferencesKey("goal_target")
        val KEY_CURRENCY = stringPreferencesKey("goal_currency")
        val KEY_IS_WAVY = booleanPreferencesKey("goal_is_wavy")
        val KEY_MONTH_START_AMOUNT = doublePreferencesKey("goal_month_start_amount")
        val KEY_LAST_UPDATE_MONTH = intPreferencesKey("goal_last_update_month")
        val KEY_BG_IMAGE = stringPreferencesKey("goal_bg_image_path")
        val KEY_IS_BLUR_ENABLED = booleanPreferencesKey("goal_is_blur_enabled")
        val KEY_IS_PLUS_BUTTON_ENABLED = booleanPreferencesKey("goal_is_plus_button_enabled")
        val KEY_COLOR_PRIMARY = intPreferencesKey("goal_color_primary")
        val KEY_COLOR_PRIMARY_INVERSE = intPreferencesKey("goal_color_primary_inverse")
        val KEY_COLOR_ON_SURFACE = intPreferencesKey("goal_color_on_surface")
        val KEY_COLOR_ON_SURFACE_INVERSE = intPreferencesKey("goal_color_on_surface_inverse")
        val KEY_COLOR_SECONDARY_CONTAINER = intPreferencesKey("goal_color_secondary_container")
    }

    val goalFlow: Flow<Goal> = context.goalDataStore.data.map { prefs ->
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val storedMonth = prefs[KEY_LAST_UPDATE_MONTH] ?: -1
        val savedAmount = prefs[KEY_SAVED] ?: 0.0
        
        // Automatically detect month change even before an update is saved
        val effectiveStartAmount = if (currentMonth != storedMonth && storedMonth != -1) {
            savedAmount
        } else {
            prefs[KEY_MONTH_START_AMOUNT] ?: 0.0
        }

        Goal(
            name = prefs[KEY_NAME] ?: "",
            emoji = prefs[KEY_EMOJI] ?: "💰",
            savedAmount = savedAmount,
            targetAmount = prefs[KEY_TARGET] ?: 0.0,
            currency = prefs[KEY_CURRENCY] ?: "$",
            isWavy = prefs[KEY_IS_WAVY] ?: false,
            startOfMonthAmount = effectiveStartAmount,
            lastUpdateMonth = if (currentMonth != storedMonth && storedMonth != -1) currentMonth else storedMonth,
            backgroundImagePath = prefs[KEY_BG_IMAGE],
            isBlurEnabled = prefs[KEY_IS_BLUR_ENABLED] ?: false,
            isPlusButtonEnabled = prefs[KEY_IS_PLUS_BUTTON_ENABLED] ?: false,
            customPrimary = prefs[KEY_COLOR_PRIMARY],
            customPrimaryInverse = prefs[KEY_COLOR_PRIMARY_INVERSE],
            customOnSurface = prefs[KEY_COLOR_ON_SURFACE],
            customOnSurfaceInverse = prefs[KEY_COLOR_ON_SURFACE_INVERSE],
            customSecondaryContainer = prefs[KEY_COLOR_SECONDARY_CONTAINER]
        )
    }

    suspend fun resetGoal() {
        context.goalDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun updateGoal(goal: Goal) {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentPrefs = context.goalDataStore.data.first()
        val storedMonth = currentPrefs[KEY_LAST_UPDATE_MONTH] ?: -1
        val storedSavedAmount = currentPrefs[KEY_SAVED] ?: 0.0

        context.goalDataStore.edit { prefs ->
            prefs[KEY_NAME] = goal.name
            prefs[KEY_EMOJI] = goal.emoji
            prefs[KEY_SAVED] = goal.savedAmount
            prefs[KEY_TARGET] = goal.targetAmount
            prefs[KEY_CURRENCY] = goal.currency
            prefs[KEY_IS_WAVY] = goal.isWavy
            prefs[KEY_BG_IMAGE] = goal.backgroundImagePath ?: ""
            prefs[KEY_IS_BLUR_ENABLED] = goal.isBlurEnabled
            prefs[KEY_IS_PLUS_BUTTON_ENABLED] = goal.isPlusButtonEnabled
            
            goal.customPrimary?.let { prefs[KEY_COLOR_PRIMARY] = it } ?: prefs.remove(KEY_COLOR_PRIMARY)
            goal.customPrimaryInverse?.let { prefs[KEY_COLOR_PRIMARY_INVERSE] = it } ?: prefs.remove(KEY_COLOR_PRIMARY_INVERSE)
            goal.customOnSurface?.let { prefs[KEY_COLOR_ON_SURFACE] = it } ?: prefs.remove(KEY_COLOR_ON_SURFACE)
            goal.customOnSurfaceInverse?.let { prefs[KEY_COLOR_ON_SURFACE_INVERSE] = it } ?: prefs.remove(KEY_COLOR_ON_SURFACE_INVERSE)
            goal.customSecondaryContainer?.let { prefs[KEY_COLOR_SECONDARY_CONTAINER] = it } ?: prefs.remove(KEY_COLOR_SECONDARY_CONTAINER)

            // Monthly efficiency logic
            if (currentMonth != storedMonth) {
                prefs[KEY_LAST_UPDATE_MONTH] = currentMonth
                prefs[KEY_MONTH_START_AMOUNT] = storedSavedAmount
            } else if (prefs[KEY_MONTH_START_AMOUNT] == null) {
                prefs[KEY_MONTH_START_AMOUNT] = goal.savedAmount
            }
        }
        
        // Trigger immediate widget update
        SavingsWidget().updateAll(context)
        WidgetPreviewManager.updateWidgetPreviews(context)
    }
}
