package com.n0white.n0widgets.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import com.n0white.n0widgets.R
import com.n0white.n0widgets.data.GoalRepository
import com.n0white.n0widgets.data.model.Goal
import com.n0white.n0widgets.ui.widget.SavingsWidget
import com.n0white.n0widgets.ui.widget.processImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditScreen(
    repository: GoalRepository,
    goal: Goal?,
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
    var targetAmount by remember { mutableStateOf("") }
    var savedAmount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("$") }
    var isWavy by remember { mutableStateOf(true) }
    var isBlurEnabled by remember { mutableStateOf(false) }
    var backgroundImagePath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(goal) {
        if (!initialized && goal != null) {
            name = goal.name
            emoji = goal.emoji
            targetAmount = goal.targetAmount.toString()
            savedAmount = goal.savedAmount.toString()
            currency = goal.currency
            isWavy = goal.isWavy
            isBlurEnabled = goal.isBlurEnabled
            backgroundImagePath = goal.backgroundImagePath
            initialized = true
        }
        if (initialized && goal != null) {
            backgroundImagePath = goal.backgroundImagePath
            if (backgroundImagePath.isNullOrEmpty()) isBlurEnabled = false
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val result = processImage(context, it, "widget_bg.jpg")
                result?.let { (path, palette) ->
                    val currentGoal = goal ?: return@launch

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

                    repository.updateGoal(
                        currentGoal.copy(
                            backgroundImagePath = path,
                            customPrimary = ultraLightColor,
                            customOnSurface = ultraLightColor,
                            customSecondaryContainer = palette.getDarkMutedColor(0)
                        )
                    )
                    SavingsWidget().updateAll(context)
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
                .padding(horizontal = 20.dp),
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
                            if (backgroundImagePath.isNullOrEmpty()) {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else {
                                scope.launch {
                                    val currentGoal = goal ?: return@launch
                                    repository.updateGoal(
                                        currentGoal.copy(
                                            backgroundImagePath = null,
                                            customPrimary = null,
                                            customOnSurface = null,
                                            customSecondaryContainer = null
                                        )
                                    )
                                    SavingsWidget().updateAll(context)
                                }
                            }
                        },
                        shape = topShape,
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
                            onClick = { isBlurEnabled = !isBlurEnabled },
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
                                Switch(
                                    checked = isBlurEnabled,
                                    onCheckedChange = { isBlurEnabled = it },
                                    modifier = Modifier
                                        .scale(if (isHighRes) 1.1f else 1.0f)
                                        .padding(start = 12.dp),
                                    thumbContent = if (isBlurEnabled) {
                                        {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                            )
                                        }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = { isWavy = !isWavy },
                        shape = bottomShape,
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
                                    if (isWavy) Icons.Outlined.Waves else Icons.Outlined.LinearScale,
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
                            Switch(
                                checked = isWavy,
                                onCheckedChange = { isWavy = it },
                                modifier = Modifier
                                    .scale(if (isHighRes) 1.1f else 1.0f)
                                    .padding(start = 12.dp),
                                thumbContent = if (isWavy) {
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
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
                            label = { Text(stringResource(R.string.goal_name)) },
                            leadingIcon = { Icon(Icons.Outlined.Savings, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = emoji,
                                onValueChange = { emoji = it },
                                label = { Text(stringResource(R.string.emoji)) },
                                leadingIcon = { Icon(Icons.Outlined.EmojiEmotions, null) },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = currency,
                                onValueChange = { if (it.length <= 3) currency = it },
                                label = { Text(stringResource(R.string.currency)) },
                                leadingIcon = { Icon(Icons.Outlined.AttachMoney, null) },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = targetAmount,
                            onValueChange = { targetAmount = it },
                            label = { Text(stringResource(R.string.target_amount)) },
                            leadingIcon = { Icon(Icons.Outlined.TrackChanges, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = savedAmount,
                            onValueChange = { savedAmount = it },
                            label = { Text(stringResource(R.string.saved_amount)) },
                            leadingIcon = { Icon(Icons.Outlined.AddCard, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true
                        )
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
                        scope.launch {
                            repository.resetGoal()
                            SavingsWidget().updateAll(context)
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
                        val currentGoal = goal ?: return@Button
                        val updatedGoal = currentGoal.copy(
                            name = name,
                            emoji = emoji,
                            targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                            savedAmount = savedAmount.toDoubleOrNull() ?: 0.0,
                            currency = currency,
                            isWavy = isWavy,
                            isBlurEnabled = isBlurEnabled
                        )
                        scope.launch {
                            repository.updateGoal(updatedGoal)
                            SavingsWidget().updateAll(context)
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
