package com.n0white.savingswidget.ui.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

// ui/widget/SavingsWidgetReceiver.kt
class SavingsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = SavingsWidget()
}
