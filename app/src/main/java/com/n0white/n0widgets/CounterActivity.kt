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
import com.n0white.n0widgets.data.CounterRepository
import com.n0white.n0widgets.ui.CounterEditScreen
import com.n0white.n0widgets.ui.theme.SavingsWidgetTheme

class CounterActivity : ComponentActivity() {
    private lateinit var counterRepository: CounterRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        counterRepository = CounterRepository(this)
        enableEdgeToEdge()

        setContent {
            SavingsWidgetTheme {
                val counter by counterRepository.counterFlow.collectAsState(initial = null)

                ScreenScaffold(
                    title = { Text(stringResource(R.string.counter_settings_title), fontWeight = FontWeight.Bold) },
                    onBack = { finish() },
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
}