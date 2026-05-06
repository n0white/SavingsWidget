package com.n0white.n0widgets.ui

import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import com.n0white.n0widgets.R
import com.n0white.n0widgets.data.CounterRepository
import com.n0white.n0widgets.data.model.Counter
import com.n0white.n0widgets.data.model.CounterFormat
import com.n0white.n0widgets.ui.widget.CounterWidget
import com.n0white.n0widgets.ui.widget.processImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterEditScreen(
    repository: CounterRepository,
    counter: Counter?,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    var isSaved by remember { mutableStateOf(false) }

    val sw = LocalConfiguration.current.smallestScreenWidthDp
    val isHighRes = sw >= 410

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

    var initialized by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var targetDate by remember { mutableStateOf(LocalDate.now().plusMonths(1)) }
    var formatMode by remember { mutableStateOf(CounterFormat.DAYS_ONLY) }
    var isInfinite by remember { mutableStateOf(false) }
    var isWavy by remember { mutableStateOf(true) }
    var isBlurEnabled by remember { mutableStateOf(false) }
    var backgroundImagePath by remember { mutableStateOf<String?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showTargetDatePicker by remember { mutableStateOf(false) }
    val isBlurEnabledInteractionSource = remember { MutableInteractionSource() }
    val isWavyInteractionSource = remember { MutableInteractionSource() }
    val formatModeInteractionSource = remember { MutableInteractionSource() }
    val isInfiniteInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(counter) {
        if (!initialized && counter != null) {
            name = counter.name
            emoji = counter.emoji
            startDate = counter.startDate
            targetDate = counter.targetDate
            formatMode = counter.formatMode
            isInfinite = counter.isInfinite
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

                    fun forceDarken(color: Int): Int {
                        val hsl = FloatArray(3)
                        androidx.core.graphics.ColorUtils.colorToHSL(color, hsl)
                        hsl[2] = 0.25f
                        return androidx.core.graphics.ColorUtils.HSLToColor(hsl)
                    }

                    val baseColor = palette.getLightVibrantColor(
                        palette.getVibrantColor(
                            palette.getLightMutedColor(Color.White.toArgb())
                        )
                    )
                    val ultraLightColor = forceLighten(baseColor)
                    val ultraDarkColor = forceDarken(baseColor)

                    repository.updateCounter(
                        currentCounter.copy(
                            backgroundImagePath = path,
                            customPrimary = ultraLightColor,
                            customPrimaryInverse = ultraDarkColor,
                            customOnSurface = ultraLightColor,
                            customOnSurfaceInverse = ultraDarkColor,
                            customSecondaryContainer = palette.getDarkMutedColor(0)
                        )
                    )
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

    val topShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val middleShape = RoundedCornerShape(4.dp)
    val bottomShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    val singleShape = RoundedCornerShape(24.dp)

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.spacedBy(if (isHighRes) 24.dp else 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
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
                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            isInfinite = !isInfinite
                        },
                        shape = topShape,
                        color = MaterialTheme.colorScheme.surfaceBright,
                        modifier = Modifier.fillMaxWidth(),
                        interactionSource = isInfiniteInteractionSource
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
                                    Icons.Outlined.AllInclusive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.infinite_counter),
                                        style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = stringResource(R.string.infinite_counter_description),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                                Switch(
                                    checked = isInfinite,
                                    onCheckedChange = null,
                                    colors = switchColors(isInfinite),
                                    modifier = Modifier
                                        .padding(start = 12.dp),
                                    interactionSource = isInfiniteInteractionSource,
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (isInfinite) Icons.Outlined.Check else Icons.Outlined.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                )
                            }
                        }
                    }

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
                                            customPrimaryInverse = null,
                                            customOnSurface = null,
                                            customOnSurfaceInverse = null,
                                            customSecondaryContainer = null
                                        )
                                    )
                                }
                            }
                        },
                        shape = middleShape,
                        color = MaterialTheme.colorScheme.surfaceBright,
                        modifier = Modifier.fillMaxWidth()
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
                                    if (backgroundImagePath.isNullOrEmpty()) Icons.Outlined.AddPhotoAlternate else Icons.Outlined.HideImage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = if (backgroundImagePath.isNullOrEmpty()) stringResource(R.string.set_background_image) else stringResource(R.string.remove_background),
                                        style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (backgroundImagePath.isNullOrEmpty()) stringResource(R.string.choose_photo_gallery) else stringResource(R.string.clear_current_background),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (!backgroundImagePath.isNullOrEmpty()) {
                        Surface(
                            onClick = { 
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                isBlurEnabled = !isBlurEnabled 
                            },
                            shape = middleShape,
                            color = MaterialTheme.colorScheme.surfaceBright,
                            modifier = Modifier.fillMaxWidth(),
                            interactionSource = isBlurEnabledInteractionSource
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
                                        Icons.Outlined.BlurOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.blur_background),
                                            style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = stringResource(R.string.apply_blur_effect),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                                    Switch(
                                        checked = isBlurEnabled,
                                        onCheckedChange = null,
                                        colors = switchColors(isBlurEnabled),
                                        modifier = Modifier
                                            .padding(start = 12.dp),
                                        interactionSource = isBlurEnabledInteractionSource,
                                        thumbContent = {
                                        Icon(
                                            imageVector = if (isBlurEnabled) Icons.Outlined.Check else Icons.Outlined.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = { 
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            isWavy = !isWavy 
                        },
                        shape = middleShape,
                        color = MaterialTheme.colorScheme.surfaceBright,
                        modifier = Modifier.fillMaxWidth(),
                        interactionSource = isWavyInteractionSource
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
                                    Icons.Outlined.Waves,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.expressive_style),
                                        style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = stringResource(R.string.use_wavy_shapes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                                Switch(
                                    checked = isWavy,
                                    onCheckedChange = null,
                                    colors = switchColors(isWavy),
                                    modifier = Modifier
                                        .padding(start = 12.dp),
                                    interactionSource = isWavyInteractionSource,
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (isWavy) Icons.Outlined.Check else Icons.Outlined.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = { 
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            formatMode = if (formatMode == CounterFormat.DAYS_ONLY) CounterFormat.YMD else CounterFormat.DAYS_ONLY 
                        },
                        shape = bottomShape,
                        color = MaterialTheme.colorScheme.surfaceBright,
                        modifier = Modifier.fillMaxWidth(),
                        interactionSource = formatModeInteractionSource
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
                                    Icons.Outlined.FormatListNumbered,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = if (formatMode == CounterFormat.DAYS_ONLY) stringResource(R.string.format_days_only) else stringResource(R.string.format_ymd),
                                        style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = stringResource(R.string.change_time_display),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                                Switch(
                                    checked = formatMode == CounterFormat.YMD,
                                    onCheckedChange = null,
                                    colors = switchColors(formatMode == CounterFormat.YMD),
                                    modifier = Modifier
                                        .padding(start = 12.dp),
                                    interactionSource = formatModeInteractionSource,
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (formatMode == CounterFormat.YMD) Icons.Outlined.Check else Icons.Outlined.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Column {
                Text(
                    text = stringResource(R.string.settings_category_content),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp, bottom = 12.dp, top = 0.dp)
                )

                Card(
                    shape = singleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isHighRes) 12.dp else 8.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.counter_name)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Label, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = emoji,
                                onValueChange = { input ->
                                    // Filter only emojis
                                    val filtered = input.filter { char ->
                                        Character.getType(char).toByte() == Character.SURROGATE ||
                                        Character.getType(char).toByte() == Character.OTHER_SYMBOL ||
                                        char.code in 0x2000..0x32FF ||
                                        char.code in 0x1F000..0x1F9FF
                                    }
                                    if (filtered.length <= 2) emoji = filtered
                                },
                                label = { Text(stringResource(R.string.emoji)) },
                                leadingIcon = { Icon(Icons.Outlined.EmojiEmotions, null) },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                singleLine = true
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val formatDate = remember {
                                { date: LocalDate ->
                                    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                        .replace(".", "")
                                        .take(3)
                                    val day = date.dayOfMonth.toString().padStart(2, '0')
                                    "$day $month ${date.year}"
                                }
                            }

                            OutlinedTextField(
                                value = formatDate(startDate),
                                onValueChange = {},
                                label = { Text(stringResource(R.string.start_date)) },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showStartDatePicker = true }) {
                                        Icon(Icons.Outlined.CalendarToday, null)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium
                            )

                            OutlinedTextField(
                                value = formatDate(targetDate),
                                onValueChange = {},
                                label = { Text(stringResource(R.string.target_date)) },
                                readOnly = true,
                                enabled = !isInfinite,
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showTargetDatePicker = true },
                                        enabled = !isInfinite
                                    ) {
                                        Icon(Icons.Outlined.Event, null)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium
                            )
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
                        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                        scope.launch {
                            name = ""
                            emoji = "📅"
                            startDate = LocalDate.now()
                            targetDate = LocalDate.now().plusMonths(1)
                            isWavy = false
                            isBlurEnabled = false
                            isInfinite = false
                            backgroundImagePath = null
                            formatMode = CounterFormat.DAYS_ONLY

                            val updatedCounter = (counter ?: Counter(
                                name = "",
                                emoji = "📅",
                                startDate = LocalDate.now(),
                                targetDate = LocalDate.now().plusMonths(1)
                            )).copy(
                                name = "",
                                emoji = "📅",
                                startDate = LocalDate.now(),
                                targetDate = LocalDate.now().plusMonths(1),
                                isWavy = false,
                                isBlurEnabled = false,
                                isInfinite = false,
                                backgroundImagePath = null,
                                formatMode = CounterFormat.DAYS_ONLY,
                                customPrimary = null,
                                customPrimaryInverse = null,
                                customOnSurface = null,
                                customOnSurfaceInverse = null,
                                customSecondaryContainer = null
                            )
                            repository.updateCounter(updatedCounter)
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
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        val currentCounter = counter ?: return@Button
                        val updatedCounter = currentCounter.copy(
                            name = name,
                            emoji = emoji,
                            startDate = startDate,
                            targetDate = targetDate,
                            formatMode = formatMode,
                            isInfinite = isInfinite,
                            isWavy = isWavy,
                            isBlurEnabled = isBlurEnabled
                        )
                        scope.launch {
                            repository.updateCounter(updatedCounter)
                            com.n0white.n0widgets.ui.widget.MidnightUpdater.schedule(context)
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

    if (showStartDatePicker) {
        MyDatePickerDialog(
            initialDate = startDate,
            onDateSelected = { startDate = it },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showTargetDatePicker) {
        MyDatePickerDialog(
            initialDate = targetDate,
            onDateSelected = { targetDate = it },
            onDismiss = { showTargetDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
