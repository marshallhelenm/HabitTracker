package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

const val MAX_INTERVAL_DAYS = 365

// The interval the picker's state describes, or null if the custom day count isn't valid yet.
fun intervalDaysFrom(custom: Boolean, daysText: String): Int? =
    if (!custom) 1 else daysText.trim().toIntOrNull()?.takeIf { it in 2..MAX_INTERVAL_DAYS }

// Daily / Custom chips; Custom reveals an "every N days" field. The day count is kept as text so the
// field can be empty or mid-edit; use intervalDaysFrom to read it.
@Composable
fun FrequencyPicker(
    custom: Boolean,
    onCustomChange: (Boolean) -> Unit,
    daysText: String,
    onDaysTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "Frequency", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(false to "Daily", true to "Custom").forEach { (isCustom, label) ->
                FilterChip(
                    selected = custom == isCustom,
                    onClick = { onCustomChange(isCustom) },
                    colors = piggyFilterChipColors(),
                    border = piggyFilterChipBorder(custom == isCustom),
                    label = { Text(label) }
                )
            }
        }
        if (custom) {
            val invalid = daysText.isNotBlank() && intervalDaysFrom(true, daysText) == null
            OutlinedTextField(
                value = daysText,
                onValueChange = { text -> onDaysTextChange(text.filter(Char::isDigit).take(3)) },
                label = { Text("At least once every…") },
                suffix = { Text("days") },
                isError = invalid,
                supportingText = if (invalid) {
                    { Text("Enter 2–$MAX_INTERVAL_DAYS days") }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
