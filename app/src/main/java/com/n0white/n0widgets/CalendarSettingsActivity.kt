package com.n0white.n0widgets

import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import com.n0white.n0widgets.data.AppSettingsRepository
import com.n0white.n0widgets.ui.theme.SavingsWidgetTheme
import com.n0white.n0widgets.ui.widget.CalendarWidget
import com.n0white.n0widgets.ui.widget.CitySuggestion
import com.n0white.n0widgets.ui.widget.WeatherService
import com.n0white.n0widgets.ui.widget.WeatherWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CalendarSettingsActivity : ComponentActivity() {
    private lateinit var appSettingsRepository: AppSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appSettingsRepository = AppSettingsRepository(this)
        enableEdgeToEdge()

        setContent {
            SavingsWidgetTheme {
                CalendarSettingsScreen(appSettingsRepository) { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSettingsScreen(
    repository: AppSettingsRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val useFahrenheit by repository.useFahrenheit.collectAsState(initial = false)
    val isFirstDayMonday by repository.isFirstDayMonday.collectAsState(initial = true)
    val weatherCityStored by repository.weatherCity.collectAsState(initial = null)
    
    var cityText by remember(weatherCityStored) { mutableStateOf(weatherCityStored ?: "") }
    var suggestions by remember { mutableStateOf<List<CitySuggestion>>(emptyList()) }
    var isSaved by remember { mutableStateOf(false) }

    val isMondayInteractionSource = remember { MutableInteractionSource() }
    val isFahrenheitInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(cityText) {
        if (cityText.length >= 3 && cityText != weatherCityStored) {
            delay(600) // Debounce
            try {
                // Використовуємо той самий API ключ, що і у віджеті
                val results = WeatherService.api.findCities(
                    query = cityText,
                    apiKey = "5dbfcfc9c946dc7aa07acaf16b36abd8"
                )
                suggestions = results
            } catch (e: Exception) {
                suggestions = emptyList()
            }
        } else {
            suggestions = emptyList()
        }
    }

    @Composable
    fun switchColors(checked: Boolean) = SwitchDefaults.colors(
        checkedThumbColor = animateColorAsState(if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline, label = "switchThumb").value,
        uncheckedThumbColor = animateColorAsState(if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline, label = "switchThumb").value,
        checkedTrackColor = animateColorAsState(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest, label = "switchTrack").value,
        uncheckedTrackColor = animateColorAsState(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest, label = "switchTrack").value,
        checkedBorderColor = animateColorAsState(if (checked) Color.Transparent else MaterialTheme.colorScheme.outline, label = "switchBorder").value,
        uncheckedBorderColor = animateColorAsState(if (checked) Color.Transparent else MaterialTheme.colorScheme.outline, label = "switchBorder").value,
        checkedIconColor = animateColorAsState(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, label = "switchIcon").value,
        uncheckedIconColor = animateColorAsState(if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest, label = "switchIcon").value
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isSaved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300),
        label = "buttonColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSaved) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(durationMillis = 300),
        label = "contentColor"
    )

    val topShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val middleShape = RoundedCornerShape(4.dp)
    val bottomShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    val singleShape = RoundedCornerShape(24.dp)

    ScreenScaffold(
        title = {
            Text(
                text = stringResource(R.string.calendar_settings_title),
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp
            )
        },
        onBack = onBack,
        isMain = false
    ) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 15.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // General Appearance Section
                Column {
                    Text(
                        text = stringResource(R.string.settings_category_appearance),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // First Day of Week
                        Surface(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                scope.launch {
                                    repository.setFirstDayMonday(!isFirstDayMonday)
                                    val appContext = context.applicationContext
                                    CalendarWidget().updateAll(appContext)
                                }
                            },
                            shape = topShape,
                            color = MaterialTheme.colorScheme.surfaceBright,
                            modifier = Modifier.fillMaxWidth(),
                            interactionSource = isMondayInteractionSource
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 80.dp)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        Icons.Outlined.CalendarViewMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.first_day_title),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (isFirstDayMonday) stringResource(R.string.first_day_monday) else stringResource(R.string.first_day_sunday),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isFirstDayMonday,
                                    onCheckedChange = null,
                                    colors = switchColors(isFirstDayMonday),
                                    modifier = Modifier.padding(start = 12.dp),
                                    interactionSource = isMondayInteractionSource,
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (isFirstDayMonday) Icons.Outlined.Check else Icons.Outlined.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                )
                            }
                        }

                        // Temperature Unit
                        Surface(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                scope.launch {
                                    repository.setUseFahrenheit(!useFahrenheit)
                                    val appContext = context.applicationContext
                                    CalendarWidget().updateAll(appContext)
                                    WeatherWorker.runOnce(appContext)
                                }
                            },
                            shape = bottomShape,
                            color = MaterialTheme.colorScheme.surfaceBright,
                            modifier = Modifier.fillMaxWidth(),
                            interactionSource = isFahrenheitInteractionSource
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 80.dp)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        Icons.Outlined.Thermostat,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.temperature_unit_title),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (useFahrenheit) stringResource(R.string.temperature_unit_fahrenheit) else stringResource(R.string.temperature_unit_celsius),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = useFahrenheit,
                                    onCheckedChange = null,
                                    colors = switchColors(useFahrenheit),
                                    modifier = Modifier.padding(start = 12.dp),
                                    interactionSource = isFahrenheitInteractionSource,
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (useFahrenheit) Icons.Outlined.Check else Icons.Outlined.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Weather Location Section
                Column {
                    Text(
                        text = stringResource(R.string.weather_location_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                    )

                    Box {
                        Card(
                            shape = singleShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = cityText,
                                    onValueChange = { cityText = it },
                                    label = { Text(stringResource(R.string.weather_location_city)) },
                                    placeholder = { Text(stringResource(R.string.weather_location_hint)) },
                                    leadingIcon = { Icon(Icons.Outlined.LocationOn, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    singleLine = true
                                )
                            }
                        }

                        if (suggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .padding(top = 88.dp, start = 12.dp, end = 12.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column {
                                    suggestions.forEach { suggestion ->
                                        ListItem(
                                            headlineContent = { Text(suggestion.name) },
                                            supportingContent = { 
                                                Text("${suggestion.country}${if (suggestion.state != null) ", ${suggestion.state}" else ""}") 
                                            },
                                            modifier = Modifier.clickable {
                                                cityText = "${suggestion.name}, ${suggestion.country}"
                                                suggestions = emptyList()
                                                focusManager.clearFocus()
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                        )
                                        if (suggestion != suggestions.last()) {
                                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            scope.launch {
                                repository.setWeatherCity("")
                                repository.setUseFahrenheit(false)
                                repository.setFirstDayMonday(true)
                                
                                val appContext = context.applicationContext
                                CalendarWidget().updateAll(appContext)
                                WeatherWorker.runOnce(appContext)
                                
                                cityText = ""
                            }
                        },
                        modifier = Modifier
                            .weight(0.4f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.reset))
                    }

                    Button(
                        onClick = {
                            val haptic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                HapticFeedbackConstants.CONFIRM
                            } else {
                                HapticFeedbackConstants.LONG_PRESS
                            }
                            view.performHapticFeedback(haptic)
                            scope.launch {
                                // 1. Зберігаємо налаштування
                                repository.setWeatherCity(cityText)
                                
                                // 2. Запускаємо повне оновлення
                                val appContext = context.applicationContext
                                
                                // Оновлюємо віджет і запускаємо воркер для погоди
                                CalendarWidget().updateAll(appContext)
                                WeatherWorker.runOnce(appContext)
                                
                                focusManager.clearFocus()
                                isSaved = true
                                delay(2000)
                                isSaved = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = contentColor)
                    ) {
                        Icon(if (isSaved) Icons.Outlined.DoneAll else Icons.Outlined.Check, null)
                        Spacer(Modifier.width(12.dp))
                        Text(if (isSaved) stringResource(R.string.saved_success) else stringResource(R.string.save_changes), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
