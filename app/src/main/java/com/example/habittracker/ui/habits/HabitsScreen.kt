package com.example.habittracker.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.model.SavingsGoalStatus
import com.example.habittracker.ui.components.EditHabitDialog
import com.example.habittracker.ui.components.HabitCard
import com.example.habittracker.ui.components.intervalDaysFrom
import com.example.habittracker.viewmodel.HabitViewModel
import com.example.habittracker.viewmodel.SavingsGoalViewModel
import com.example.habittracker.ui.completeHabit
import com.example.habittracker.ui.toggleHabitToday
import com.example.habittracker.ui.components.UndoCompletionDialog
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun HabitsScreen(
    viewModel: HabitViewModel,
    savingsGoalViewModel: SavingsGoalViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val goals by savingsGoalViewModel.savingsGoals.collectAsStateWithLifecycle()

    var selectedHabitForEdit by remember { mutableStateOf<Habit?>(null) }
    var habitPendingUndo by remember { mutableStateOf<Habit?>(null) }
    var formOpen by rememberSaveable { mutableStateOf(false) }
    var formTitle by rememberSaveable { mutableStateOf("") }
    var formDescription by rememberSaveable { mutableStateOf("") }
    var formCustomFrequency by rememberSaveable { mutableStateOf(false) }
    var formIntervalText by rememberSaveable { mutableStateOf("") }
    var formRewardRate by rememberSaveable { mutableStateOf(BigDecimal.ONE) }
    var formGoalId by rememberSaveable { mutableStateOf<Long?>(null) }

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
                customFrequency = formCustomFrequency,
                onCustomFrequencyChange = { formCustomFrequency = it },
                intervalText = formIntervalText,
                onIntervalTextChange = { formIntervalText = it },
                rewardRate = formRewardRate,
                onRewardRateChange = { formRewardRate = it },
                goals = goals,
                goalId = formGoalId,
                onGoalChange = { formGoalId = it },
                onSubmit = {
                    viewModel.addHabit(
                        title = formTitle,
                        description = formDescription,
                        intervalDays = intervalDaysFrom(formCustomFrequency, formIntervalText) ?: 1,
                        rewardRate = formRewardRate,
                        goalId = formGoalId
                    )
                    formTitle = ""
                    formDescription = ""
                    formGoalId = null
                    formCustomFrequency = false
                    formIntervalText = ""
                    formRewardRate = BigDecimal.ONE
                    formOpen = false
                },
                onCloseClick = { formOpen = false }
            )
        } else {
            Button(
                onClick = { formOpen = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("New habit")
            }
        }



        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(habits, key = { it.id }) { habit ->
                HabitCard(
                    habit = habit,
                    onCompleteClick = {
                        if (habit.lastCompletedOn == LocalDate.now()) {
                            habitPendingUndo = habit
                        } else {
                            toggleHabitToday(viewModel, savingsGoalViewModel, habit)
                        }
                    },
                    onEditClick = { selectedHabitForEdit = habit },
                    goalLabel = goals.firstOrNull { it.id == habit.goalId }
                        ?.let { if (it.status == SavingsGoalStatus.ACTIVE) it.name else "${it.name} ✓" }
                        ?: if (goals.size > 1) "Unassigned" else null,
                    footerActions = {
                        Text(
                            text = "Track",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = habit.tracking,
                            onCheckedChange = { isTracked ->
                                viewModel.updateHabit(habit.copy(tracking = isTracked))
                            },
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                )
            }
        }
    }

    habitPendingUndo?.let { habit ->
        UndoCompletionDialog(
            habit = habit,
            onConfirm = {
                toggleHabitToday(viewModel, savingsGoalViewModel, habit)
                habitPendingUndo = null
            },
            onDismiss = { habitPendingUndo = null }
        )
    }

    selectedHabitForEdit?.let { habit ->
        EditHabitDialog(
            habit = habit,
            goals = goals,
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
