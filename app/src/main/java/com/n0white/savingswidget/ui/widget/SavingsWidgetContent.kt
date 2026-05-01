package com.n0white.savingswidget.ui.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.Composable
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
import com.n0white.savingswidget.MainActivity
import com.n0white.savingswidget.data.model.Goal
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SavingsWidgetContent(goal: Goal) {
    val size = LocalSize.current
    val context = LocalContext.current
    val isSmall = size.width < 200.dp 
    val colors = GlanceTheme.colors
    
    val sw = context.resources.configuration.smallestScreenWidthDp
    val isHighRes = sw >= 400

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.widgetBackground)
                .cornerRadius(24.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp) 
                .clickable(actionStartActivity(android.content.Intent(context, MainActivity::class.java)))
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {

                // 1. Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Savings",
                            style = TextStyle(
                                fontSize = if (isHighRes) 11.sp else 10.sp, 
                                color = colors.onSurfaceVariant
                            )
                        )
                        Text(
                            text = goal.name,
                            style = TextStyle(
                                fontSize = when {
                                    isSmall && isHighRes -> 19.sp
                                    isSmall -> 18.sp
                                    isHighRes -> 24.sp
                                    else -> 21.sp
                                },
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface
                            ),
                            maxLines = 2
                        )
                    }
                    
                    Spacer(modifier = GlanceModifier.width(12.dp))

                    if (goal.emoji.isNotEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .size(if (isHighRes) 48.dp else 44.dp)
                                .background(colors.secondaryContainer) 
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

                // 2. Middle section (Progress block)
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    if (isSmall) {
                        Text(
                            text = "${goal.currency}${goal.savedAmount.formatAmount()}",
                            style = TextStyle(
                                fontSize = if (isHighRes) 25.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "of ${goal.currency}${goal.targetAmount.formatAmount()}",
                                style = TextStyle(fontSize = if (isHighRes) 12.sp else 10.sp, color = colors.onSurfaceVariant)
                            )
                            
                            if (goal.savedAmount > 0) {
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                MonthlyEfficiencyChip(efficiency = goal.monthlyEfficiency, compact = true, isHighRes = isHighRes)
                            }
                        }
                    } else {
                        // Large Widget
                        if (goal.savedAmount > 0) {
                            MonthlyEfficiencyChip(efficiency = goal.monthlyEfficiency, isHighRes = isHighRes)
                            Spacer(modifier = GlanceModifier.height(4.dp))
                        }
                        
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically 
                        ) {
                            Text(
                                text = "${goal.currency}${goal.savedAmount.formatAmount()}",
                                style = TextStyle(
                                    fontSize = if (isHighRes) 36.sp else 31.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "target",
                                    style = TextStyle(fontSize = if (isHighRes) 10.sp else 9.sp, color = colors.onSurfaceVariant)
                                )
                                Text(
                                    text = "${goal.currency}${goal.targetAmount.formatAmount()}",
                                    style = TextStyle(fontSize = if (isHighRes) 14.sp else 12.sp, color = colors.onSurfaceVariant)
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(if (isHighRes) 4.dp else 2.dp))

                    // Progress Bar
                    WavyProgressIndicator(
                        progress = goal.progress,
                        colorProvider = colors.primary,
                        trackColorProvider = colors.secondaryContainer,
                        isWavy = goal.isWavy,
                        modifier = GlanceModifier.fillMaxWidth().height(if (isHighRes) 16.dp else 14.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(if (isHighRes) 8.dp else 6.dp))

                // 3. Footer
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(goal.progress * 100).roundToInt()}%",
                        style = TextStyle(
                            fontSize = if (isHighRes) 14.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    
                    val footerText = if (isSmall) "left: " else "remaining: "
                    Text(
                        text = "$footerText${goal.currency}${goal.remaining.formatAmount()}",
                        style = TextStyle(
                            fontSize = if (isHighRes) 12.sp else 10.sp,
                            color = colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyEfficiencyChip(efficiency: Int, compact: Boolean = false, isHighRes: Boolean = false) {
    val colors = GlanceTheme.colors
    Box(
        modifier = GlanceModifier
            .background(colors.tertiaryContainer)
            .cornerRadius(10.dp)
            .padding(
                horizontal = if (compact) 4.dp else (if (isHighRes) 10.dp else 8.dp), 
                vertical = if (compact) 1.dp else (if (isHighRes) 3.dp else 2.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (compact) "+$efficiency%" else "+$efficiency% this month",
            style = TextStyle(
                fontSize = if (compact) 8.sp else (if (isHighRes) 10.sp else 9.sp),
                fontWeight = FontWeight.Bold,
                color = colors.onTertiaryContainer
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
    
    // Width logic: We calculate the actual expected width in DP based on the widget size.
    // Standard horizontal padding for our widget is 16.dp * 2 = 32.dp.
    val actualWidthDp = if (size.width.value > 0) (size.width.value.toInt() - 32).coerceAtLeast(100) else 160
    val heightDp = 18
    
    val isLargeWidget = size.width >= 150.dp && size.height >= 150.dp
    val dotThreshold = if (isLargeWidget) 0.97f else 0.98f
    
    Box(modifier = modifier) {
        val trackBitmap = createProgressMaskBitmap(actualWidthDp, heightDp, progress, density, isWavy, dotThreshold, isTrack = true)
        Image(
            provider = ImageProvider(trackBitmap),
            contentDescription = null,
            colorFilter = ColorFilter.tint(trackColorProvider),
            // Important: Use FillBounds but with a bitmap that MATCHES the container's width
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

private fun createProgressMaskBitmap(
    widthDp: Int,
    heightDp: Int,
    progress: Float,
    density: Float,
    isWavy: Boolean,
    dotThreshold: Float,
    isTrack: Boolean
): Bitmap {
    val strokeWidth = 5f * density
    val padding = strokeWidth / 2f + 2f // Safety margin to avoid clipping rounded caps
    
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
                // wavelength is now fixed in PIXELS relative to DENSITY, 
                // so it stays consistent regardless of container width.
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
