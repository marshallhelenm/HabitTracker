package com.example.habittracker.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.ui.formatMoney
import com.example.habittracker.viewmodel.parseBalance
import java.math.BigDecimal

@Composable
fun EditSavingsGoalScreen(
    goal: SavingsGoal,
    // balanceText is null unless the balance was changed under More options.
    onSave: (name: String, targetAmountText: String, earningsCapText: String, balanceText: String?) -> Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable(goal.id) { mutableStateOf(goal.name) }
    var targetAmountText by rememberSaveable(goal.id) { mutableStateOf(goal.targetAmount.toPlainString()) }
    var earningsCapText by rememberSaveable(goal.id) { mutableStateOf(goal.dailyEarningsCap?.toPlainString() ?: "") }
    var balanceText by rememberSaveable(goal.id) { mutableStateOf(goal.currentAmount.toPlainString()) }
    var inputError by rememberSaveable(goal.id) { mutableStateOf<String?>(null) }
    // Balance editing is tucked away and starts closed every time, so it isn't changed by accident.
    var moreOptionsOpen by remember(goal.id) { mutableStateOf(false) }
    var pendingBalance by remember(goal.id) { mutableStateOf<BigDecimal?>(null) }

    fun save(newBalanceText: String?) {
        if (onSave(name, targetAmountText, earningsCapText, newBalanceText)) {
            inputError = null
        } else {
            inputError = "Unable to save changes. Please try again."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
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


                OutlinedTextField(
                    value = earningsCapText,
                    onValueChange = { earningsCapText = it },
                    label = { Text("Daily Earnings Cap (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TextButton(onClick = { moreOptionsOpen = !moreOptionsOpen }) {
                    Text("More options")
                    Icon(
                        imageVector = if (moreOptionsOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }

                if (moreOptionsOpen) {
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("Current balance") },
                        supportingText = {
                            Text("Changes the saved amount directly. It isn't counted as earnings or toward the daily cap.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (inputError != null) {
                    Text(
                        text = inputError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    OutlinedButton(onClick = onCancel) {
                        Text("Cancel / Back")
                    }
                    Button(
                        onClick = {
                            val normalizedName = name.trim()
                            val parsedAmount = targetAmountText.trim().toBigDecimalOrNull()
                            val trimmedEarningsCap = earningsCapText.trim()
                            val parsedEarningsCap = trimmedEarningsCap.toBigDecimalOrNull()
                            val earningsCapValid = trimmedEarningsCap.isEmpty() ||
                                (parsedEarningsCap != null && parsedEarningsCap > BigDecimal.ZERO)
                            if (normalizedName.isEmpty() || parsedAmount == null || parsedAmount <= BigDecimal.ZERO || !earningsCapValid) {
                                inputError = "Provide a goal name and a target amount greater than zero. " +
                                    "If set, the daily earnings cap must be greater than zero."
                                return@Button
                            }
                            val newBalance = parseBalance(balanceText)
                            if (newBalance == null) {
                                inputError = "The balance must be zero or more, in dollars and cents."
                                return@Button
                            }

                            if (newBalance.compareTo(goal.currentAmount) != 0) {
                                pendingBalance = newBalance
                            } else {
                                save(newBalanceText = null)
                            }
                        }
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    pendingBalance?.let { newBalance ->
        AlertDialog(
            onDismissRequest = { pendingBalance = null },
            title = { Text("Change the balance?") },
            text = {
                Text(
                    "Change ${goal.name}'s balance from ${formatMoney(goal.currentAmount)} to ${formatMoney(newBalance)}?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingBalance = null
                    save(newBalanceText = newBalance.toPlainString())
                }) {
                    Text("Change balance")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingBalance = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
