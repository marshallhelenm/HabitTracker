package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.model.SavingsGoalStatus

@Composable
fun GoalPicker(
    goals: List<SavingsGoal>,
    selectedGoalId: Long?,
    onSelect: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "Saves toward", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val selectionExists = selectedGoalId == null || goals.any { it.id == selectedGoalId }
            val followsPin = selectedGoalId == null || !selectionExists
            FilterChip(
                selected = followsPin,
                onClick = { onSelect(null) },
                colors = piggyFilterChipColors(),
                border = piggyFilterChipBorder(followsPin),
                // A rule, not a goal: the habit follows the pin if it moves to another goal.
                label = { Text("Whichever is pinned") }
            )
            // Completed goals no longer take earnings, so they're only listed if already selected.
            goals.filter { it.status == SavingsGoalStatus.ACTIVE || it.id == selectedGoalId }.forEach { goal ->
                val selected = goal.id == selectedGoalId
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(goal.id) },
                    colors = piggyFilterChipColors(),
                    border = piggyFilterChipBorder(selected),
                    label = { Text(if (goal.status == SavingsGoalStatus.ACTIVE) goal.name else "${goal.name} ✓") },
                    leadingIcon = if (goal.pinned) {
                        {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}
