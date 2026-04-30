package com.example.savingswidget.ui.widget

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.Composable
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
import com.example.savingswidget.data.model.Goal
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SavingsWidgetContent(goal: Goal) {
    val size = LocalSize.current
    val isSmall = size.width < 200.dp 
    val colors = GlanceTheme.colors

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.widgetBackground)
                .cornerRadius(24.dp)
                .padding(16.dp)
                .clickable(actionStartActivity(Intent().setClassName("com.example.savingswidget", "com.example.savingswidget.MainActivity")))
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {

                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Savings",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = colors.onSurfaceVariant
                            )
                        )
                        Text(
                            text = goal.name,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface
                            ),
                            maxLines = 2
                        )
                    }
                    if (goal.emoji.isNotEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .size(48.dp)
                                .background(colors.secondaryContainer) 
                                .cornerRadius(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = goal.emoji, 
                                style = TextStyle(
                                    fontSize = 22.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Middle section: Amounts & Progress
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    if (isSmall) {
                        Text(
                            text = "${goal.currency}${goal.savedAmount.formatAmount()}",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        )
                        Text(
                            text = "of ${goal.currency}${goal.targetAmount.formatAmount()}",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = colors.onSurfaceVariant
                            )
                        )
                    } else {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${goal.currency}${goal.savedAmount.formatAmount()}",
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "target",
                                    style = TextStyle(fontSize = 11.sp, color = colors.onSurfaceVariant)
                                )
                                Text(
                                    text = "${goal.currency}${goal.targetAmount.formatAmount()}",
                                    style = TextStyle(fontSize = 14.sp, color = colors.onSurfaceVariant)
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    // THE FIX: We use a mask-based approach. 
                    // We draw the progress bar in WHITE on a transparent background, 
                    // then use Glance's ColorFilter.tint() to apply the THEMED color.
                    // This allows the system to change the color without re-generating the bitmap.
                    WavyProgressIndicator(
                        progress = goal.progress,
                        colorProvider = colors.primary,
                        trackColorProvider = colors.secondaryContainer,
                        isWavy = goal.isWavy,
                        modifier = GlanceModifier.fillMaxWidth().height(16.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom section: Progress footer
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(goal.progress * 100).roundToInt()}%",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    
                    val footerText = if (isSmall) "left: " else "remaining: "
                    Text(
                        text = "$footerText${goal.currency}${goal.remaining.formatAmount()}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
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
    
    val widthDp = if (size.width > 0.dp) size.width.value.toInt() - 32 else 200
    val heightDp = 16
    
    val isLargeWidget = size.width >= 150.dp && size.height >= 150.dp
    val dotThreshold = if (isLargeWidget) 0.97f else 0.98f
    
    // We create TWO layers. One for track, one for progress.
    // Each is a "mask" (pure white) that gets tinted by Glance.
    
    Box(modifier = modifier) {
        // 1. Track Layer (Tinted with secondaryContainer)
        val trackBitmap = createProgressMaskBitmap(widthDp, heightDp, progress, density, isWavy, dotThreshold, isTrack = true)
        Image(
            provider = ImageProvider(trackBitmap),
            contentDescription = null,
            colorFilter = ColorFilter.tint(trackColorProvider),
            modifier = GlanceModifier.fillMaxSize()
        )

        // 2. Progress Layer (Tinted with primary)
        val progressBitmap = createProgressMaskBitmap(widthDp, heightDp, progress, density, isWavy, dotThreshold, isTrack = false)
        Image(
            provider = ImageProvider(progressBitmap),
            contentDescription = "Progress: ${(progress * 100).toInt()}%",
            colorFilter = ColorFilter.tint(colorProvider),
            modifier = GlanceModifier.fillMaxSize()
        )
    }
}

/**
 * Creates a "Mask" bitmap where the shapes are drawn in solid WHITE.
 * Glance's ColorFilter.tint() will then replace this white with the theme-aware color.
 */
private fun createProgressMaskBitmap(
    widthDp: Int,
    heightDp: Int,
    progress: Float,
    density: Float,
    isWavy: Boolean,
    dotThreshold: Float,
    isTrack: Boolean
): Bitmap {
    val width = (widthDp * density).toInt().coerceAtLeast(1)
    val height = (heightDp * density).toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // We draw with PURE WHITE. Tinting will handle the theme colors.
    val maskColor = android.graphics.Color.WHITE
    
    val paint = Paint().apply {
        color = maskColor
        style = Paint.Style.STROKE
        strokeWidth = 6f * density
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }
    
    val centerY = height / 2f
    val strokeWidth = 6f * density
    val margin = strokeWidth / 2f 
    val effectiveWidth = width - (margin * 2)
    val progressWidth = effectiveWidth * progress.coerceIn(0f, 1f)
    val gap = 8f * density
    
    if (isTrack) {
        // Draw Track part of the mask
        if (progress < 1f) {
            val trackStart = margin + progressWidth + gap
            if (trackStart < width - margin) {
                canvas.drawLine(trackStart, centerY, width - margin, centerY, paint)
            }
        }
    } else {
        // Draw Progress part of the mask
        
        // 1. Draw Stop Dot
        if (progress < dotThreshold) {
            paint.style = Paint.Style.FILL
            canvas.drawCircle(width - margin - 1f * density, centerY, 1.25f * density, paint)
        }
        
        // 2. Draw Progress Line/Wave
        if (progress > 0f) {
            paint.style = Paint.Style.STROKE
            if (isWavy) {
                val path = Path()
                path.moveTo(margin, centerY)
                val waveLength = 32f * density
                val waveHeight = 5f * density
                
                var x = 0f
                val step = 0.5f 
                while (x < progressWidth) {
                    val y = centerY + sin(x * 2 * PI / waveLength).toFloat() * (waveHeight / 2)
                    path.lineTo(margin + x, y)
                    x += step
                }
                path.lineTo(margin + progressWidth, centerY + sin(progressWidth * 2 * PI / waveLength).toFloat() * (waveHeight / 2))
                canvas.drawPath(path, paint)
            } else {
                canvas.drawLine(margin, centerY, margin + progressWidth, centerY, paint)
            }
        }
    }
    
    return bitmap
}
