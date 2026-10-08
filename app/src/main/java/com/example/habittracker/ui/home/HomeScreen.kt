package com.example.habittracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habittracker.ui.components.HabitCard
import com.example.habittracker.viewmodel.HabitViewModel
import androidx.compose.foundation.layout.Row
import com.example.habittracker.viewmodel.SavingsGoalViewModel
import com.example.habittracker.ui.completeHabit
import com.example.habittracker.ui.components.PiggyBankCard

@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel,
    savingsGoalViewModel: SavingsGoalViewModel,
    modifier: Modifier = Modifier
) {
    val habits = habitViewModel.habits.collectAsStateWithLifecycle().value
    val trackedHabits = habits.filter { it.tracking }
    var showQuickAddDialog by rememberSaveable { mutableStateOf(false) }

    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Quick add habit")
            }
        }
    ) { innerPadding ->
        // CODE TO BE DELETED
        // Row(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         .padding(innerPadding)
        //         .padding(16.dp),
        //     horizontalArrangement = Arrangement.spacedBy(12.dp)
        // ) {
        //     GoalProgressBar(percentFilled = savingsGoalViewModel.percentCompleted(savingsGoalViewModel.getCurrentSavingsGoal()), modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val pinnedGoal by savingsGoalViewModel.pinnedGoal.collectAsStateWithLifecycle()
            PiggyBankCard(
                goal = pinnedGoal,
                percentFilled = savingsGoalViewModel.percentCompleted(pinnedGoal)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Today's habits",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (trackedHabits.isEmpty()) {
                    Text(
                        text = "No tracked habits yet. Tap + to add a daily habit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    return@Column
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(trackedHabits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onCompleteClick = { completeHabit(habitViewModel, savingsGoalViewModel, habit) }
                        )
                    }
                }
            }
        }

    }

    if (showQuickAddDialog) {
        QuickAddHabitDialog(
            onDismiss = { showQuickAddDialog = false },
            onConfirm = { name ->
                if (habitViewModel.quickAddDailyHabit(name)) {
                    showQuickAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun QuickAddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick add habit") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
