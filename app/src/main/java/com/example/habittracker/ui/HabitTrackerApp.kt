package com.example.habittracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.habittracker.ui.components.AppTopBar
import com.example.habittracker.ui.goals.EditSavingsGoalScreen
import com.example.habittracker.ui.goals.SavingsGoalsScreen
import com.example.habittracker.ui.habits.HabitsScreen
import com.example.habittracker.ui.home.HomeScreen
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
