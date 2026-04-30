package com.n0white.savingswidget.ui.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.LocalContext
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
import com.n0white.savingswidget.R
import com.n0white.savingswidget.MainActivity
import com.n0white.savingswidget.data.model.Goal
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SavingsWidgetContent(goal: Goal) {
    val size = LocalSize.current
    val isSmall = size.width < 200.dp
    val context = LocalContext.current

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
                .clickable(actionStartActivity(Intent().setClassName("com.n0white.savingswidget", "com.n0white.savingswidget.MainActivity")))
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
                            text = context.getString(R.string.widget_header_title),
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
                            text = "${context.getString(R.string.label_of)}${goal.currency}${goal.targetAmount.formatAmount()}",
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
                                    text = context.getString(R.string.label_target),
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

                    LinearProgressIndicator(
                        progress = goal.progress,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .cornerRadius(3.dp),
                        color = colors.primary,
                        backgroundColor = colors.secondaryContainer
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

                    val footerText = if (isSmall) context.getString(R.string.label_left) else context.getString(R.string.label_remaining)
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
