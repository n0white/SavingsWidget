package com.n0white.n0widgets.ui.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.SizeMode
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.n0white.n0widgets.R
import com.n0white.n0widgets.data.GoalRepository
import com.n0white.n0widgets.data.model.Goal
import kotlinx.coroutines.flow.first

class SavingsWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = GoalRepository(context)

        provideContent {
            val goal by repo.goalFlow.collectAsState(
                initial = Goal(
                    name = context.getString(R.string.default_goal_name),
                    emoji = "💰",
                    savedAmount = 0.0,
                    targetAmount = 0.0
                )
            )
            SavingsWidgetContent(goal = goal)
        }
    }
}
