package com.example.savingswidget.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.savingswidget.data.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "savings")

class GoalRepository(val context: Context) {

    companion object {
        val KEY_NAME = stringPreferencesKey("goal_name")
        val KEY_EMOJI = stringPreferencesKey("goal_emoji")
        val KEY_SAVED = doublePreferencesKey("goal_saved")
        val KEY_TARGET = doublePreferencesKey("goal_target")
        val KEY_CURRENCY = stringPreferencesKey("goal_currency")
    }

    val goalFlow: Flow<Goal> = context.dataStore.data.map { prefs ->
        Goal(
            name = prefs[KEY_NAME] ?: "Savings Goal",
            emoji = prefs[KEY_EMOJI] ?: "💰",
            savedAmount = prefs[KEY_SAVED] ?: 0.0,
            targetAmount = prefs[KEY_TARGET] ?: 1000.0,
            currency = prefs[KEY_CURRENCY] ?: "$"
        )
    }

    suspend fun updateGoal(goal: Goal) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME] = goal.name
            prefs[KEY_EMOJI] = goal.emoji
            prefs[KEY_SAVED] = goal.savedAmount
            prefs[KEY_TARGET] = goal.targetAmount
            prefs[KEY_CURRENCY] = goal.currency
        }
    }
}
