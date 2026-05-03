package com.n0white.n0widgets.ui

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import com.n0white.n0widgets.data.CounterRepository
import com.n0white.n0widgets.data.model.Counter
import com.n0white.n0widgets.data.model.CounterFormat
import com.n0white.n0widgets.ui.widget.CounterWidget
import com.n0white.n0widgets.ui.widget.processImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterEditScreen(
    repository: CounterRepository,
    counter: Counter?,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }

    val sw = context.resources.configuration.smallestScreenWidthDp
    val isHighRes = sw >= 410

    var initialized by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var targetDate by remember { mutableStateOf(LocalDate.now().plusMonths(1)) }
    var formatMode by remember { mutableStateOf(CounterFormat.DAYS_ONLY) }
    var isWavy by remember { mutableStateOf(true) }
    var isBlurEnabled by remember { mutableStateOf(false) }
    var backgroundImagePath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(counter) {
        if (!initialized && counter != null) {
            name = counter.name
            emoji = counter.emoji
            startDate = counter.startDate
            targetDate = counter.targetDate
            formatMode = counter.formatMode
            isWavy = counter.isWavy
            isBlurEnabled = counter.isBlurEnabled
            backgroundImagePath = counter.backgroundImagePath
            initialized = true
        }
        if (initialized && counter != null) {
            backgroundImagePath = counter.backgroundImagePath
            if (backgroundImagePath.isNullOrEmpty()) isBlurEnabled = false
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val result = processImage(context, it, "counter_bg.jpg")
                result?.let { (path, palette) ->
                    val currentCounter = counter ?: return@launch

                    fun forceLighten(color: Int): Int {
                        val hsl = FloatArray(3)
                        androidx.core.graphics.ColorUtils.colorToHSL(color, hsl)
                        hsl[1] = hsl[1].coerceAtMost(0.6f)
                        hsl[2] = 0.85f
                        return androidx.core.graphics.ColorUtils.HSLToColor(hsl)
                    }

                    val baseColor = palette.getLightVibrantColor(
                        palette.getVibrantColor(
                            palette.getLightMutedColor(Color.White.toArgb())
                        )
                    )
                    val ultraLightColor = forceLighten(baseColor)

                    repository.updateCounter(
                        currentCounter.copy(
                            backgroundImagePath = path,
                            customPrimary = ultraLightColor,
                            customOnSurface = ultraLightColor,
                            customSecondaryContainer = palette.getDarkMutedColor(0)
                        )
                    )
                    CounterWidget().updateAll(context)
                }
            }
        }
    }

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

    if (!initialized) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = if (isHighRes) 8.dp else 4.dp),
        verticalArrangement = Arrangement.spacedBy(if (isHighRes) 16.dp else 10.dp)
    ) {
        // Background Image Picker
        Surface(
            onClick = {
                if (backgroundImagePath.isNullOrEmpty()) {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    scope.launch {
                        val currentCounter = counter ?: return@launch
                        repository.updateCounter(
                            currentCounter.copy(
                                backgroundImagePath = null,
                                customPrimary = null,
                                customOnSurface = null,
                                customSecondaryContainer = null
                            )
                        )
                        CounterWidget().updateAll(context)
                    }
                }
            },
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isHighRes) 12.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(if (isHighRes) 36.dp else 30.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (backgroundImagePath.isNullOrEmpty()) Icons.Default.AddPhotoAlternate else Icons.Default.HideImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(if (isHighRes) 20.dp else 18.dp)
                        )
                    }
                    Spacer(Modifier.width(if (isHighRes) 12.dp else 8.dp))
                    Column {
                        Text(
                            if (backgroundImagePath.isNullOrEmpty()) "Set Background Image" else "Remove Background",
                            style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Blur Toggle
        if (!backgroundImagePath.isNullOrEmpty()) {
            Surface(
                onClick = { isBlurEnabled = !isBlurEnabled },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isHighRes) 12.dp else 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(if (isHighRes) 36.dp else 30.dp)
                                .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.BlurOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(if (isHighRes) 20.dp else 18.dp)
                            )
                        }
                        Spacer(Modifier.width(if (isHighRes) 12.dp else 8.dp))
                        Column {
                            Text(
                                "Blur Background",
                                style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Switch(
                        checked = isBlurEnabled,
                        onCheckedChange = { isBlurEnabled = it },
                        modifier = Modifier.scale(if (isHighRes) 0.9f else 0.8f)
                    )
                }
            }
        }

        // Expressive Switch
        Surface(
            onClick = { isWavy = !isWavy },
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isHighRes) 12.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(if (isHighRes) 36.dp else 30.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isWavy) Icons.Default.Waves else Icons.Default.LinearScale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(if (isHighRes) 20.dp else 18.dp)
                        )
                    }
                    Spacer(Modifier.width(if (isHighRes) 12.dp else 8.dp))
                    Column {
                        Text(
                            "Expressive Style",
                            style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Switch(
                    checked = isWavy,
                    onCheckedChange = { isWavy = it },
                    modifier = Modifier.scale(if (isHighRes) 0.9f else 0.8f)
                )
            }
        }

        // Formatting Toggle
        Surface(
            onClick = { formatMode = if (formatMode == CounterFormat.DAYS_ONLY) CounterFormat.YMD else CounterFormat.DAYS_ONLY },
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isHighRes) 12.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(if (isHighRes) 36.dp else 30.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(if (isHighRes) 20.dp else 18.dp)
                        )
                    }
                    Spacer(Modifier.width(if (isHighRes) 12.dp else 8.dp))
                    Column {
                        Text(
                            if (formatMode == CounterFormat.DAYS_ONLY) "Format: Days Only" else "Format: Years, Months, Days",
                            style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Switch(
                    checked = formatMode == CounterFormat.YMD,
                    onCheckedChange = { formatMode = if (it) CounterFormat.YMD else CounterFormat.DAYS_ONLY },
                    modifier = Modifier.scale(if (isHighRes) 0.9f else 0.8f)
                )
            }
        }

        // Fields
        Column(verticalArrangement = Arrangement.spacedBy(if (isHighRes) 12.dp else 8.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Counter Name") },
                leadingIcon = { Icon(Icons.Default.Label, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji") },
                    leadingIcon = { Icon(Icons.Default.EmojiEmotions, null) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true
                )
            }

            // Date Pickers
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

                OutlinedTextField(
                    value = startDate.format(dateFormatter),
                    onValueChange = {},
                    label = { Text("Start Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            showDatePicker(context, startDate) { startDate = it }
                        }) {
                            Icon(Icons.Default.CalendarToday, null)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                )

                OutlinedTextField(
                    value = targetDate.format(dateFormatter),
                    onValueChange = {},
                    label = { Text("Target Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            showDatePicker(context, targetDate) { targetDate = it }
                        }) {
                            Icon(Icons.Default.Event, null)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        repository.resetCounter()
                        CounterWidget().updateAll(context)
                    }
                },
                modifier = Modifier
                    .weight(0.4f)
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
            }

            Button(
                onClick = {
                    val currentCounter = counter ?: return@Button
                    val updatedCounter = currentCounter.copy(
                        name = name,
                        emoji = emoji,
                        startDate = startDate,
                        targetDate = targetDate,
                        formatMode = formatMode,
                        isWavy = isWavy,
                        isBlurEnabled = isBlurEnabled
                    )
                    scope.launch {
                        repository.updateCounter(updatedCounter)
                        CounterWidget().updateAll(context)
                        com.n0white.n0widgets.ui.widget.MidnightUpdater.schedule(context)
                        isSaved = true
                        delay(2000)
                        isSaved = false
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = contentColor)
            ) {
                Icon(if (isSaved) Icons.Default.DoneAll else Icons.Default.Check, null)
                Spacer(Modifier.width(12.dp))
                Text(if (isSaved) "Saved!" else "Save Changes", fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun showDatePicker(
    context: android.content.Context,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    ).show()
}