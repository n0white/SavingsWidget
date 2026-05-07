package com.n0white.n0widgets.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.compose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetPreviewManager {
    fun updateWidgetPreviews(context: Context) {
        // API 35 (Android 15) is required for setWidgetPreview
        if (Build.VERSION.SDK_INT < 35) return

        val scope = CoroutineScope(Dispatchers.Main)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        scope.launch {
            // Generated Preview for CounterWidget
            try {
                val counterWidget = CounterWidget()
                // Розмір 160x160 краще підходить для віджета 2x2
                val counterRemoteViews = counterWidget.compose(
                    context = context,
                    size = DpSize(160.dp, 160.dp)
                )
                val success = appWidgetManager.setWidgetPreview(
                    ComponentName(context, CounterWidgetReceiver::class.java),
                    AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                    counterRemoteViews
                )
                Log.d("WidgetPreview", "Counter preview set: $success")
            } catch (e: Exception) {
                Log.e("WidgetPreview", "Error setting Counter preview", e)
            }

            // Generated Preview for SavingsWidget
            try {
                val savingsWidget = SavingsWidget()
                val savingsRemoteViews = savingsWidget.compose(
                    context = context,
                    size = DpSize(160.dp, 160.dp)
                )
                val success = appWidgetManager.setWidgetPreview(
                    ComponentName(context, SavingsWidgetReceiver::class.java),
                    AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                    savingsRemoteViews
                )
                Log.d("WidgetPreview", "Savings preview set: $success")
            } catch (e: Exception) {
                Log.e("WidgetPreview", "Error setting Savings preview", e)
            }

            // Generated Preview for CalendarWidget
            try {
                val calendarWidget = CalendarWidget()
                val calendarRemoteViews = calendarWidget.compose(
                    context = context,
                    size = DpSize(320.dp, 160.dp)
                )
                val success = appWidgetManager.setWidgetPreview(
                    ComponentName(context, CalendarWidgetReceiver::class.java),
                    AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                    calendarRemoteViews
                )
                Log.d("WidgetPreview", "Calendar preview set: $success")
            } catch (e: Exception) {
                Log.e("WidgetPreview", "Error setting Calendar preview", e)
            }
        }
    }
}
