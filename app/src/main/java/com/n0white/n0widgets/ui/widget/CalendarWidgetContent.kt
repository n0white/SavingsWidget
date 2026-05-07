package com.n0white.n0widgets.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.TextAlign
import androidx.glance.color.ColorProviders
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.*
import android.provider.CalendarContract
import android.content.Context
import androidx.glance.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import com.n0white.n0widgets.R

@Composable
fun CalendarWidgetContent(
    weatherInfo: WeatherInfo? = null,
    events: List<String> = emptyList(),
    useFahrenheit: Boolean = false,
    isMondayFirst: Boolean = true
) {
    val today = LocalDate.now()
    val context = LocalContext.current
    val colors = GlanceTheme.colors
    
    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.widgetBackground)
                .cornerRadius(24.dp)
                .padding(16.dp)
                .clickable(actionStartActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("content://com.android.calendar/time/")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Date, Weather and Events
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Big Date
                        Text(
                            text = today.dayOfMonth.toString(),
                            style = TextStyle(
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        )

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // Weather and Month
                        Column {
                            // Weather
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    provider = ImageProvider(weatherInfo?.iconResId ?: R.drawable.cloudy),
                                    contentDescription = null,
                                    modifier = GlanceModifier.size(24.dp)
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                Text(
                                    text = weatherInfo?.let { "${it.temp}${if (useFahrenheit) "°F" else "°C"}" } ?: "—${if (useFahrenheit) "°F" else "°C"}",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.onSurface
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.height(4.dp))

                            // Month name and Day of week
                            Text(
                                text = "${today.month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())}, ${today.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())}",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    // Events Section
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(colors.secondaryContainer)
                            .cornerRadius(12.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (events.isEmpty()) {
                                Text(
                                    text = context.getString(R.string.no_events_today),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = colors.onSecondaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            } else {
                                events.take(2).forEach { event ->
                                    Text(
                                        text = event,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.onSecondaryContainer,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = GlanceModifier.width(16.dp))
                
                // Right Side: Month Grid
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    horizontalAlignment = Alignment.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Year Header
                    Text(
                        text = today.year.toString(),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            textAlign = TextAlign.End
                        ),
                        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp, end = 7.dp)
                    )
                    
                    CalendarGrid(today, colors, isMondayFirst)
                }
            }
        }
    }
}

fun getTodayEvents(context: Context): List<String> {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
        return emptyList()
    }

    val events = mutableListOf<String>()
    val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val projection = arrayOf(
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART
    )

    val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ? AND ${CalendarContract.Events.DELETED} = 0"
    val selectionArgs = arrayOf(startOfDay.toString(), endOfDay.toString())

    try {
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC"
        )?.use { cursor ->
            val titleIndex = cursor.getColumnIndex(CalendarContract.Events.TITLE)
            val startIndex = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
            val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            
            while (cursor.moveToNext()) {
                val title = cursor.getString(titleIndex)
                val startTimeMillis = cursor.getLong(startIndex)
                val timePrefix = if (startTimeMillis > 0) {
                    val time = java.time.Instant.ofEpochMilli(startTimeMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                    "${time.format(timeFormatter)} "
                } else ""
                
                events.add("$timePrefix$title")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return events
}

@Composable
private fun CalendarGrid(today: LocalDate, colors: ColorProviders, isMondayFirst: Boolean) {
    val days = getDaysOfMonth(today, isMondayFirst)
    val weekDays = if (isMondayFirst) {
        listOf("M", "T", "W", "T", "F", "S", "S")
    } else {
        listOf("S", "M", "T", "W", "T", "F", "S")
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Weekday Headers
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = GlanceModifier.height(4.dp))
        
        // Days Grid
        val rows = days.chunked(7)
        rows.forEach { rowDays ->
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                rowDays.forEach { date ->
                    Box(
                        modifier = GlanceModifier.defaultWeight().height(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val isToday = date == today
                            Box(
                                modifier = if (isToday) {
                                    GlanceModifier
                                        .size(20.dp)
                                        .background(colors.primary)
                                        .cornerRadius(10.dp)
                                } else GlanceModifier,
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isToday) colors.onPrimary else colors.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
                // Fill empty cells if row is not full
                if (rowDays.size < 7) {
                    repeat(7 - rowDays.size) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                }
            }
        }
    }
}

private fun getDaysOfMonth(date: LocalDate, isMondayFirst: Boolean): List<LocalDate?> {
    val firstDayOfMonth = date.withDayOfMonth(1)
    val days = mutableListOf<LocalDate?>()
    
    // Sunday = 7 in java.time
    val dowValue = firstDayOfMonth.dayOfWeek.value
    val firstDayOfWeek = if (isMondayFirst) {
        dowValue - 1 // 0 = Mon, 1 = Tue ...
    } else {
        if (dowValue == 7) 0 else dowValue // 0 = Sun, 1 = Mon ...
    }
    
    repeat(firstDayOfWeek) {
        days.add(null)
    }
    
    for (i in 1..date.lengthOfMonth()) {
        days.add(firstDayOfMonth.withDayOfMonth(i))
    }
    
    return days
}
