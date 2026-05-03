package com.n0white.n0widgets

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
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
                fontWeight = FontWeight.Bold
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
        topBar = {
            if (isMain) {
                TopAppBar(
                    title = title,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    scrollBehavior = scrollBehavior
                )
            } else {
                LargeTopAppBar(
                    title = title,
                    navigationIcon = {
                        if (onBack != null) {
                            FilledTonalIconButton(
                                onClick = onBack,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                // Explicit import-less icon usage if needed, but we have the import
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
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