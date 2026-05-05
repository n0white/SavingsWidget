package com.n0white.n0widgets.ui.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.color.ColorProvider
import android.content.Intent
import com.n0white.n0widgets.SavingsActivity
import com.n0white.n0widgets.AddAmountActivity
import com.n0white.n0widgets.R
import com.n0white.n0widgets.data.model.Goal
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SavingsWidgetContent(goal: Goal, isThemeBackgroundEnabled: Boolean) {
    val size = LocalSize.current
    val context = LocalContext.current
    val isSmall = size.width < 200.dp
    val sw = context.resources.configuration.smallestScreenWidthDp
    val isHighRes = sw >= 400

    GlanceTheme {
        val colors = GlanceTheme.colors
        val isBgImagePresent = !goal.backgroundImagePath.isNullOrEmpty()
        val forceDark = isBgImagePresent && !isThemeBackgroundEnabled

        // These providers will automatically switch based on system theme
        val overlayColorProvider = if (forceDark) {
            androidx.glance.unit.ColorProvider(Color(context.getColor(R.color.widget_overlay_night)))
        } else {
            androidx.glance.unit.ColorProvider(R.color.widget_overlay)
        }

        val onImagePrimary = if (forceDark) {
            androidx.glance.unit.ColorProvider(Color(context.getColor(R.color.widget_on_image_primary_night)))
        } else {
            androidx.glance.unit.ColorProvider(R.color.widget_on_image_primary)
        }
        
        val onImageSecondary = if (forceDark) {
            androidx.glance.unit.ColorProvider(Color(context.getColor(R.color.widget_on_image_secondary_night)))
        } else {
            androidx.glance.unit.ColorProvider(R.color.widget_on_image_secondary)
        }
        
        val onImageTertiaryContainer = if (forceDark) {
            androidx.glance.unit.ColorProvider(Color(context.getColor(R.color.widget_on_image_tertiary_container_night)))
        } else {
            androidx.glance.unit.ColorProvider(R.color.widget_on_image_tertiary_container)
        }

        val primaryColor = if (goal.customPrimary != null && goal.customPrimaryInverse != null) {
            if (forceDark) {
                androidx.glance.unit.ColorProvider(Color(goal.customPrimary))
            } else {
                androidx.glance.color.ColorProvider(day = Color(goal.customPrimaryInverse), night = Color(goal.customPrimary))
            }
        } else {
            goal.customPrimary?.let { androidx.glance.unit.ColorProvider(Color(it)) }
                ?: if (isBgImagePresent) onImagePrimary else colors.primary
        }

        val secondaryContainerColor = goal.customSecondaryContainer?.let { androidx.glance.unit.ColorProvider(Color(it)) }
            ?: if (isBgImagePresent) onImageSecondary else colors.secondaryContainer

        val onSurfaceColor = if (goal.customOnSurface != null && goal.customOnSurfaceInverse != null) {
            if (forceDark) {
                androidx.glance.unit.ColorProvider(Color(goal.customOnSurface))
            } else {
                androidx.glance.color.ColorProvider(day = Color(goal.customOnSurfaceInverse), night = Color(goal.customOnSurface))
            }
        } else {
            goal.customOnSurface?.let { androidx.glance.unit.ColorProvider(Color(it)) } ?: colors.onSurface
        }
        
        val onSurfaceVariantColor = onSurfaceColor
        
        val tertiaryContainerColor = if (goal.customPrimary != null && goal.customPrimaryInverse != null) {
            if (forceDark) {
                androidx.glance.unit.ColorProvider(Color(goal.customPrimary).copy(alpha = 0.2f))
            } else {
                androidx.glance.color.ColorProvider(
                    day = Color(goal.customPrimaryInverse).copy(alpha = 0.2f),
                    night = Color(goal.customPrimary).copy(alpha = 0.2f)
                )
            }
        } else {
            goal.customPrimary?.let { androidx.glance.unit.ColorProvider(Color(it).copy(alpha = 0.2f)) }
                ?: if (isBgImagePresent) onImageTertiaryContainer else colors.tertiaryContainer
        }

        val onTertiaryContainerColor = onSurfaceColor

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(if (forceDark) androidx.glance.unit.ColorProvider(Color(context.getColor(R.color.widget_background_night))) else colors.widgetBackground)
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(android.content.Intent(context, SavingsActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }))
        ) {
            if (!goal.backgroundImagePath.isNullOrEmpty()) {
                val file = File(goal.backgroundImagePath)
                if (file.exists()) {
                    val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (originalBitmap != null) {
                        val bitmap = if (goal.isBlurEnabled) {
                            blurBitmap(originalBitmap, context, 15f)
                        } else {
                            originalBitmap
                        }
                        
                        Image(
                            provider = ImageProvider(bitmap),
                            contentDescription = null,
                            contentScale = androidx.glance.layout.ContentScale.Crop,
                            modifier = GlanceModifier.fillMaxSize().cornerRadius(24.dp)
                        )
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .background(overlayColorProvider)
                                .cornerRadius(24.dp)
                        ) {}
                    }
                }
            }

            Column(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = context.getString(R.string.savings_label),
                            style = TextStyle(
                                fontSize = if (isHighRes) 11.sp else 10.sp, 
                                color = onSurfaceVariantColor
                            )
                        )
                        val density = context.resources.displayMetrics.density
                        val baseFontSize = if (isHighRes) 21.sp else 19.sp
                        val reducedFontSize = if (isHighRes) 17.sp else 16.sp
                        val availableWidthDp = (size.width.value.toInt() - 32 - 48 - 12).dp
                        val displayName = goal.name.ifBlank { context.getString(R.string.default_goal_name) }
                        val finalFontSize = if (!isSmall) {
                            getDynamicFontSize(displayName, availableWidthDp, baseFontSize, reducedFontSize, density)
                        } else {
                            if (isHighRes) 20.sp else 18.sp
                        }

                        Text(
                            text = displayName,
                            style = TextStyle(
                                fontSize = finalFontSize,
                                fontWeight = FontWeight.Medium,
                                color = onSurfaceColor
                            ),
                            maxLines = 2
                        )
                    }
                    
                    Spacer(modifier = GlanceModifier.width(12.dp))

                    if (goal.isPlusButtonEnabled) {
                        Box(
                            modifier = GlanceModifier
                                .size(if (isHighRes) 48.dp else 44.dp)
                                .background(tertiaryContainerColor)
                                .cornerRadius(if (isHighRes) 14.dp else 12.dp)
                                .clickable(actionStartActivity(Intent(context, AddAmountActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_add),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(onTertiaryContainerColor),
                                modifier = GlanceModifier.size(if (isHighRes) 24.dp else 22.dp)
                            )
                        }
                    } else if (goal.emoji.isNotEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .size(if (isHighRes) 48.dp else 44.dp)
                                .background(tertiaryContainerColor)
                                .cornerRadius(if (isHighRes) 14.dp else 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = goal.emoji, 
                                style = TextStyle(fontSize = if (isHighRes) 22.sp else 19.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                Column(modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp)) {
                    if (isSmall) {
                        Text(
                            text = "${goal.currency}${goal.savedAmount.formatAmount()}",
                            style = TextStyle(
                                fontSize = if (isHighRes) 25.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = context.getString(R.string.progress_of, goal.currency, goal.targetAmount.formatAmount()),
                                style = TextStyle(fontSize = if (isHighRes) 12.sp else 10.sp, color = onSurfaceVariantColor)
                            )
                            
                            if (goal.savedAmount > 0) {
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                MonthlyEfficiencyChip(
                                    efficiency = goal.monthlyEfficiency, 
                                    compact = true, 
                                    isHighRes = isHighRes,
                                    containerColor = tertiaryContainerColor,
                                    contentColor = onTertiaryContainerColor
                                )
                            }
                        }
                    } else {
                        if (goal.savedAmount > 0) {
                            MonthlyEfficiencyChip(
                                efficiency = goal.monthlyEfficiency, 
                                isHighRes = isHighRes,
                                containerColor = tertiaryContainerColor,
                                contentColor = onTertiaryContainerColor
                            )
                            Spacer(modifier = GlanceModifier.height(1.dp))
                        }
                        
                        val density = context.resources.displayMetrics.density
                        val savedAmountText = "${goal.currency}${goal.savedAmount.formatAmount()}"
                        val baseAmountSize = if (isHighRes) 36.sp else 31.sp
                        val reducedAmountSize = if (isHighRes) 26.sp else 23.sp
                        val amountAvailableWidth = (size.width.value.toInt() - 32 - 70).dp
                        val finalAmountFontSize = getDynamicFontSize(savedAmountText, amountAvailableWidth, baseAmountSize, reducedAmountSize, density)

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically 
                        ) {
                            Text(
                                text = savedAmountText,
                                style = TextStyle(
                                    fontSize = finalAmountFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = context.getString(R.string.target_label),
                                    style = TextStyle(fontSize = if (isHighRes) 10.sp else 9.sp, color = onSurfaceVariantColor)
                                )
                                Text(
                                    text = "${goal.currency}${goal.targetAmount.formatAmount()}",
                                    style = TextStyle(fontSize = if (isHighRes) 14.sp else 12.sp, color = onSurfaceVariantColor)
                                )
                            }
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(if (isHighRes) 4.dp else 2.dp))

                    WavyProgressIndicator(
                        progress = goal.progress,
                        colorProvider = primaryColor,
                        trackColorProvider = secondaryContainerColor,
                        isWavy = goal.isWavy,
                        modifier = GlanceModifier.fillMaxWidth().height(if (isHighRes) 16.dp else 14.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(if (isHighRes) 8.dp else 6.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(goal.progress * 100).roundToInt()}%",
                        style = TextStyle(
                            fontSize = if (isHighRes) 14.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    
                    val footerLabel = if (isSmall) context.getString(R.string.remaining_label_short) else context.getString(R.string.remaining_label)
                    Text(
                        text = "$footerLabel${goal.currency}${goal.remaining.formatAmount()}",
                        style = TextStyle(
                            fontSize = if (isHighRes) 12.sp else 10.sp,
                            color = onSurfaceVariantColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyEfficiencyChip(
    efficiency: Int, 
    compact: Boolean = false, 
    isHighRes: Boolean = false,
    containerColor: ColorProvider? = null,
    contentColor: ColorProvider? = null
) {
    val colors = GlanceTheme.colors
    val finalContainer = containerColor ?: colors.tertiaryContainer
    val finalContent = contentColor ?: colors.onTertiaryContainer
    
    Box(
        modifier = GlanceModifier
            .background(finalContainer)
            .cornerRadius(10.dp)
            .padding(
                horizontal = if (compact) 4.dp else (if (isHighRes) 10.dp else 8.dp), 
                vertical = if (compact) 1.dp else (if (isHighRes) 3.dp else 2.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        val sign = if (efficiency > 0) "+" else ""
        val efficiencyText = if (compact) "$sign$efficiency%" else context.getString(R.string.efficiency_suffix, "$sign$efficiency%")
        Text(
            text = efficiencyText,
            style = TextStyle(
                fontSize = if (compact) 8.sp else (if (isHighRes) 10.sp else 9.sp),
                fontWeight = FontWeight.Bold,
                color = finalContent
            )
        )
    }
}

fun Double.formatAmount(): String =
    NumberFormat.getNumberInstance(Locale.US).format(this)

@Composable
fun WavyProgressIndicator(
    progress: Float,
    colorProvider: ColorProvider,
    trackColorProvider: ColorProvider,
    isWavy: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    val size = LocalSize.current
    
    val actualWidthDp = if (size.width.value > 0) (size.width.value.toInt() - 32).coerceAtLeast(50) else 160
    val heightDp = 18
    
    val isLargeWidget = size.width >= 150.dp && size.height >= 150.dp
    val dotThreshold = if (isLargeWidget) 0.97f else 0.98f
    
    Box(modifier = modifier) {
        val trackBitmap = createProgressMaskBitmap(actualWidthDp, heightDp, progress, density, isWavy, dotThreshold, isTrack = true)
        Image(
            provider = ImageProvider(trackBitmap),
            contentDescription = null,
            colorFilter = ColorFilter.tint(trackColorProvider),
            contentScale = androidx.glance.layout.ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxSize()
        )

        val progressBitmap = createProgressMaskBitmap(actualWidthDp, heightDp, progress, density, isWavy, dotThreshold, isTrack = false)
        Image(
            provider = ImageProvider(progressBitmap),
            contentDescription = "Progress: ${(progress * 100).toInt()}%",
            colorFilter = ColorFilter.tint(colorProvider),
            contentScale = androidx.glance.layout.ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxSize()
        )
    }
}

fun createProgressMaskBitmap(
    widthDp: Int,
    heightDp: Int,
    progress: Float,
    density: Float,
    isWavy: Boolean,
    dotThreshold: Float,
    isTrack: Boolean
): Bitmap {
    val strokeWidth = 5f * density
    val padding = strokeWidth / 2f + 2f 
    
    val width = (widthDp * density + padding * 2).toInt().coerceAtLeast(1)
    val height = (heightDp * density).toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val maskColor = android.graphics.Color.WHITE
    
    val paint = Paint().apply {
        color = maskColor
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }
    
    val centerY = height / 2f
    val effectiveWidth = width.toFloat() - (padding * 2)
    val progressWidth = effectiveWidth * progress.coerceIn(0f, 1f)
    
    val gap = if (progress > 0f && progress < 1f) 8f * density else 0f
    
    if (isTrack) {
        if (progress < 1f) {
            val trackStart = padding + progressWidth + gap
            val trackEnd = width.toFloat() - padding
            if (trackStart < trackEnd) {
                canvas.drawLine(trackStart, centerY, trackEnd, centerY, paint)
            }
        }
    } else {
        if (progress < dotThreshold) {
            paint.style = Paint.Style.FILL
            canvas.drawCircle(width.toFloat() - padding, centerY, 1.1f * density, paint)
        }
        
        if (progress > 0f) {
            paint.style = Paint.Style.STROKE
            if (isWavy) {
                val path = Path()
                path.moveTo(padding, centerY)
                val waveLength = 32f * density
                val waveHeight = 5f * density
                
                var x = 0f
                val step = 0.5f 
                while (x < progressWidth) {
                    val y = centerY + sin(x * 2 * PI / waveLength).toFloat() * (waveHeight / 2)
                    path.lineTo(padding + x, y)
                    x += step
                }
                path.lineTo(padding + progressWidth, centerY + sin(progressWidth * 2 * PI / waveLength).toFloat() * (waveHeight / 2))
                canvas.drawPath(path, paint)
            } else {
                canvas.drawLine(padding, centerY, padding + progressWidth, centerY, paint)
            }
        }
    }
    
    return bitmap
}

fun getDynamicFontSize(
    text: String,
    maxWidth: androidx.compose.ui.unit.Dp,
    baseSize: androidx.compose.ui.unit.TextUnit,
    reducedSize: androidx.compose.ui.unit.TextUnit,
    density: Float
): androidx.compose.ui.unit.TextUnit {
    val paint = Paint().apply {
        textSize = baseSize.value * density
    }
    val measuredWidthPx = paint.measureText(text)
    val maxWidthPx = maxWidth.value * density

    return if (measuredWidthPx > maxWidthPx) reducedSize else baseSize
}

fun blurBitmap(bitmap: Bitmap, context: android.content.Context, radius: Float): Bitmap {
    val outBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val rs = RenderScript.create(context)
    val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    val allIn = Allocation.createFromBitmap(rs, bitmap)
    val allOut = Allocation.createFromBitmap(rs, outBitmap)
    blurScript.setRadius(radius)
    blurScript.setInput(allIn)
    blurScript.forEach(allOut)
    allOut.copyTo(outBitmap)
    rs.destroy()
    return outBitmap
}

fun processImage(context: android.content.Context, uri: android.net.Uri, fileName: String): Pair<String, androidx.palette.graphics.Palette>? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        if (originalBitmap == null) return null
        
        val maxDimension = 600
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = maxDimension.toFloat() / Math.max(width, height).coerceAtLeast(1)
        
        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
        } else {
            originalBitmap
        }
        
        val palette = androidx.palette.graphics.Palette.from(bitmap).generate()
        val file = File(context.filesDir, fileName)
        val out = java.io.FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
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
