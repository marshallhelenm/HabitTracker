package com.example.habittracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.consumeWindowInsets
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
import com.example.habittracker.ui.components.AppBottomBar
import com.example.habittracker.ui.components.AppDestination
import com.example.habittracker.ui.components.AppTopBar
import com.example.habittracker.ui.goals.EditSavingsGoalScreen
import com.example.habittracker.ui.goals.SavingsGoalsScreen
import com.example.habittracker.ui.habits.HabitsScreen
import com.example.habittracker.ui.home.HomeScreen
import com.example.habittracker.ui.settings.SettingsScreen
import com.example.habittracker.data.AppSettings
import com.example.habittracker.viewmodel.EarningsNotice
import com.example.habittracker.viewmodel.HabitViewModel
import com.example.habittracker.viewmodel.SavingsGoalViewModel

private enum class AppScreen {
    Home,
    Habits,
    Goals,
    GoalEdit,
    Settings
}

@Composable
fun HabitTrackerApp(
    habitViewModel: HabitViewModel,
    savingsGoalViewModel: SavingsGoalViewModel,
    appSettings: AppSettings,
    modifier: Modifier = Modifier
) {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Home) }
    var editingGoalId by rememberSaveable { mutableStateOf<Long?>(null) }
    val habits by habitViewModel.habits.collectAsStateWithLifecycle()
    val notices by savingsGoalViewModel.notices.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cap messages are transient; each shows once as a snackbar and is then dropped.
    val capNotice = notices.filterIsInstance<EarningsNotice.DailyCapReached>().firstOrNull()
    LaunchedEffect(capNotice) {
        if (capNotice != null) {
            snackbarHostState.showSnackbar("${capNotice.goalName} hit today's ${formatCoins(capNotice.cap)} limit")
            savingsGoalViewModel.dismissNotice(capNotice)
        }
    }

    notices.filterIsInstance<EarningsNotice.GoalCompleted>().firstOrNull()?.let { notice ->
        GoalCompletedDialog(notice = notice, onDismiss = { savingsGoalViewModel.dismissNotice(notice) })
    }

    val screenTitle = when (currentScreen) {
        AppScreen.Home -> "Habit Piggy"
        AppScreen.Habits -> "Habits"
        AppScreen.Goals -> "Goals"
        AppScreen.GoalEdit -> "Edit Goal"
        AppScreen.Settings -> "Settings"
    }

    // The bottom bar item to highlight; Edit Goal is a sub-screen of Goals, Settings of Home.
    val destination = when (currentScreen) {
        AppScreen.Home, AppScreen.Settings -> AppDestination.Home
        AppScreen.Habits -> AppDestination.Habits
        AppScreen.Goals, AppScreen.GoalEdit -> AppDestination.Goals
    }

    fun goBack() {
        currentScreen = when (currentScreen) {
            AppScreen.GoalEdit -> {
                editingGoalId = null
                AppScreen.Goals
            }
            else -> AppScreen.Home
        }
    }

    val isSubScreen = currentScreen == AppScreen.GoalEdit || currentScreen == AppScreen.Settings

    // Home is the root, so back there falls through to the system and closes the app.
    BackHandler(enabled = currentScreen != AppScreen.Home) { goBack() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = screenTitle,
                onBack = if (isSubScreen) ::goBack else null,
                onSettings = if (isSubScreen) null else { { currentScreen = AppScreen.Settings } }
            )
        },
        bottomBar = {
            AppBottomBar(
                current = destination,
                // Leaving Edit Goal from the bar discards unsaved changes, like Cancel.
                onSelect = { selected ->
                    editingGoalId = null
                    currentScreen = when (selected) {
                        AppDestination.Home -> AppScreen.Home
                        AppDestination.Habits -> AppScreen.Habits
                        AppDestination.Goals -> AppScreen.Goals
                    }
                }
            )
        }
    ) { innerPadding ->
        // Screens with their own Scaffold (Home's + button) would otherwise add the system bar
        // insets again on top of this padding.
        val screenModifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        when (currentScreen) {
            AppScreen.Home -> {
                HomeScreen(
                    habitViewModel = habitViewModel,
                    savingsGoalViewModel = savingsGoalViewModel,
                    appSettings = appSettings,
                    onCreateGoal = { currentScreen = AppScreen.Goals },
                    modifier = screenModifier
                )
            }

            AppScreen.Habits -> {
                HabitsScreen(
                    viewModel = habitViewModel,
                    savingsGoalViewModel = savingsGoalViewModel,
                    modifier = screenModifier
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
                    modifier = screenModifier
                )
            }

            AppScreen.Settings -> {
                SettingsScreen(appSettings = appSettings, modifier = screenModifier)
            }

            AppScreen.GoalEdit -> {
                val goalId = editingGoalId
                val goal = goalId?.let { savingsGoalViewModel.getGoalById(it) }
                if (goal == null) {
                    currentScreen = AppScreen.Goals
                } else {
                    EditSavingsGoalScreen(
                        goal = goal,
                        onSave = { name, targetAmountText, earningsCapText, balanceText ->
                            val updated = savingsGoalViewModel.updateSavingsGoal(
                                id = goal.id,
                                name = name,
                                targetAmountText = targetAmountText,
                                earningsCapText = earningsCapText,
                                balanceText = balanceText
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
                        modifier = screenModifier
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
                    "The extra ${formatCoins(notice.overflowAmount)} went to ${notice.overflowGoalName}."
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
