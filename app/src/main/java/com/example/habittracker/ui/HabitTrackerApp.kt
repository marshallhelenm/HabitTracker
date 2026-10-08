package com.example.habittracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habittracker.ui.components.AppTopBar
import com.example.habittracker.ui.goals.EditSavingsGoalScreen
import com.example.habittracker.ui.goals.SavingsGoalsScreen
import com.example.habittracker.ui.habits.HabitsScreen
import com.example.habittracker.ui.home.HomeScreen
import com.example.habittracker.viewmodel.EarningsNotice
import com.example.habittracker.viewmodel.HabitViewModel
import com.example.habittracker.viewmodel.SavingsGoalViewModel

private enum class AppScreen {
    Home,
    Habits,
    Goals,
    GoalEdit
}

@Composable
fun HabitTrackerApp(
    habitViewModel: HabitViewModel,
    savingsGoalViewModel: SavingsGoalViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Home) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var editingGoalId by rememberSaveable { mutableStateOf<Long?>(null) }
    val habits by habitViewModel.habits.collectAsStateWithLifecycle()
    val notices by savingsGoalViewModel.notices.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cap messages are transient; each shows once as a snackbar and is then dropped.
    val capNotice = notices.filterIsInstance<EarningsNotice.DailyCapReached>().firstOrNull()
    LaunchedEffect(capNotice) {
        if (capNotice != null) {
            snackbarHostState.showSnackbar("${capNotice.goalName} hit today's ${formatMoney(capNotice.cap)} limit")
            savingsGoalViewModel.dismissNotice(capNotice)
        }
    }

    notices.filterIsInstance<EarningsNotice.GoalCompleted>().firstOrNull()?.let { notice ->
        GoalCompletedDialog(notice = notice, onDismiss = { savingsGoalViewModel.dismissNotice(notice) })
    }

    val screenTitle = when (currentScreen) {
        AppScreen.Home -> "Home"
        AppScreen.Habits -> "Habits"
        AppScreen.Goals -> "Goals"
        AppScreen.GoalEdit -> "Edit Goal"
    }

    // Home is the root, so back there falls through to the system and closes the app.
    BackHandler(enabled = currentScreen != AppScreen.Home) {
        currentScreen = when (currentScreen) {
            AppScreen.GoalEdit -> {
                editingGoalId = null
                AppScreen.Goals
            }
            else -> AppScreen.Home
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = screenTitle,
                selectedMenu = screenTitle,
                menuExpanded = menuExpanded,
                onPigClick = { currentScreen = AppScreen.Home },
                onMenuClick = { menuExpanded = true },
                onDismissMenu = { menuExpanded = false },
                onGoHome = { currentScreen = AppScreen.Home },
                onGoHabits = { currentScreen = AppScreen.Habits },
                onGoGoals = {
                    editingGoalId = null
                    currentScreen = AppScreen.Goals
                }
            )
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.Home -> {
                HomeScreen(
                    habitViewModel = habitViewModel,
                    savingsGoalViewModel = savingsGoalViewModel,
                    onCreateGoal = { currentScreen = AppScreen.Goals },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.Habits -> {
                HabitsScreen(
                    viewModel = habitViewModel,
                    savingsGoalViewModel = savingsGoalViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.Goals -> {
                SavingsGoalsScreen(
                    viewModel = savingsGoalViewModel,
                    habits = habits,
                    onGoalDeleted = { habitViewModel.refresh() },
                    onEditGoal = { goalId ->
                        editingGoalId = goalId
                        currentScreen = AppScreen.GoalEdit
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.GoalEdit -> {
                val goalId = editingGoalId
                val goal = goalId?.let { savingsGoalViewModel.getGoalById(it) }
                if (goal == null) {
                    currentScreen = AppScreen.Goals
                } else {
                    EditSavingsGoalScreen(
                        goal = goal,
                        onSave = { name, targetAmountText, earningsCapText ->
                            val updated = savingsGoalViewModel.updateSavingsGoal(
                                id = goal.id,
                                name = name,
                                targetAmountText = targetAmountText,
                                earningsCapText = earningsCapText
                            )
                            if (updated) {
                                currentScreen = AppScreen.Goals
                                editingGoalId = null
                            }
                            updated
                        },
                        onCancel = {
                            currentScreen = AppScreen.Goals
                            editingGoalId = null
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCompletedDialog(notice: EarningsNotice.GoalCompleted, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎉 You filled ${notice.goalName}!") },
        text = {
            Text(
                if (notice.overflowGoalName != null) {
                    "The extra ${formatMoney(notice.overflowAmount)} went to ${notice.overflowGoalName}."
                } else {
                    "Its habits will now save toward your pinned goal."
                }
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Oink!")
            }
        }
    )
}
