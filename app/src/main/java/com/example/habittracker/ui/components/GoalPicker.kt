package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habittracker.model.SavingsGoal

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
            FilterChip(
                selected = selectedGoalId == null || !selectionExists,
                onClick = { onSelect(null) },
                label = { Text("Pinned goal") }
            )
            goals.forEach { goal ->
                FilterChip(
                    selected = goal.id == selectedGoalId,
                    onClick = { onSelect(goal.id) },
                    label = { Text(goal.name) }
                )
            }
        }
    }
}
