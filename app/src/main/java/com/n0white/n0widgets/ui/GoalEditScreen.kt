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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
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
    // Путь к фону храним отдельно, чтобы обновлять его после выбора изображения
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

        // Fields
        Column(verticalArrangement = Arrangement.spacedBy(if (isHighRes) 12.dp else 8.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Goal Name") },
                leadingIcon = { Icon(Icons.Default.Savings, null) },
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

                OutlinedTextField(
                    value = currency,
                    onValueChange = { if (it.length <= 3) currency = it },
                    label = { Text("Currency") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = { Text("Target Amount") },
                leadingIcon = { Icon(Icons.Default.TrackChanges, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                singleLine = true
            )

            OutlinedTextField(
                value = savedAmount,
                onValueChange = { savedAmount = it },
                label = { Text("Saved Amount") },
                leadingIcon = { Icon(Icons.Default.AddCard, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
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