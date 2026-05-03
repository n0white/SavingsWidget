package com.n0white.n0widgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.n0white.n0widgets.data.GoalRepository
import com.n0white.n0widgets.ui.GoalEditScreen
import com.n0white.n0widgets.ui.theme.SavingsWidgetTheme

class SavingsActivity : ComponentActivity() {
    private lateinit var goalRepository: GoalRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goalRepository = GoalRepository(this)
        enableEdgeToEdge()

        setContent {
            SavingsWidgetTheme {
                val goal by goalRepository.goalFlow.collectAsState(initial = null)
                
                ScreenScaffold(
                    title = { Text(stringResource(R.string.savings_settings_title), fontWeight = FontWeight.Bold) },
                    onBack = { finish() },
                    isMain = false
                ) {
                    GoalEditScreen(
                        repository = goalRepository,
                        goal = goal
                    )
                }
            }
        }
    }
}