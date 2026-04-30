package com.example.savingswidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import com.example.savingswidget.data.GoalRepository
import com.example.savingswidget.data.model.Goal
import com.example.savingswidget.ui.theme.SavingsWidgetTheme
import com.example.savingswidget.ui.widget.SavingsWidget
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var repository: GoalRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = GoalRepository(this)
        enableEdgeToEdge()
        setContent {
            SavingsWidgetTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Goal Settings") }
                        )
                    }
                ) { innerPadding ->
                    GoalEditScreen(
                        repository = repository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GoalEditScreen(repository: GoalRepository, modifier: Modifier = Modifier) {
    val goal by repository.goalFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (goal == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        var name by remember(goal) { mutableStateOf(goal?.name ?: "") }
        var emoji by remember(goal) { mutableStateOf(goal?.emoji ?: "") }
        var targetAmount by remember(goal) { mutableStateOf(goal?.targetAmount?.toString() ?: "") }
        var savedAmount by remember(goal) { mutableStateOf(goal?.savedAmount?.toString() ?: "") }
        var currency by remember(goal) { mutableStateOf(goal?.currency ?: "$") }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Goal Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = emoji,
                onValueChange = { emoji = it },
                label = { Text("Emoji (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Currency Symbol") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = { Text("Target Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = savedAmount,
                onValueChange = { savedAmount = it },
                label = { Text("Saved Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val updatedGoal = Goal(
                        name = name,
                        emoji = emoji,
                        targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                        savedAmount = savedAmount.toDoubleOrNull() ?: 0.0,
                        currency = currency
                    )
                    scope.launch {
                        repository.updateGoal(updatedGoal)
                        SavingsWidget().updateAll(repository.context)
                        snackbarHostState.showSnackbar("Goal updated!")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save")
            }

            SnackbarHost(hostState = snackbarHostState)
        }
    }
}
