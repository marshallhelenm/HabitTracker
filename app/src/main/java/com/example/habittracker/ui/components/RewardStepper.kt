package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.habittracker.ui.formatCoins
import com.example.habittracker.viewmodel.MAX_REWARD_RATE
import com.example.habittracker.viewmodel.MIN_REWARD_RATE
import com.example.habittracker.viewmodel.REWARD_STEP
import java.math.BigDecimal

// Sets a habit's reward rate in quarter steps. Buttons rather than a text field, so the value is
// always a valid amount.
@Composable
fun RewardStepper(
    rate: BigDecimal,
    onRateChange: (BigDecimal) -> Unit,
    fixed: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (fixed) "Per check-in" else "Reward × streak",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        FilledTonalIconButton(
            onClick = { onRateChange(rate - REWARD_STEP) },
            enabled = rate > MIN_REWARD_RATE
        ) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = "Lower reward")
        }
        Text(
            text = formatCoins(rate),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 64.dp)
        )
        FilledTonalIconButton(
            onClick = { onRateChange(rate + REWARD_STEP) },
            enabled = rate < MAX_REWARD_RATE
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Raise reward")
        }
    }
}
