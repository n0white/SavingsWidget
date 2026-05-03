package com.n0white.n0widgets

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.n0white.n0widgets.data.CounterRepository
import com.n0white.n0widgets.data.GoalRepository
import com.n0white.n0widgets.ui.MainScreen
import com.n0white.n0widgets.ui.theme.SavingsWidgetTheme

class MainActivity : ComponentActivity() {
    private lateinit var goalRepository: GoalRepository
    private lateinit var counterRepository: CounterRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goalRepository = GoalRepository(this)
        counterRepository = CounterRepository(this)
        com.n0white.n0widgets.ui.widget.MidnightUpdater.schedule(this)
        enableEdgeToEdge()

        setContent {
            SavingsWidgetTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    ScreenScaffold(
        title = {
            Text(
                text = buildAnnotatedString {
                    append("n")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onPrimaryContainer)) {
                        append("0")
                    }
                    append("widgets")
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium
            )
        },
        onBack = null,
        isMain = true
    ) {
        MainScreen(
            onNavigateToSavings = {
                val intent = Intent(context, SavingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                context.startActivity(intent)
            },
            onNavigateToCounter = {
                val intent = Intent(context, CounterActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                context.startActivity(intent)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)?,
    isMain: Boolean,
    content: @Composable () -> Unit
) {
    val scrollBehavior = if (isMain) {
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    } else {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        topBar = {
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .statusBarsPadding()
            ) {
                if (isMain) {
                    TopAppBar(
                        title = title,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        scrollBehavior = scrollBehavior
                    )
                } else {
                    LargeTopAppBar(
                        title = {
                            val collapsedFraction = scrollBehavior.state.collapsedFraction
                            // Текст зникає швидше (до 35% прокрутки) і з'являється пізніше (після 65%),
                            // щоб уникнути накладання в проміжній зоні
                            val titleAlpha = when {
                                collapsedFraction < 0.35f -> 1f - (collapsedFraction / 0.35f)
                                collapsedFraction > 0.65f -> (collapsedFraction - 0.65f) / 0.35f
                                else -> 0f
                            }

                            Box(modifier = Modifier
                                .graphicsLayer { alpha = titleAlpha }
                                .padding(start = 16.dp)
                            ) {
                                val style = if (collapsedFraction > 0.5f) {
                                    MaterialTheme.typography.titleLarge
                                } else {
                                    MaterialTheme.typography.headlineLarge
                                }
                                ProvideTextStyle(value = style) {
                                    title()
                                }
                            }
                        },
                        navigationIcon = {
                            if (onBack != null) {
                                FilledTonalIconButton(
                                    onClick = onBack,
                                    modifier = Modifier.padding(start = 23.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                    )
                                ) {
                                    // Explicit import-less icon usage if needed, but we have the import
                                    Icon(
                                        Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = stringResource(R.string.back)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent, // Background is handled by the parent Box
                            scrolledContainerColor = Color.Transparent
                        ),
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .clipToBounds()
        ) {
            content()
        }
    }
}