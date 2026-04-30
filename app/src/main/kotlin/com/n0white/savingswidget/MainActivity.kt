package com.n0white.savingswidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import com.n0white.savingswidget.data.GoalRepository
import com.n0white.savingswidget.data.model.Goal
import com.n0white.savingswidget.ui.theme.SavingsWidgetTheme
import com.n0white.savingswidget.ui.widget.SavingsWidget
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
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        LargeTopAppBar(
                            title = {
                                Text(
                                    stringResource(R.string.title_my_savings),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
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
    val saveSuccessMsg = stringResource(R.string.msg_save_success)

    if (goal == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        var name by remember(goal) { mutableStateOf(goal?.name ?: "") }
        var emoji by remember(goal) { mutableStateOf(goal?.emoji ?: "") }
        var targetAmount by remember(goal) { mutableStateOf(goal?.targetAmount?.toString() ?: "") }
        var savedAmount by remember(goal) { mutableStateOf(goal?.savedAmount?.toString() ?: "") }
        var currency by remember(goal) { mutableStateOf(goal?.currency ?: stringResource(R.string.default_currency)) }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                stringResource(R.string.edit_screen_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.label_goal_name)) },
                    placeholder = { Text(stringResource(R.string.placeholder_goal_name)) },
                    leadingIcon = { Icon(Icons.Default.Savings, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text(stringResource(R.string.label_emoji)) },
                        placeholder = { Text(stringResource(R.string.placeholder_emoji)) },
                        leadingIcon = { Icon(Icons.Default.EmojiEmotions, null) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text(stringResource(R.string.label_currency)) },
                        placeholder = { Text(stringResource(R.string.placeholder_currency)) },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text(stringResource(R.string.label_target_amount)) },
                    leadingIcon = { Icon(Icons.Default.TrackChanges, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )

                OutlinedTextField(
                    value = savedAmount,
                    onValueChange = { savedAmount = it },
                    label = { Text(stringResource(R.string.label_saved_amount)) },
                    leadingIcon = { Icon(Icons.Default.AddCard, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )
            }

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
                        snackbarHostState.showSnackbar(saveSuccessMsg)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.btn_save_changes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SnackbarHost(hostState = snackbarHostState)
        }
    }
}
