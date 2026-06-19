package com.example.habittracker.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habittracker.model.SavingsGoal
import java.math.BigDecimal

@Composable
fun EditSavingsGoalScreen(
    goal: SavingsGoal,
    onSave: (name: String, targetAmountText: String) -> Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable(goal.id) { mutableStateOf(goal.name) }
    var targetAmountText by rememberSaveable(goal.id) { mutableStateOf(goal.targetAmount.toPlainString()) }
    var inputError by rememberSaveable(goal.id) { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Edit Goal",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Goal name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = targetAmountText,
            onValueChange = { targetAmountText = it },
            label = { Text("Target amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (inputError != null) {
            Text(
                text = inputError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel / Back")
            }
            TextButton(
                onClick = {
                    val normalizedName = name.trim()
                    val parsedAmount = targetAmountText.trim().toBigDecimalOrNull()
                    if (normalizedName.isEmpty() || parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
                        inputError = "Provide a goal name and a target amount greater than zero."
                        return@TextButton
                    }

                    val saved = onSave(name, targetAmountText)
                    if (!saved) {
                        inputError = "Unable to save changes. Please try again."
                    } else {
                        inputError = null
                    }
                }
            ) {
                Text("Save Changes")
            }
        }
    }
}
