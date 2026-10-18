package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal

@Composable
fun EditHabitDialog(
    habit: Habit,
    goals: List<SavingsGoal>,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit,
    onDelete: (Long) -> Unit
) {
    var title by rememberSaveable(habit.id) { mutableStateOf(habit.title) }
    var description by rememberSaveable(habit.id) { mutableStateOf(habit.description) }
    var tracking by rememberSaveable(habit.id) { mutableStateOf(habit.tracking) }
    var customFrequency by rememberSaveable(habit.id) { mutableStateOf(habit.intervalDays > 1) }
    var intervalText by rememberSaveable(habit.id) {
        mutableStateOf(if (habit.intervalDays > 1) habit.intervalDays.toString() else "")
    }
    val intervalDays = intervalDaysFrom(customFrequency, intervalText)
    var goalId by rememberSaveable(habit.id) { mutableStateOf(habit.goalId) }
    var rewardRate by rememberSaveable(habit.id) { mutableStateOf(habit.rewardRate) }
    var fixedReward by rememberSaveable(habit.id) { mutableStateOf(habit.fixedReward) }
    var showStreak by rememberSaveable(habit.id) { mutableStateOf(habit.showStreak) }
    val streakShown = showStreak || !fixedReward

    AlertDialog(
        onDismissRequest = onDismiss,
        // The platform default leaves wide side margins on phones; use most of the width instead.
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        title = { Text("Edit habit") },
        text = {
            // Scrolls so the fields never outgrow the dialog on short screens or with large text,
            // which otherwise squashes the last rows on top of each other.
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                RewardTypePicker(
                    fixed = fixedReward,
                    onFixedChange = { fixedReward = it },
                    showStreak = showStreak,
                    onShowStreakChange = { showStreak = it }
                )

                RewardStepper(rate = rewardRate, onRateChange = { rewardRate = it }, fixed = fixedReward)

                // Hidden without a streak; the habit keeps its interval for if the streak comes back.
                if (streakShown) {
                    FrequencyPicker(
                        custom = customFrequency,
                        onCustomChange = { customFrequency = it },
                        daysText = intervalText,
                        onDaysTextChange = { intervalText = it }
                    )
                }

                GoalPicker(
                    goals = goals,
                    selectedGoalId = goalId,
                    onSelect = { goalId = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tracking")
                    Switch(
                        checked = tracking,
                        onCheckedChange = { tracking = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && (!streakShown || intervalDays != null),
                onClick = {
                    onSave(
                        habit.copy(
                            title = title,
                            description = description,
                            tracking = tracking,
                            intervalDays = intervalDays ?: habit.intervalDays,
                            goalId = goalId,
                            rewardRate = rewardRate,
                            fixedReward = fixedReward,
                            showStreak = streakShown
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onDelete(habit.id) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}