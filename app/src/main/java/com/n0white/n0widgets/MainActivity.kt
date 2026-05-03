package com.n0white.n0widgets

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.n0white.n0widgets.data.CounterRepository
import com.n0white.n0widgets.data.GoalRepository
import com.n0white.n0widgets.ui.CounterEditScreen
import com.n0white.n0widgets.ui.GoalEditScreen
import com.n0white.n0widgets.ui.MainScreen
import com.n0white.n0widgets.ui.theme.SavingsWidgetTheme

class MainActivity : ComponentActivity() {
    private lateinit var goalRepository: GoalRepository
    private lateinit var counterRepository: CounterRepository
    private val intentState = mutableStateOf<Intent?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentState.value = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goalRepository = GoalRepository(this)
        counterRepository = CounterRepository(this)
        com.n0white.n0widgets.ui.widget.MidnightUpdater.schedule(this)
        enableEdgeToEdge()

        intentState.value = intent

        setContent {
            SavingsWidgetTheme {
                MainApp(goalRepository, counterRepository, intentState.value)
            }
        }
    }
}

@Composable
fun MainApp(
    goalRepository: GoalRepository,
    counterRepository: CounterRepository,
    intent: Intent?
) {
    val navController = rememberNavController()

    LaunchedEffect(intent) {
        intent?.getStringExtra("screen")?.let { screen ->
            when (screen) {
                "savings" -> navController.navigate("savings") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }
                "counter" -> navController.navigate("counter") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }

    val slideDuration = 300

    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(slideDuration)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(slideDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(slideDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(slideDuration)
            )
        }
    ) {
        composable("main") {
            ScreenScaffold(
                title = "n0widgets",
                onBack = null,
                isMain = true
            ) {
                MainScreen(
                    onNavigateToSavings = { navController.navigate("savings") },
                    onNavigateToCounter = { navController.navigate("counter") }
                )
            }
        }
        composable("savings") {
            val goal by goalRepository.goalFlow.collectAsState(initial = null)
            ScreenScaffold(
                title = "Savings Settings",
                onBack = { navController.popBackStack() },
                isMain = false
            ) {
                GoalEditScreen(
                    repository = goalRepository,
                    goal = goal
                )
            }
        }
        composable("counter") {
            val counter by counterRepository.counterFlow.collectAsState(initial = null)
            ScreenScaffold(
                title = "Counter Settings",
                onBack = { navController.popBackStack() },
                isMain = false
            ) {
                CounterEditScreen(
                    repository = counterRepository,
                    counter = counter
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
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
        topBar = {
            if (isMain) {
                TopAppBar(
                    title = { Text(title, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    scrollBehavior = scrollBehavior
                )
            } else {
                LargeTopAppBar(
                    title = { Text(title, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (onBack != null) {
                            FilledTonalIconButton(
                                onClick = onBack,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}