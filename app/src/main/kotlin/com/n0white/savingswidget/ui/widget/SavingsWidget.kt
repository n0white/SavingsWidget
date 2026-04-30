package com.n0white.savingswidget.ui.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.SizeMode
import com.n0white.savingswidget.R
import com.n0white.savingswidget.data.GoalRepository
import com.n0white.savingswidget.data.model.Goal

class SavingsWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = GoalRepository(context)

        provideContent {
            val context = LocalContext.current
            val goal by repo.goalFlow.collectAsState(
                initial = Goal(
                    name = context.getString(R.string.state_loading),
                    emoji = context.getString(R.string.emoji_loading),
                    savedAmount = 0.0,
                    targetAmount = 1.0,
                    currency = context.getString(R.string.default_currency)
                )
            )
            SavingsWidgetContent(goal = goal)
        }
    }
}
