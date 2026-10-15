package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// "Grows with streak" / "Fixed amount" chips. Fixed reveals a switch for showing the streak, since
// only a fixed reward makes sense without one.
@Composable
fun RewardTypePicker(
    fixed: Boolean,
    onFixedChange: (Boolean) -> Unit,
    showStreak: Boolean,
    onShowStreakChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "Reward", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(false to "Grows with streak", true to "Fixed amount").forEach { (isFixed, label) ->
                FilterChip(
                    selected = fixed == isFixed,
                    onClick = { onFixedChange(isFixed) },
                    colors = piggyFilterChipColors(),
                    border = piggyFilterChipBorder(fixed == isFixed),
                    label = { Text(label) }
                )
            }
        }
        if (fixed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Track streak")
                Switch(checked = showStreak, onCheckedChange = onShowStreakChange)
            }
        }
    }
}
