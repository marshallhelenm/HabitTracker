package com.example.habittracker.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.habittracker.R
import com.example.habittracker.model.SavingsGoalStatus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habittracker.ui.components.HabitCard
import com.example.habittracker.viewmodel.HabitViewModel
import androidx.compose.foundation.layout.Row
import com.example.habittracker.viewmodel.SavingsGoalViewModel
import com.example.habittracker.ui.completeHabit
import com.example.habittracker.ui.components.PiggyBankCard

@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel,
    savingsGoalViewModel: SavingsGoalViewModel,
    onCreateGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habits = habitViewModel.habits.collectAsStateWithLifecycle().value
    val trackedHabits = habits.filter { it.tracking }
    val allGoals by savingsGoalViewModel.savingsGoals.collectAsStateWithLifecycle()
    val carouselGoals by savingsGoalViewModel.carouselGoals.collectAsStateWithLifecycle()
    val goalsLoaded by savingsGoalViewModel.loaded.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { carouselGoals.size })
    val currentGoal = carouselGoals.getOrNull(pagerState.currentPage)
    val currentGoalIsActive = currentGoal?.status == SavingsGoalStatus.ACTIVE
    val pinnedGoal = allGoals.firstOrNull { it.pinned }
    val activeGoalIds = allGoals.filter { it.status == SavingsGoalStatus.ACTIVE }.map { it.id }.toSet()
    val visibleHabits = when {
        currentGoal == null -> trackedHabits
        // A completed goal no longer earns; its habits now show on the pinned goal's page.
        !currentGoalIsActive -> emptyList()
        else -> habitsForGoalPage(trackedHabits, currentGoal, activeGoalIds)
    }
    var showQuickAddDialog by rememberSaveable { mutableStateOf(false) }

    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Quick add habit")
            }
        }
    ) { innerPadding ->
        // CODE TO BE DELETED
        // Row(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         .padding(innerPadding)
        //         .padding(16.dp),
        //     horizontalArrangement = Arrangement.spacedBy(12.dp)
        // ) {
        //     GoalProgressBar(percentFilled = savingsGoalViewModel.percentCompleted(savingsGoalViewModel.getCurrentSavingsGoal()), modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!goalsLoaded) {
                // Nothing to show yet; avoids flashing the empty-bank card while goals load.
            } else if (carouselGoals.isEmpty()) {
                NoGoalsCard(onCreateGoal = onCreateGoal)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 16.dp,
                        key = { page -> carouselGoals[page].id }
                    ) { page ->
                        val goal = carouselGoals[page]
                        PiggyBankCard(
                            goal = goal,
                            percentFilled = savingsGoalViewModel.percentCompleted(goal)
                        )
                    }
                    if (carouselGoals.size > 1) {
                        PagerDots(
                            count = carouselGoals.size,
                            selected = pagerState.currentPage,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Today's habits",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (visibleHabits.isEmpty()) {
                    Text(
                        text = if (currentGoal == null) {
                            "No tracked habits yet. Tap + to add a daily habit."
                        } else if (!currentGoalIsActive) {
                            "🎉 ${currentGoal.name} is complete!" +
                                (pinnedGoal?.let { " Its habits now save toward ${it.name}." } ?: "")
                        } else {
                            "No habits are saving toward ${currentGoal.name} yet. Tap + to add one."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    return@Column
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(visibleHabits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onCompleteClick = { completeHabit(habitViewModel, savingsGoalViewModel, habit) },
                            // With a single goal every habit pays into it, so the tag would only add noise.
                            goalLabel = when {
                                habit.goalId == currentGoal?.id || allGoals.size <= 1 -> null
                                habit.goalId == null -> "Unassigned"
                                // Linked to a goal that's finished (or gone); it pays in here now.
                                else -> allGoals.firstOrNull { it.id == habit.goalId }?.let { "${it.name} ✓" } ?: "Unassigned"
                            }
                        )
                    }
                }
            }
        }

    }

    if (showQuickAddDialog) {
        QuickAddHabitDialog(
            goalName = if (currentGoalIsActive) currentGoal?.name else pinnedGoal?.name,
            onDismiss = { showQuickAddDialog = false },
            onConfirm = { name ->
                if (habitViewModel.quickAddDailyHabit(name, goalId = currentGoal?.id?.takeIf { currentGoalIsActive })) {
                    showQuickAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun QuickAddHabitDialog(
    goalName: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick add habit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (goalName != null) {
                    Text(
                        text = "Saves toward $goalName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NoGoalsCard(onCreateGoal: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.piggy),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Text(text = "Your piggy bank is empty", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onCreateGoal) {
                Text("Create your first savings goal")
            }
        }
    }
}

@Composable
private fun PagerDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .size(if (isSelected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
            )
        }
    }
}
