package com.example.habittracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.habittracker.ui.formatCoins
import com.example.habittracker.viewmodel.MAX_REWARD_RATE
import com.example.habittracker.viewmodel.MIN_REWARD_RATE
import com.example.habittracker.viewmodel.REWARD_STEP
import com.example.habittracker.viewmodel.rewardRateFrom
import com.example.habittracker.viewmodel.rewardRateText

// Sets a habit's reward rate: the buttons move it by REWARD_STEP, and tapping the amount lets you
// type any amount instead. Like FrequencyPicker, the amount is kept as text so it can be empty or
// mid-edit; use rewardRateFrom to read it.
@Composable
fun RewardStepper(
    rateText: String,
    onRateTextChange: (String) -> Unit,
    fixed: Boolean = false,
    modifier: Modifier = Modifier
) {
    val rate = rewardRateFrom(rateText)
    var editing by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (fixed) "Per check-in" else "Reward × streak",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            FilledTonalIconButton(
                onClick = { rate?.let { onRateTextChange(rewardRateText((it - REWARD_STEP).max(MIN_REWARD_RATE))) } },
                enabled = rate != null && rate > MIN_REWARD_RATE
            ) {
                Icon(imageVector = Icons.Filled.Remove, contentDescription = "Lower reward")
            }
            if (editing) {
                // onFocusChanged reports "not focused" once before the field takes focus; only a
                // later loss of focus (tapping away, Done) should end editing.
                var hadFocus by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier.width(72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BasicTextField(
                        value = rateText,
                        onValueChange = { text -> onRateTextChange(text.filter { it.isDigit() || it == '.' || it == ',' }.take(6)) },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            color = if (rate == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { if (it.isFocused) hadFocus = true else if (hadFocus) editing = false }
                    )
                    HorizontalDivider(
                        color = if (rate == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            } else {
                Text(
                    text = rate?.let { formatCoins(it) } ?: rateText.ifBlank { "—" },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (rate == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(min = 72.dp)
                        .clickable(onClickLabel = "Type a reward", role = Role.Button) { editing = true }
                )
            }
            FilledTonalIconButton(
                onClick = { rate?.let { onRateTextChange(rewardRateText((it + REWARD_STEP).min(MAX_REWARD_RATE))) } },
                enabled = rate != null && rate < MAX_REWARD_RATE
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Raise reward")
            }
        }
        if (rate == null) {
            Text(
                text = "Enter ${rewardRateText(MIN_REWARD_RATE)}–${rewardRateText(MAX_REWARD_RATE)} coins",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
