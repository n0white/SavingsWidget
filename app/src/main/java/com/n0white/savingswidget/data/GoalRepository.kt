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
    }

    val goalFlow: Flow<Goal> = context.dataStore.data.map { prefs ->
        Goal(
            name = prefs[KEY_NAME] ?: "Savings Goal",
            emoji = prefs[KEY_EMOJI] ?: "💰",
            savedAmount = prefs[KEY_SAVED] ?: 0.0,
            targetAmount = prefs[KEY_TARGET] ?: 1000.0,
            currency = prefs[KEY_CURRENCY] ?: "$",
            isWavy = prefs[KEY_IS_WAVY] ?: true,
            startOfMonthAmount = prefs[KEY_MONTH_START_AMOUNT] ?: 0.0,
            lastUpdateMonth = prefs[KEY_LAST_UPDATE_MONTH] ?: -1
        )
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
            
            // Monthly efficiency logic
            if (currentMonth != storedMonth) {
                // New month detected
                prefs[KEY_LAST_UPDATE_MONTH] = currentMonth
                prefs[KEY_MONTH_START_AMOUNT] = storedSavedAmount
            } else if (prefs[KEY_MONTH_START_AMOUNT] == null) {
                // First time setup
                prefs[KEY_MONTH_START_AMOUNT] = goal.savedAmount
            }
        }
    }
}
