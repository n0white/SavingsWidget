package com.n0white.savingswidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.updateAll
import androidx.palette.graphics.Palette
import com.n0white.savingswidget.data.GoalRepository
import com.n0white.savingswidget.data.model.Goal
import com.n0white.savingswidget.ui.components.WavyProgressIndicator
import com.n0white.savingswidget.ui.theme.SavingsWidgetTheme
import com.n0white.savingswidget.ui.widget.SavingsWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var repository: GoalRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        repository = GoalRepository(this)
        enableEdgeToEdge()
        setContent {
            SavingsWidgetTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    "Goal Settings",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { innerPadding ->
                    GoalEditScreen(
                        repository = repository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GoalEditScreen(repository: GoalRepository, modifier: Modifier = Modifier) {
    val goal by repository.goalFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }
    
    val sw = context.resources.configuration.smallestScreenWidthDp
    val isHighRes = sw >= 410

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val result = processImage(context, it)
                result?.let { (path, palette) ->
                    val currentGoal = goal ?: return@launch
                    
                    // Helper to make any color "Ultra Light" (High luminance)
                    fun forceLighten(color: Int): Int {
                        val hsl = FloatArray(3)
                        androidx.core.graphics.ColorUtils.colorToHSL(color, hsl)
                        hsl[1] = hsl[1].coerceAtMost(0.6f) // More de-saturated for extreme pastel
                        hsl[2] = 0.85f // Even higher brightness (94%)
                        return androidx.core.graphics.ColorUtils.HSLToColor(hsl)
                    }

                    val baseColor = palette.getLightVibrantColor(
                        palette.getVibrantColor(
                            palette.getLightMutedColor(Color.White.toArgb())
                        )
                    )

                    val ultraLightColor = forceLighten(baseColor)

                    val updatedGoal = currentGoal.copy(
                        backgroundImagePath = path,
                        customPrimary = ultraLightColor,
                        customOnSurface = ultraLightColor,
                        customSecondaryContainer = palette.getDarkMutedColor(0)
                    )
                    repository.updateGoal(updatedGoal)
                    SavingsWidget().updateAll(context)
                }
            }
        }
    }

    if (goal == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        var name by remember(goal) { mutableStateOf(goal?.name ?: "") }
        var emoji by remember(goal) { mutableStateOf(goal?.emoji ?: "") }
        var targetAmount by remember(goal) { mutableStateOf(goal?.targetAmount?.toString() ?: "") }
        var savedAmount by remember(goal) { mutableStateOf(goal?.savedAmount?.toString() ?: "") }
        var currency by remember(goal) { mutableStateOf(goal?.currency ?: "$") }
        var isWavy by remember(goal) { mutableStateOf(goal?.isWavy ?: true) }
        var isBlurEnabled by remember(goal) { mutableStateOf(goal?.isBlurEnabled ?: false) }

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

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = if (isHighRes) 8.dp else 4.dp),
            verticalArrangement = Arrangement.spacedBy(if (isHighRes) 16.dp else 10.dp)
        ) {
            // Background Image Picker
            Surface(
                onClick = {
                    if (goal?.backgroundImagePath.isNullOrEmpty()) {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } else {
                        scope.launch {
                            repository.updateGoal(goal!!.copy(
                                backgroundImagePath = null,
                                customPrimary = null,
                                customOnSurface = null,
                                customSecondaryContainer = null
                            ))
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
                                if (goal?.backgroundImagePath.isNullOrEmpty()) Icons.Default.AddPhotoAlternate else Icons.Default.HideImage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(if (isHighRes) 20.dp else 18.dp)
                            )
                        }
                        Spacer(Modifier.width(if (isHighRes) 12.dp else 8.dp))
                        Column {
                            Text(
                                if (goal?.backgroundImagePath.isNullOrEmpty()) "Set Background Image" else "Remove Background",
                                style = if (isHighRes) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Blur Toggle (Visible only when image is present)
            if (!goal?.backgroundImagePath.isNullOrEmpty()) {
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
                            thumbContent = if (isBlurEnabled) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            } else null,
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
                        thumbContent = if (isWavy) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else null,
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
                    singleLine = true,
                    textStyle = if (isHighRes) LocalTextStyle.current else MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { input ->
                            if (input.all { char ->
                                    val type = Character.getType(char)
                                    type == Character.SURROGATE.toInt() ||
                                    type == Character.OTHER_SYMBOL.toInt() ||
                                    type == Character.NON_SPACING_MARK.toInt() ||
                                    char.isSurrogate()
                                }) {
                                emoji = input
                            }
                        },
                        label = { Text("Emoji") },
                        leadingIcon = { Icon(Icons.Default.EmojiEmotions, null) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        textStyle = if (isHighRes) LocalTextStyle.current else MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { if (it.length <= 3) currency = it },
                        label = { Text("Currency") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        textStyle = if (isHighRes) LocalTextStyle.current else MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { input ->
                        if (!input.contains("-")) {
                            val parts = input.split(".")
                            if (parts.size <= 2 && parts[0].length <= 9 && (parts.size < 2 || parts[1].length <= 2)) {
                                targetAmount = input
                            }
                        }
                    },
                    label = { Text("Target Amount") },
                    leadingIcon = { Icon(Icons.Default.TrackChanges, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    textStyle = if (isHighRes) LocalTextStyle.current else MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = savedAmount,
                    onValueChange = { input ->
                        if (!input.contains("-")) {
                            val parts = input.split(".")
                            if (parts.size <= 2 && parts[0].length <= 9 && (parts.size < 2 || parts[1].length <= 2)) {
                                savedAmount = input
                            }
                        }
                    },
                    label = { Text("Saved Amount") },
                    leadingIcon = { Icon(Icons.Default.AddCard, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    textStyle = if (isHighRes) LocalTextStyle.current else MaterialTheme.typography.bodyMedium
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
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Goal")
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
                            SavingsWidget().updateAll(repository.context)
                            isSaved = true
                            delay(2000)
                            isSaved = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = contentColor
                    )
                ) {
                    Icon(
                        if (isSaved) Icons.Default.DoneAll else Icons.Default.Check,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isSaved) "Saved!" else "Save Changes",
                        style = if (isHighRes) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(if (isHighRes) 8.dp else 4.dp))
        }
    }
}

private fun processImage(context: Context, uri: Uri): Pair<String, Palette>? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        if (originalBitmap == null) return null
        
        // Resize bitmap to avoid "Can't show content" error (RemoteViews memory limit)
        val maxDimension = 600
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = maxDimension.toFloat() / Math.max(width, height).coerceAtLeast(1)
        
        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                originalBitmap,
                (width * scale).toInt(),
                (height * scale).toInt(),
                true
            )
        } else {
            originalBitmap
        }
        
        // Extract colors
        val palette = Palette.from(bitmap).generate()
        
        // Save to internal storage
        val file = File(context.filesDir, "widget_bg.jpg")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out) // Lower quality to save space
        out.flush()
        out.close()
        
        if (bitmap != originalBitmap) bitmap.recycle()
        originalBitmap.recycle()
        
        Pair(file.absolutePath, palette)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
