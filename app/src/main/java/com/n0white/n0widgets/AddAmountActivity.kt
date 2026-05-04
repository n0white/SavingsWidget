package com.n0white.n0widgets

import android.os.Build
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.n0white.n0widgets.data.GoalRepository
import com.n0white.n0widgets.ui.theme.SavingsWidgetTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

fun android.view.animation.Interpolator.toEasing() = Easing { x -> getInterpolation(x) }

class AddAmountActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        val repository = GoalRepository(this)
        val sourceBounds = intent.sourceBounds

        setContent {
            SavingsWidgetTheme {
                val scope = rememberCoroutineScope()
                var amountText by remember { mutableStateOf("") }
                val goalState = repository.goalFlow.collectAsState(initial = null)
                val focusRequester = remember { FocusRequester() }

                var isClosing by remember { mutableStateOf(false) }
                val animationProgress = remember { Animatable(0f) }

                val performClose = {
                    if (!isClosing) {
                        isClosing = true
                        scope.launch {
                            animationProgress.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                            )
                            finish()
                            overridePendingTransition(0, 0)
                        }
                    }
                }

                // Standard way to handle back with predictive support in Compose
                androidx.activity.compose.BackHandler(enabled = true) {
                    performClose()
                }

                LaunchedEffect(Unit) {
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 400, easing = OvershootInterpolator(1.2f).toEasing())
                    )
                    delay(100)
                    focusRequester.requestFocus()
                }

                val density = LocalDensity.current
                val configuration = LocalConfiguration.current
                
                // Use actual screen dimensions in pixels
                val displayMetrics = android.util.DisplayMetrics()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                val screenWidthPx = displayMetrics.widthPixels.toFloat()
                val screenHeightPx = displayMetrics.heightPixels.toFloat()

                // Calculate pivot and initial scale based on widget bounds
                val widgetWidth = sourceBounds?.width()?.toFloat() ?: 0f
                val widgetHeight = sourceBounds?.height()?.toFloat() ?: 0f
                
                val initialScaleX = if (widgetWidth > 0) (widgetWidth / screenWidthPx).coerceIn(0.1f, 1f) else 0.5f
                val initialScaleY = if (widgetHeight > 0) (widgetHeight / screenHeightPx).coerceIn(0.1f, 1f) else 0.5f

                val pivotX = if (sourceBounds != null) {
                    (sourceBounds.centerX().toFloat() / screenWidthPx).coerceIn(0f, 1f)
                } else 0.5f

                val pivotY = if (sourceBounds != null) {
                    (sourceBounds.centerY().toFloat() / screenHeightPx).coerceIn(0f, 1f)
                } else 0.5f

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { performClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                            .graphicsLayer {
                                scaleX = initialScaleX + (1f - initialScaleX) * animationProgress.value
                                scaleY = initialScaleY + (1f - initialScaleY) * animationProgress.value
                                alpha = animationProgress.value.coerceIn(0f, 1f)
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(pivotX, pivotY)
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* Prevent clicks on card from closing */ },
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.add_amount_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amountText = it },
                                label = { Text(stringResource(R.string.amount_to_add)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { performClose() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(android.R.string.cancel))
                                }

                                Button(
                                    onClick = {
                                        val amount = amountText.toDoubleOrNull() ?: 0.0
                                        if (amount > 0) {
                                            scope.launch {
                                                val currentGoal = goalState.value ?: repository.goalFlow.first()
                                                repository.updateGoal(
                                                    currentGoal.copy(
                                                        savedAmount = currentGoal.savedAmount + amount
                                                    )
                                                )
                                                performClose()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = amountText.isNotEmpty() && !isClosing
                                ) {
                                    Text(stringResource(R.string.add))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
