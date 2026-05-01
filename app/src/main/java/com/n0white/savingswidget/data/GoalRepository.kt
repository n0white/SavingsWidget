package com.n0white.savingswidget.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.n0white.savingswidget.data.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore by preferencesDataStore(name = "savings")

class GoalRepository(val context: Context) {

    companion object {
        val KEY_NAME = stringPreferencesKey("goal_name")
        val KEY_EMOJI = stringPreferencesKey("goal_emoji")
        val KEY_SAVED = doublePreferencesKey("goal_saved")
        val KEY_TARGET = doublePreferencesKey("goal_target")
        val KEY_CURRENCY = stringPreferencesKey("goal_currency")
        val KEY_IS_WAVY = booleanPreferencesKey("goal_is_wavy")
        val KEY_MONTH_START_AMOUNT = doublePreferencesKey("month_start_amount")
        val KEY_LAST_UPDATE_MONTH = intPreferencesKey("last_update_month")
        val KEY_BG_IMAGE = stringPreferencesKey("bg_image_path")
        val KEY_COLOR_PRIMARY = intPreferencesKey("color_primary")
        val KEY_COLOR_ON_SURFACE = intPreferencesKey("color_on_surface")
        val KEY_COLOR_SECONDARY_CONTAINER = intPreferencesKey("color_secondary_container")
    }

    val goalFlow: Flow<Goal> = context.dataStore.data.map { prefs ->
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
            name = prefs[KEY_NAME] ?: "Savings Goal",
            emoji = prefs[KEY_EMOJI] ?: "💰",
            savedAmount = savedAmount,
            targetAmount = prefs[KEY_TARGET] ?: 1000.0,
            currency = prefs[KEY_CURRENCY] ?: "$",
            isWavy = prefs[KEY_IS_WAVY] ?: true,
            startOfMonthAmount = effectiveStartAmount,
            lastUpdateMonth = if (currentMonth != storedMonth && storedMonth != -1) currentMonth else storedMonth,
            backgroundImagePath = prefs[KEY_BG_IMAGE],
            customPrimary = prefs[KEY_COLOR_PRIMARY],
            customOnSurface = prefs[KEY_COLOR_ON_SURFACE],
            customSecondaryContainer = prefs[KEY_COLOR_SECONDARY_CONTAINER]
        )
    }

    suspend fun resetGoal() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun updateGoal(goal: Goal) {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentPrefs = context.dataStore.data.first()
        val storedMonth = currentPrefs[KEY_LAST_UPDATE_MONTH] ?: -1
        val storedSavedAmount = currentPrefs[KEY_SAVED] ?: 0.0

        context.dataStore.edit { prefs ->
            prefs[KEY_NAME] = goal.name
            prefs[KEY_EMOJI] = goal.emoji
            prefs[KEY_SAVED] = goal.savedAmount
            prefs[KEY_TARGET] = goal.targetAmount
            prefs[KEY_CURRENCY] = goal.currency
            prefs[KEY_IS_WAVY] = goal.isWavy
            prefs[KEY_BG_IMAGE] = goal.backgroundImagePath ?: ""
            
            goal.customPrimary?.let { prefs[KEY_COLOR_PRIMARY] = it } ?: prefs.remove(KEY_COLOR_PRIMARY)
            goal.customOnSurface?.let { prefs[KEY_COLOR_ON_SURFACE] = it } ?: prefs.remove(KEY_COLOR_ON_SURFACE)
            goal.customSecondaryContainer?.let { prefs[KEY_COLOR_SECONDARY_CONTAINER] = it } ?: prefs.remove(KEY_COLOR_SECONDARY_CONTAINER)
            
            // Monthly efficiency logic
            if (currentMonth != storedMonth) {
                prefs[KEY_LAST_UPDATE_MONTH] = currentMonth
                prefs[KEY_MONTH_START_AMOUNT] = storedSavedAmount
            } else if (prefs[KEY_MONTH_START_AMOUNT] == null) {
                prefs[KEY_MONTH_START_AMOUNT] = goal.savedAmount
            }
        }
    }
}
