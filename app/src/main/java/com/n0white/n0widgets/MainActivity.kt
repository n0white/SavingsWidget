package com.n0white.n0widgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.n0white.n0widgets.data.CounterRepository
import com.n0white.n0widgets.data.GoalRepository
import com.n0white.n0widgets.ui.CounterEditScreen
import com.n0white.n0widgets.ui.GoalEditScreen
import com.n0white.n0widgets.ui.MainScreen
import com.n0white.n0widgets.ui.theme.SavingsWidgetTheme

enum class Screen {
    MAIN,
    SAVINGS,
    COUNTER
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var goalRepository: GoalRepository
    private lateinit var counterRepository: CounterRepository
    private var setScreen: ((Screen) -> Unit)? = null

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        val screenExtra = intent.getStringExtra("screen")
        when (screenExtra) {
            "savings" -> setScreen?.invoke(Screen.SAVINGS)
            "counter" -> setScreen?.invoke(Screen.COUNTER)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        goalRepository = GoalRepository(this)
        counterRepository = CounterRepository(this)
        com.n0white.n0widgets.ui.widget.MidnightUpdater.schedule(this)
        enableEdgeToEdge()
        setContent {
            var currentScreen by remember { 
                val screenExtra = intent.getStringExtra("screen")
                val startScreen = when (screenExtra) {
                    "savings" -> Screen.SAVINGS
                    "counter" -> Screen.COUNTER
                    else -> Screen.MAIN
                }
                mutableStateOf(startScreen) 
            }
            
            setScreen = { currentScreen = it }

            SavingsWidgetTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    when (currentScreen) {
                                        Screen.MAIN -> "n0widgets"
                                        Screen.SAVINGS -> "Savings Settings"
                                        Screen.COUNTER -> "Counter Settings"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                if (currentScreen != Screen.MAIN) {
                                    IconButton(onClick = { currentScreen = Screen.MAIN }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                Screen.MAIN -> MainScreen(
                                    onNavigateToSavings = { currentScreen = Screen.SAVINGS },
                                    onNavigateToCounter = { currentScreen = Screen.COUNTER }
                                )
                                Screen.SAVINGS -> GoalEditScreen(
                                    repository = goalRepository
                                )
                                Screen.COUNTER -> CounterEditScreen(
                                    repository = counterRepository
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
