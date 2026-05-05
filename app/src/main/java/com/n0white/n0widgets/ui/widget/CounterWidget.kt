package com.n0white.n0widgets.ui.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.SizeMode
import androidx.glance.state.GlanceStateDefinition
import com.n0white.n0widgets.R
import com.n0white.n0widgets.data.CounterRepository
import com.n0white.n0widgets.data.AppSettingsRepository
import com.n0white.n0widgets.data.model.Counter
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class CounterWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = CounterRepository(context)
        val settingsRepo = AppSettingsRepository(context)

        provideContent {
            val counter by repo.counterFlow.collectAsState(
                initial = Counter(
                    name = context.getString(R.string.default_counter_name),
                    emoji = "📅",
                    startDate = LocalDate.now(),
                    targetDate = LocalDate.now().plusDays(1)
                )
            )
            val isThemeBackgroundEnabled by settingsRepo.isThemeBackgroundEnabled.collectAsState(initial = false)
            CounterWidgetContent(counter = counter, isThemeBackgroundEnabled = isThemeBackgroundEnabled)
        }
    }
}
