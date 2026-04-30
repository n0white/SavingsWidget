package com.example.savingswidget.ui.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.LocalSize
import androidx.glance.layout.Alignment
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import com.example.savingswidget.MainActivity
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

    GlanceTheme {
        val colors = GlanceTheme.colors
        
        /**
         * Refined Inverted Style with Balanced Hierarchy:
         * 
         * 1. Background: 'onSecondary' (Pure neutral base)
         * 2. Main Accents: 'primary' (Color for progress, saved amount)
         * 3. Main Content: 'onSurface' (Clear contrast for Goal Name)
         * 4. Labels: 'onSurfaceVariant' (Neutral grey for technical labels)
         * 5. Interaction: Click to open MainActivity
         */
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
                                    fontSize = 22.sp,
                                    color = colors.onSecondaryContainer
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

                    WavyProgressIndicator(
                        progress = goal.progress,
                        color = colors.primary.getColor(LocalContext.current),
                        trackColor = colors.secondaryContainer.getColor(LocalContext.current),
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
    color: androidx.compose.ui.graphics.Color,
    trackColor: androidx.compose.ui.graphics.Color,
    isWavy: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    val size = LocalSize.current
    
    // We estimate the width based on LocalSize. If it's wrap_content it might be tricky,
    // but in our widget it's usually fillMaxWidth or a fixed size.
    val widthDp = if (size.width > 0.dp) size.width.value.toInt() - 32 else 200 // 32 for padding
    val heightDp = 16
    
    // 97% for 2x2 widgets (usually > 150dp width/height), 98% for others
    val isLargeWidget = size.width >= 150.dp && size.height >= 150.dp
    val dotThreshold = if (isLargeWidget) 0.97f else 0.98f
    
    val bitmap = createWavyProgressBitmap(widthDp, heightDp, progress, color.toArgb(), trackColor.toArgb(), density, isWavy, dotThreshold)
    
    Image(
        provider = ImageProvider(bitmap),
        contentDescription = "Progress: ${(progress * 100).toInt()}%",
        modifier = modifier
    )
}

private fun createWavyProgressBitmap(
    widthDp: Int,
    heightDp: Int,
    progress: Float,
    color: Int,
    trackColor: Int,
    density: Float,
    isWavy: Boolean,
    dotThreshold: Float
): Bitmap {
    val width = (widthDp * density).toInt().coerceAtLeast(1)
    val height = (heightDp * density).toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = 6f * density // Increased thickness to 6dp
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }
    
    val centerY = height / 2f
    val strokeWidth = 6f * density
    val margin = strokeWidth / 2f // Add margin to prevent clipping of rounded caps
    val effectiveWidth = width - (margin * 2)
    val progressWidth = effectiveWidth * progress.coerceIn(0f, 1f)
    
    val gap = 8f * density // Adjusted gap to 8dp
    
    // 1. Draw Track
    if (progress < 1f) {
        paint.color = trackColor
        val trackStart = margin + progressWidth + gap
        if (trackStart < width - margin) {
            canvas.drawLine(trackStart, centerY, width - margin, centerY, paint)
        }
    }
    
    // 2. Draw Stop Dot
    if (progress < dotThreshold) {
        paint.style = Paint.Style.FILL
        paint.color = color
        // Position dot closer to the end of the track
        canvas.drawCircle(width - margin - 1f * density, centerY, 1.25f * density, paint)
    }
    
    // 3. Draw Progress (Wavy or Straight)
    if (progress > 0f) {
        paint.style = Paint.Style.STROKE
        paint.color = color
        if (isWavy) {
            val path = Path()
            path.moveTo(margin, centerY)
            val waveLength = 32f * density // Increased from 24dp for smoother wave
            val waveHeight = 5f * density // Slightly reduced for smoother wave
            
            var x = 0f
            val step = 0.5f // Even smaller step for maximum smoothness in Bitmap
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
    
    return bitmap
}
