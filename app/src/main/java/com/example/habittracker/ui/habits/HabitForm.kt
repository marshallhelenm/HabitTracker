package com.example.habittracker.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.ui.components.FrequencyPicker
import com.example.habittracker.ui.components.GoalPicker
import com.example.habittracker.ui.components.RewardStepper
import com.example.habittracker.ui.components.RewardTypePicker
import com.example.habittracker.ui.components.intervalDaysFrom
import com.example.habittracker.viewmodel.rewardRateFrom

@Composable
fun HabitForm(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    customFrequency: Boolean,
    onCustomFrequencyChange: (Boolean) -> Unit,
    intervalText: String,
    onIntervalTextChange: (String) -> Unit,
    rewardRateText: String,
    onRewardRateTextChange: (String) -> Unit,
    fixedReward: Boolean,
    onFixedRewardChange: (Boolean) -> Unit,
    showStreak: Boolean,
    onShowStreakChange: (Boolean) -> Unit,
    goals: List<SavingsGoal>,
    goalId: Long?,
    onGoalChange: (Long?) -> Unit,
    onSubmit: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streakShown = showStreak || !fixedReward
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Habit",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close form"
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Habit name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            RewardTypePicker(
                fixed = fixedReward,
                onFixedChange = onFixedRewardChange,
                showStreak = showStreak,
                onShowStreakChange = onShowStreakChange
            )

            RewardStepper(rateText = rewardRateText, onRateTextChange = onRewardRateTextChange, fixed = fixedReward)

            // Frequency only decides how long a streak survives, so it's moot without one.
            if (streakShown) {
                FrequencyPicker(
                    custom = customFrequency,
                    onCustomChange = onCustomFrequencyChange,
                    daysText = intervalText,
                    onDaysTextChange = onIntervalTextChange
                )
            }

            GoalPicker(
                goals = goals,
                selectedGoalId = goalId,
                onSelect = onGoalChange
            )

            Button(
                onClick = onSubmit,
                enabled = title.isNotBlank() && rewardRateFrom(rewardRateText) != null && (!streakShown || intervalDaysFrom(customFrequency, intervalText) != null),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Habit")
            }
        }
    }
}