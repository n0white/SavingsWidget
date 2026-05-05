package com.n0white.n0widgets.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CounterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CounterWidget()

    private val scope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        
        if (action == MidnightUpdater.ACTION_REFRESH_COUNTER) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    glanceAppWidget.updateAll(context)
                    MidnightUpdater.schedule(context)
                } finally {
                    pendingResult.finish()
                }
            }
        } else if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Only reschedule the next midnight update without refreshing UI
            MidnightUpdater.schedule(context)
        } else if (action == Intent.ACTION_CONFIGURATION_CHANGED) {
            scope.launch {
                glanceAppWidget.updateAll(context)
            }
        }
    }
}
