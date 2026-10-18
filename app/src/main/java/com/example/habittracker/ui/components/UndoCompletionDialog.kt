package com.example.habittracker.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.habittracker.model.Habit

// Un-checking takes coins back out of goals, so it's confirmed rather than a single stray tap.
@Composable
fun UndoCompletionDialog(
    habit: Habit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Un-check ${habit.title}?") },
        text = {
            Text("Today's check-in is undone: the streak goes back to where it was, and anything it earned today comes back out of your piggy bank.")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Un-check")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep it")
            }
        }
    )
}
