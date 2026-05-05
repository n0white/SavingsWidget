package com.n0white.n0widgets

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.n0white.n0widgets.data.AppSettingsRepository
import com.n0white.n0widgets.ui.widget.SavingsWidget
import com.n0white.n0widgets.ui.widget.CounterWidget
import com.n0white.n0widgets.ui.widget.WidgetPreviewManager
import androidx.glance.appwidget.updateAll
import com.n0white.n0widgets.ui.theme.SavingsWidgetTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    private lateinit var appSettingsRepository: AppSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appSettingsRepository = AppSettingsRepository(this)
        enableEdgeToEdge()

        setContent {
            SavingsWidgetTheme {
                val isThemeBackgroundEnabled by appSettingsRepository.isThemeBackgroundEnabled.collectAsState(initial = false)
                val scope = rememberCoroutineScope()
                val view = LocalView.current

                ScreenScaffold(
                    title = {
                        Text(
                            text = stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 26.sp
                        )
                    },
                    onBack = { finish() },
                    isMain = false
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 15.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
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

                                Surface(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        scope.launch {
                                            appSettingsRepository.setThemeBackgroundEnabled(!isThemeBackgroundEnabled)
                                            SavingsWidget().updateAll(this@SettingsActivity)
                                            CounterWidget().updateAll(this@SettingsActivity)
                                            WidgetPreviewManager.updateWidgetPreviews(this@SettingsActivity)
                                        }
                                    },
                                    shape = RoundedCornerShape(24.dp),
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
                                                Icons.Outlined.Palette,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = stringResource(R.string.theme_background_title),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = stringResource(R.string.theme_background_description),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = isThemeBackgroundEnabled,
                                            onCheckedChange = { enabled ->
                                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                scope.launch {
                                                    appSettingsRepository.setThemeBackgroundEnabled(enabled)
                                                    SavingsWidget().updateAll(this@SettingsActivity)
                                                    CounterWidget().updateAll(this@SettingsActivity)
                                                    WidgetPreviewManager.updateWidgetPreviews(this@SettingsActivity)
                                                }
                                            },
                                            modifier = Modifier.scale(1.1f).padding(start = 12.dp),
                                            thumbContent = if (isThemeBackgroundEnabled) {
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
                    }
                }
            }
        }
    }
}
