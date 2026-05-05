package com.n0white.n0widgets

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
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
        com.n0white.n0widgets.ui.widget.WidgetPreviewManager.updateWidgetPreviews(this)
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
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append("0")
                    }
                    append("widgets")
                },
                modifier = Modifier.padding(start = 12.dp),
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
            },
            onNavigateToSettings = {
                val intent = Intent(context, SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                context.startActivity(intent)
            },
            onNavigateToAbout = {
                val intent = Intent(context, AboutActivity::class.java)
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
    val view = LocalView.current
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
                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    
                    Box {
                        LargeTopAppBar(
                            title = {},
                            navigationIcon = {
                                if (onBack != null) {
                                    FilledTonalIconButton(
                                        onClick = {
                                            onBack()
                                        },
                                        modifier = Modifier.padding(start = 15.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Outlined.ArrowBack,
                                            contentDescription = stringResource(R.string.back)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent
                            ),
                            scrollBehavior = scrollBehavior
                        )

                        val scale = 1.8181818f + (1f - 1.8181818f) * collapsedFraction
                        val startPadding = lerp(24.dp, 80.dp, collapsedFraction)
                        val bottomPadding = lerp(0.dp, 18.dp, collapsedFraction)

                        val configuration = LocalConfiguration.current
                        val screenWidth = configuration.screenWidthDp.dp
                        val maxTitleWidth = (screenWidth - startPadding - 16.dp) / scale

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(start = startPadding, bottom = bottomPadding),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            ProvideTextStyle(value = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium
                            )) {
                                Box(modifier = Modifier
                                    .widthIn(max = maxTitleWidth)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        transformOrigin = TransformOrigin(0f, 1f)
                                    }) {
                                    title()
                                }
                            }
                        }
                    }
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