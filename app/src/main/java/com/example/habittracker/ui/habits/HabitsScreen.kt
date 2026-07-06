package com.example.habittracker.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habittracker.model.Habit
import com.example.habittracker.model.HabitFrequency
import com.example.habittracker.ui.components.EditHabitDialog
import com.example.habittracker.ui.components.HabitCard
import com.example.habittracker.viewmodel.HabitViewModel

@Composable
fun HabitsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()

    var selectedHabitForEdit by remember { mutableStateOf<Habit?>(null) }
    var formOpen by rememberSaveable { mutableStateOf(false) }
    var formTitle by rememberSaveable { mutableStateOf("") }
    var formDescription by rememberSaveable { mutableStateOf("") }
    var formFrequency by rememberSaveable { mutableStateOf(HabitFrequency.DAILY) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (formOpen) {
            HabitForm(
                title = formTitle,
                onTitleChange = { formTitle = it },
                description = formDescription,
                onDescriptionChange = { formDescription = it },
                onSubmit = {
                    viewModel.addHabit(
                        title = formTitle,
                        description = formDescription
                    )
                    formTitle = ""
                    formDescription = ""
                    formFrequency = HabitFrequency.DAILY
                    formOpen = false
                },
                onCloseClick = { formOpen = false }
            )
        } else {
            IconButton(
                onClick = { formOpen = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Habit")
            }
        }



        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(habits, key = { it.id }) { habit ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(56.dp)
                    ) {
                        Text(
                            text = "Track",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Checkbox(
                            checked = habit.tracking,
                            onCheckedChange = { isTracked ->
                                viewModel.updateHabit(habit.copy(tracking = isTracked))
                            }
                        )
                    }

                    HabitCard(
                        habit = habit,
                        onCompleteClick = { viewModel.markHabitCompletedToday(habit) },
                        onEditClick = { selectedHabitForEdit = habit },
                        headerActions = {
                            TextButton(onClick = { viewModel.deleteHabit(habit.id) }) {
                                Text("Delete")
                            }
                        }
                    )
                }
            }
        }
    }

    selectedHabitForEdit?.let { habit ->
        EditHabitDialog(
            habit = habit,
            onDismiss = { selectedHabitForEdit = null },
            onSave = {
                viewModel.updateHabit(it)
                selectedHabitForEdit = null
            },
            onDelete = { id ->
                viewModel.deleteHabit(id)
                selectedHabitForEdit = null
            }
        )
    }
}
