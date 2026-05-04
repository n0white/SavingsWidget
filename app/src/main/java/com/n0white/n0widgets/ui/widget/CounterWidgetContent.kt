package com.n0white.n0widgets.ui.widget

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.n0white.n0widgets.CounterActivity
import com.n0white.n0widgets.R
import com.n0white.n0widgets.data.model.Counter
import java.io.File
import kotlin.math.roundToInt

@Composable
fun CounterWidgetContent(counter: Counter) {
    val size = LocalSize.current
    val context = LocalContext.current
    val isSmall = size.width < 200.dp
    val colors = GlanceTheme.colors
    
    val sw = context.resources.configuration.smallestScreenWidthDp
    val isHighRes = sw >= 400

    val isBgImagePresent = !counter.backgroundImagePath.isNullOrEmpty()
    val isBgImageDark = counter.customOnSurface?.let { 
        val c = Color(it)
        (0.299 * c.red + 0.587 * c.green + 0.114 * c.blue) > 0.5 
    } ?: false

    val primaryColor = counter.customPrimary?.let { androidx.glance.unit.ColorProvider(Color(it)) } 
        ?: if (isBgImagePresent) {
            if (isBgImageDark) colors.primaryContainer else colors.primary
        } else colors.primary

    val secondaryContainerColor = counter.customSecondaryContainer?.let { androidx.glance.unit.ColorProvider(Color(it)) }
        ?: if (isBgImagePresent) {
            if (isBgImageDark) colors.secondaryContainer else colors.secondary
        } else colors.secondaryContainer

    val onSurfaceColor = counter.customOnSurface?.let { androidx.glance.unit.ColorProvider(Color(it)) } ?: colors.onSurface
    val onSurfaceVariantColor = counter.customOnSurface?.let { androidx.glance.unit.ColorProvider(Color(it)) } ?: colors.onSurfaceVariant
    
    val tertiaryContainerColor = counter.customPrimary?.let { androidx.glance.unit.ColorProvider(Color(it).copy(alpha = 0.2f)) }
        ?: if (isBgImagePresent) {
            if (isBgImageDark) colors.tertiaryContainer else colors.tertiary
        } else colors.tertiaryContainer

    val onTertiaryContainerColor = counter.customOnSurface?.let { androidx.glance.unit.ColorProvider(Color(it)) }
        ?: if (isBgImagePresent) {
            if (isBgImageDark) colors.onTertiaryContainer else colors.onTertiary
        } else colors.onTertiaryContainer

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(colors.widgetBackground)
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(android.content.Intent(context, CounterActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }))
        ) {
            // Background Image
            if (!counter.backgroundImagePath.isNullOrEmpty()) {
                val file = File(counter.backgroundImagePath)
                if (file.exists()) {
                    val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (originalBitmap != null) {
                        val bitmap = if (counter.isBlurEnabled) {
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
                                .background(Color.Black.copy(alpha = 0.5f))
                                .cornerRadius(24.dp)
                        ) {}
                    }
                }
            }

            Column(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 1. Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = context.getString(R.string.counter_label),
                            style = TextStyle(
                                fontSize = if (isHighRes) 11.sp else 10.sp, 
                                color = onSurfaceVariantColor
                            )
                        )
                        val density = context.resources.displayMetrics.density
                        val baseFontSize = if (isHighRes) 21.sp else 19.sp
                        val reducedFontSize = if (isHighRes) 17.sp else 16.sp
                        
                        val availableWidthDp = (size.width.value.toInt() - 32 - 48 - 12).dp
                        
                        val displayName = counter.name.ifBlank { context.getString(R.string.default_counter_name) }
                        val finalFontSize = if (!isSmall) {
                            getDynamicFontSize(
                                text = displayName,
                                maxWidth = availableWidthDp,
                                baseSize = baseFontSize,
                                reducedSize = reducedFontSize,
                                density = density
                            )
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

                    if (counter.emoji.isNotEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .size(if (isHighRes) 48.dp else 44.dp)
                                .background(tertiaryContainerColor)
                                .cornerRadius(if (isHighRes) 14.dp else 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = counter.emoji, 
                                style = TextStyle(fontSize = if (isHighRes) 22.sp else 19.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // 2. Middle section (Progress block)
                Column(modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp)) {
                    if (isSmall) {
                        Text(
                            text = counter.getRemainingTimeString(context),
                            style = TextStyle(
                                fontSize = if (isHighRes) 22.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = context.getString(R.string.to_target, counter.getTargetDateString(isShort = true)),
                                style = TextStyle(fontSize = if (isHighRes) 12.sp else 10.sp, color = onSurfaceVariantColor)
                            )
                            
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            StatusChip(
                                text = counter.getMotivationPhrase(context, true), 
                                isHighRes = isHighRes,
                                containerColor = tertiaryContainerColor,
                                contentColor = onTertiaryContainerColor
                            )
                        }
                    } else {
                        // Large Widget
                        StatusChip(
                            text = counter.getMotivationPhrase(context, false), 
                            isHighRes = isHighRes,
                            containerColor = tertiaryContainerColor,
                            contentColor = onTertiaryContainerColor
                        )
                        Spacer(modifier = GlanceModifier.height(1.dp))
                        
                        val density = context.resources.displayMetrics.density
                        val remainingText = counter.getRemainingTimeString(context)
                        
                        val baseAmountSize = if (isHighRes) 32.sp else 28.sp
                        val reducedAmountSize = if (isHighRes) 24.sp else 22.sp
                        
                        val amountAvailableWidth = (size.width.value.toInt() - 32 - 100).dp
                        val finalAmountFontSize = getDynamicFontSize(
                            text = remainingText,
                            maxWidth = amountAvailableWidth,
                            baseSize = baseAmountSize,
                            reducedSize = reducedAmountSize,
                            density = density
                        )

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically 
                        ) {
                            Text(
                                text = remainingText,
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
                                    text = counter.getTargetDateString(),
                                    style = TextStyle(fontSize = if (isHighRes) 12.sp else 10.sp, color = onSurfaceVariantColor)
                                )
                            }
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(if (isHighRes) 4.dp else 2.dp))

                    // Progress Bar
                    WavyProgressIndicator(
                        progress = counter.progress,
                        colorProvider = primaryColor,
                        trackColorProvider = secondaryContainerColor,
                        isWavy = counter.isWavy,
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
                        text = "${(counter.progress * 100).roundToInt()}%",
                        style = TextStyle(
                            fontSize = if (isHighRes) 14.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    
                    val footerLabel = if (isSmall) context.getString(R.string.passed_label_short) else context.getString(R.string.passed_label)
                    Text(
                        text = "$footerLabel${counter.getPassedTimeString(context)}",
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
fun StatusChip(
    text: String, 
    isHighRes: Boolean = false,
    containerColor: androidx.glance.unit.ColorProvider? = null,
    contentColor: androidx.glance.unit.ColorProvider? = null
) {
    val colors = GlanceTheme.colors
    val finalContainer = containerColor ?: colors.tertiaryContainer
    val finalContent = contentColor ?: colors.onTertiaryContainer
    
    Box(
        modifier = GlanceModifier
            .background(finalContainer)
            .cornerRadius(10.dp)
            .padding(
                horizontal = if (isHighRes) 10.dp else 8.dp, 
                vertical = if (isHighRes) 3.dp else 2.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = if (isHighRes) 10.sp else 9.sp,
                fontWeight = FontWeight.Bold,
                color = finalContent
            )
        )
    }
}
