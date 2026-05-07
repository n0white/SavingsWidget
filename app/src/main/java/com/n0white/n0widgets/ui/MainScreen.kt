package com.n0white.n0widgets.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.n0white.n0widgets.CalendarSettingsActivity
import com.n0white.n0widgets.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onNavigateToSavings: () -> Unit, 
    onNavigateToCounter: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current

    var isBatteryOptimized by remember { mutableStateOf(false) }
    var canScheduleExactAlarms by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            while (isActive) {
                val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
                val currentBatteryOptimized = !pm.isIgnoringBatteryOptimizations(context.packageName)
                if (isBatteryOptimized != currentBatteryOptimized) {
                    isBatteryOptimized = currentBatteryOptimized
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager =
                        context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                    val currentCanSchedule = alarmManager.canScheduleExactAlarms()
                    if (canScheduleExactAlarms != currentCanSchedule) {
                        canScheduleExactAlarms = currentCanSchedule
                    }
                }

                delay(5000)
            }
        }
        onDispose { job.cancel() }
    }

    val topShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val middleShape = RoundedCornerShape(4.dp)
    val bottomShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    val singleShape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.main_screen_select_widget_hint),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                NavigationCard(
                    title = stringResource(R.string.main_screen_savings_widget_title),
                    subtitle = stringResource(R.string.main_screen_savings_widget_subtitle),
                    icon = Icons.Outlined.Savings,
                    onClick = {
                        coroutineScope.launch {
                            delay(150)
                            onNavigateToSavings()
                        }
                    },
                    shape = topShape,
                    iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                NavigationCard(
                    title = stringResource(R.string.main_screen_counter_widget_title),
                    subtitle = stringResource(R.string.main_screen_counter_widget_subtitle),
                    icon = Icons.Outlined.HourglassEmpty,
                    onClick = {
                        coroutineScope.launch {
                            delay(150)
                            onNavigateToCounter()
                        }
                    },
                    shape = middleShape,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                NavigationCard(
                    title = stringResource(R.string.main_screen_calendar_widget_title),
                    subtitle = stringResource(R.string.main_screen_calendar_widget_subtitle),
                    icon = Icons.Outlined.CalendarToday,
                    onClick = {
                        coroutineScope.launch {
                            delay(150)
                            val intent = Intent(context, CalendarSettingsActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            context.startActivity(intent)
                        }
                    },
                    shape = bottomShape,
                    iconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.other_category),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                NavigationCard(
                    title = stringResource(R.string.settings_title),
                    subtitle = stringResource(R.string.settings_description),
                    icon = Icons.Outlined.Settings,
                    onClick = {
                        coroutineScope.launch {
                            delay(150)
                            onNavigateToSettings()
                        }
                    },
                    shape = topShape,
                    iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                NavigationCard(
                    title = stringResource(R.string.about_title),
                    subtitle = stringResource(R.string.about_description),
                    icon = Icons.Outlined.Info,
                    onClick = {
                        coroutineScope.launch {
                            delay(150)
                            onNavigateToAbout()
                        }
                    },
                    shape = bottomShape,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        if (isBatteryOptimized || !canScheduleExactAlarms) {
            Surface(
                onClick = {
                    if (isBatteryOptimized) {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                    } else if (!canScheduleExactAlarms && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                },
                shape = singleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f), MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.main_screen_background_updates_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.main_screen_background_updates_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    shape: Shape,
    iconContainerColor: Color,
    iconContentColor: Color
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceBright,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 88.dp)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconContainerColor, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconContentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
