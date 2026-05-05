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
import androidx.glance.unit.ColorProvider
import androidx.glance.color.ColorProvider
import com.n0white.n0widgets.CounterActivity
import com.n0white.n0widgets.R
import com.n0white.n0widgets.data.model.Counter
import java.io.File
import kotlin.math.roundToInt

@Composable
fun CounterWidgetContent(counter: Counter, isThemeBackgroundEnabled: Boolean) {
    val size = LocalSize.current
    val context = LocalContext.current
    val isSmall = size.width < 200.dp
    val sw = context.resources.configuration.smallestScreenWidthDp
    val isHighRes = sw >= 400

    GlanceTheme {
        val colors = GlanceTheme.colors
        val isBgImagePresent = !counter.backgroundImagePath.isNullOrEmpty()
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

        val primaryColor = if (counter.customPrimary != null && counter.customPrimaryInverse != null) {
            if (forceDark) {
                androidx.glance.unit.ColorProvider(Color(counter.customPrimary))
            } else {
                androidx.glance.color.ColorProvider(day = Color(counter.customPrimaryInverse), night = Color(counter.customPrimary))
            }
        } else {
            counter.customPrimary?.let { androidx.glance.unit.ColorProvider(Color(it)) }
                ?: if (isBgImagePresent) onImagePrimary else colors.primary
        }

        val secondaryContainerColor = counter.customSecondaryContainer?.let { androidx.glance.unit.ColorProvider(Color(it)) }
            ?: if (isBgImagePresent) onImageSecondary else colors.secondaryContainer

        val onSurfaceColor = if (counter.customOnSurface != null && counter.customOnSurfaceInverse != null) {
            if (forceDark) {
                androidx.glance.unit.ColorProvider(Color(counter.customOnSurface))
            } else {
                androidx.glance.color.ColorProvider(day = Color(counter.customOnSurfaceInverse), night = Color(counter.customOnSurface))
            }
        } else {
            counter.customOnSurface?.let { androidx.glance.unit.ColorProvider(Color(it)) } ?: colors.onSurface
        }
        
        val onSurfaceVariantColor = onSurfaceColor
        
        val tertiaryContainerColor = if (counter.customPrimary != null && counter.customPrimaryInverse != null) {
            if (forceDark) {
                androidx.glance.unit.ColorProvider(Color(counter.customPrimary).copy(alpha = 0.2f))
            } else {
                androidx.glance.color.ColorProvider(
                    day = Color(counter.customPrimaryInverse).copy(alpha = 0.2f),
                    night = Color(counter.customPrimary).copy(alpha = 0.2f)
                )
            }
        } else {
            counter.customPrimary?.let { androidx.glance.unit.ColorProvider(Color(it).copy(alpha = 0.2f)) }
                ?: if (isBgImagePresent) onImageTertiaryContainer else colors.tertiaryContainer
        }

        val onTertiaryContainerColor = onSurfaceColor

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(if (forceDark) androidx.glance.unit.ColorProvider(Color(context.getColor(R.color.widget_background_night))) else colors.widgetBackground)
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(android.content.Intent(context, CounterActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }))
        ) {
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

                Column(modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp)) {
                    val mainText = if (counter.isInfinite) counter.getPassedTimeString(context) else counter.getRemainingTimeString(context)
                    
                    if (isSmall) {
                        Text(
                            text = mainText,
                            style = TextStyle(
                                fontSize = if (isHighRes) 22.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val targetLabel = if (counter.isInfinite) {
                                context.getString(R.string.target_milestone, counter.getNextMilestoneString(context))
                            } else {
                                context.getString(R.string.to_target, counter.getTargetDateString(isShort = true))
                            }
                            Text(
                                text = targetLabel,
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
                        StatusChip(
                            text = counter.getMotivationPhrase(context, false), 
                            isHighRes = isHighRes,
                            containerColor = tertiaryContainerColor,
                            contentColor = onTertiaryContainerColor
                        )
                        Spacer(modifier = GlanceModifier.height(1.dp))
                        
                        val density = context.resources.displayMetrics.density
                        val baseAmountSize = if (isHighRes) 32.sp else 28.sp
                        val reducedAmountSize = if (isHighRes) 24.sp else 22.sp
                        val amountAvailableWidth = (size.width.value.toInt() - 32 - 100).dp
                        val finalAmountFontSize = getDynamicFontSize(mainText, amountAvailableWidth, baseAmountSize, reducedAmountSize, density)

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically 
                        ) {
                            Text(
                                text = mainText,
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
                                    text = if (counter.isInfinite) counter.getNextMilestoneString(context) else counter.getTargetDateString(),
                                    style = TextStyle(fontSize = if (isHighRes) 12.sp else 10.sp, color = onSurfaceVariantColor)
                                )
                            }
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(if (isHighRes) 4.dp else 2.dp))

                    WavyProgressIndicator(
                        progress = counter.progress,
                        colorProvider = primaryColor,
                        trackColorProvider = secondaryContainerColor,
                        isWavy = counter.isWavy,
                        modifier = GlanceModifier.fillMaxWidth().height(if (isHighRes) 16.dp else 14.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(if (isHighRes) 8.dp else 6.dp))

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
                    
                    val footerLabel = if (counter.isInfinite) {
                        context.getString(R.string.remaining_milestone_label)
                    } else if (isSmall) {
                        context.getString(R.string.passed_label_short)
                    } else {
                        context.getString(R.string.passed_label)
                    }
                    
                    val footerValue = if (counter.isInfinite) {
                        counter.getRemainingTimeString(context)
                    } else {
                        counter.getPassedTimeString(context)
                    }

                    Text(
                        text = "$footerLabel$footerValue",
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
