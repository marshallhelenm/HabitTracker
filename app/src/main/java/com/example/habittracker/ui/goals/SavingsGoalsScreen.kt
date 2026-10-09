package com.example.habittracker.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.model.SavingsGoalStatus
import com.example.habittracker.ui.components.ChunkyArrowDown
import com.example.habittracker.ui.components.ChunkyArrowUp
import com.example.habittracker.ui.components.GoalProgressBar
import com.example.habittracker.ui.components.piggyFilterChipBorder
import com.example.habittracker.ui.components.piggyFilterChipColors
import com.example.habittracker.ui.home.habitsForGoalPage
import com.example.habittracker.ui.formatMoney
import com.example.habittracker.viewmodel.SavingsGoalViewModel
import com.example.habittracker.viewmodel.nextPinnedGoal
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SavingsGoalsScreen(
    viewModel: SavingsGoalViewModel,
    habits: List<Habit>,
    onEditGoal: (Long) -> Unit,
    onGoalDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val goalIds = goals.filter { it.status == SavingsGoalStatus.ACTIVE }.map { it.id }.toSet()
    var goalPendingDelete by remember { mutableStateOf<SavingsGoal?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var targetAmount by rememberSaveable { mutableStateOf("") }
    var inputError by rememberSaveable { mutableStateOf<String?>(null) }
    // Collapsed by default so the goal list gets the screen, matching the Habits screen.
    var formOpen by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable { mutableStateOf(GoalSort.MY_ORDER) }
    var hideFinished by rememberSaveable { mutableStateOf(false) }
    val listedGoals = goalsForList(goals, sort, hideFinished)
    // In "My order", active goals other than the pinned one can be dragged among themselves; the pinned
    // goal always leads the carousel and finished goals aren't on it.
    val draggableIds = if (sort == GoalSort.MY_ORDER) {
        listedGoals.filter { it.status == SavingsGoalStatus.ACTIVE && !it.pinned }.map { it.id }.toSet()
    } else {
        emptySet()
    }
    // The list as shown, rearranged live while dragging. It's saved on drop, and resets to the stored
    // order whenever goals reload.
    var displayedGoals by remember(listedGoals) { mutableStateOf(listedGoals) }
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (from.key in draggableIds && to.key in draggableIds) {
            displayedGoals = displayedGoals.toMutableList().apply {
                add(indexOfFirst { it.id == to.key }, removeAt(indexOfFirst { it.id == from.key }))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!formOpen) {
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
                Text("New goal")
            }
        } else Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New savings goal",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        formOpen = false
                        inputError = null
                    }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close form")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
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

                Button(
                    onClick = {
                        val added = viewModel.addSavingsGoal(name = name, targetAmountText = targetAmount)
                        if (added) {
                            inputError = null
                            name = ""
                            targetAmount = ""
                            formOpen = false
                        } else {
                            inputError = "Provide a goal name and a target amount greater than zero."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Savings Goal")
                }
            }
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (goals.isNotEmpty()) {
                item(key = "list-controls") {
                    GoalListControls(
                        sort = sort,
                        onSortChange = { sort = it },
                        hideFinished = hideFinished,
                        onHideFinishedChange = { hideFinished = it }
                    )
                }
            }
            if (goals.isNotEmpty() && listedGoals.isEmpty()) {
                item(key = "all-hidden") {
                    Text(
                        text = "Every goal here is complete 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            items(displayedGoals, key = { it.id }) { goal ->
                ReorderableItem(reorderState, key = goal.id, enabled = goal.id in draggableIds) { isDragging ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (goal.id in draggableIds) {
                                    Icon(
                                        imageVector = Icons.Filled.DragIndicator,
                                        contentDescription = "Drag to reorder",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier
                                            .draggableHandle(onDragStopped = {
                                                viewModel.reorderGoals(displayedGoals.map { it.id }.filter { it in draggableIds })
                                            })
                                            .padding(end = 8.dp)
                                            .size(24.dp)
                                    )
                                }
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                if (goal.pinned) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(
                                            text = "Pinned",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                IconButton(onClick = { onEditGoal(goal.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit goal",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                // Only active goals can be pinned; completed ones no longer take earnings.
                                if (goal.status == SavingsGoalStatus.ACTIVE) IconButton(onClick = { viewModel.pinSavingsGoal(goal.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.PushPin,
                                        contentDescription = "Pin goal",
                                        tint = if (goal.pinned) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    if (habitsForGoalPage(habits, goal, goalIds).isEmpty()) {
                                        viewModel.deleteSavingsGoal(goal.id, onDeleted = onGoalDeleted)
                                    } else {
                                        goalPendingDelete = goal
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete goal",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GoalProgressBar(percentFilled = viewModel.percentCompleted(goal), height = 14.dp)
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "${formatMoney(goal.currentAmount)} of ${formatMoney(goal.targetAmount)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (goal.status == SavingsGoalStatus.ACTIVE) {
                                        val habitCount = habitsForGoalPage(habits, goal, goalIds).size
                                        Text(
                                            text = if (habitCount == 1) "1 habit · " else "$habitCount habits · ",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    Text(
                                        text = goal.status.name.lowercase().replaceFirstChar { it.uppercase() } +
                                            if (goal.status == SavingsGoalStatus.COMPLETED) " 🎉" else "",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    goalPendingDelete?.let { goal ->
        val affected = habitsForGoalPage(habits, goal, goalIds).size
        val nextPinned = nextPinnedGoal(goals, excluding = listOf(goal.id))
        val habitsPhrase = if (affected == 1) "1 habit" else "$affected habits"
        val destination = when {
            !goal.pinned -> "your pinned goal"
            nextPinned != null -> "${nextPinned.name}, which will become your pinned goal"
            else -> null
        }
        AlertDialog(
            onDismissRequest = { goalPendingDelete = null },
            title = { Text("Delete ${goal.name}?") },
            text = {
                Text(
                    if (destination != null) {
                        "$habitsPhrase will save toward $destination instead."
                    } else {
                        "$habitsPhrase won't earn anything until you add a new goal."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSavingsGoal(goal.id, onDeleted = onGoalDeleted)
                    goalPendingDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { goalPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GoalListControls(
    sort: GoalSort,
    onSortChange: (GoalSort) -> Unit,
    hideFinished: Boolean,
    onHideFinishedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your goals",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = hideFinished,
                onClick = { onHideFinishedChange(!hideFinished) },
                label = { Text("Hide completed") },
                colors = piggyFilterChipColors(),
                border = piggyFilterChipBorder(hideFinished)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Sort", style = MaterialTheme.typography.labelLarge)
            // Each chip covers a sort and its reverse (A–Z / Z–A, Progress ↓ / ↑):
            // tapping the selected chip again flips the direction.
            listOf(GoalSort.MY_ORDER, GoalSort.NAME, GoalSort.PROGRESS).forEach { chipSort ->
                val selected = sort == chipSort || sort == chipSort.reversed
                FilterChip(
                    selected = selected,
                    onClick = { onSortChange(if (selected) sort.reversed ?: sort else chipSort) },
                    label = { Text(if (selected) sort.label else chipSort.label) },
                    trailingIcon = sortDirectionIcon(if (selected) sort else chipSort),
                    colors = piggyFilterChipColors(),
                    border = piggyFilterChipBorder(selected)
                )
            }
        }
    }
}

private fun sortDirectionIcon(sort: GoalSort): (@Composable () -> Unit)? {
    val (icon, description) = when (sort) {
        GoalSort.PROGRESS -> ChunkyArrowDown to "Most filled first"
        GoalSort.PROGRESS_ASCENDING -> ChunkyArrowUp to "Least filled first"
        else -> return null
    }
    return {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(18.dp)
        )
    }
}
