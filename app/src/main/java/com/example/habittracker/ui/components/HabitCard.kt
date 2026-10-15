package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import com.example.habittracker.model.Habit
import com.example.habittracker.ui.LocalToday
import com.example.habittracker.ui.formatMoney
import com.example.habittracker.viewmodel.dueBy
import com.example.habittracker.viewmodel.effectiveStreak
import com.example.habittracker.viewmodel.isCompletedOn
import com.example.habittracker.viewmodel.nextPayout

@Composable
fun HabitCard(
    habit: Habit,
    onCompleteClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    headerActions: (@Composable RowScope.() -> Unit)? = null,
    footerActions: (@Composable RowScope.() -> Unit)? = null,
    goalLabel: String? = null,
    // True on Home, where the card shows what the next check-in pays; elsewhere it shows the rate.
    showNextPayout: Boolean = false,
    modifier: Modifier = Modifier
) {
    val today = LocalToday.current
    val completedToday = isCompletedOn(habit, today)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (completedToday) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (onEditClick != null) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit habit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (headerActions != null) {
                    headerActions()
                }

                if (onCompleteClick != null) {
                    IconButton(onClick = onCompleteClick) {
                        Icon(
                            imageVector = if (completedToday) {
                                Icons.Filled.CheckCircle
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = if (completedToday) "Undo today’s completion" else "Mark complete for today",
                            tint = if (completedToday) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            if (habit.description.isNotBlank()) {
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val deadline = dueBy(habit, today)
            if (habit.showStreak && habit.intervalDays > 1 && deadline != null && !completedToday) {
                Text(
                    text = dueLabel(deadline, today),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Chips get whatever width the actions leave and wrap onto another line when they
                // don't fit, so trailing controls like the Track switch are never pushed off the card.
                FlowRow(
                    modifier = Modifier.weight(1f),
                    itemVerticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (habit.showStreak) {
                        StreakChip(streak = effectiveStreak(habit, today), daily = habit.intervalDays == 1)
                    }
                    RewardChip(
                        text = when {
                            !showNextPayout && habit.fixedReward -> "${formatMoney(habit.rewardRate)} each time"
                            !showNextPayout -> "${formatMoney(habit.rewardRate)} × streak"
                            !completedToday -> "+${formatMoney(nextPayout(habit, today))}"
                            // Without a visible streak there's no interval to wait out; it can be done again tomorrow.
                            habit.intervalDays == 1 || !habit.showStreak -> "+${formatMoney(nextPayout(habit, today))} tomorrow"
                            else -> "+${formatMoney(nextPayout(habit, today))} next time"
                        }
                    )
                    if (goalLabel != null) {
                        GoalChip(label = goalLabel, onGoldCard = completedToday)
                    }
                }
                if (footerActions != null) {
                    footerActions()
                }
            }
        }
    }
}

@Composable
private fun StreakChip(streak: Int, daily: Boolean) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = when {
                    // Encouraging rather than "0 day streak" for a missed day or a brand-new habit.
                    streak == 0 -> "Start a new streak"
                    // Custom habits count completions, not days.
                    !daily -> "$streak in a row"
                    else -> "$streak day streak"
                },
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun RewardChip(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MonetizationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Text(text = text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun GoalChip(label: String, onGoldCard: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        // A gold chip would vanish into a gold "done today" card, so it switches to white there.
        color = if (onGoldCard) {
            MaterialTheme.colorScheme.surfaceContainerLowest
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        },
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Savings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// When a custom habit must next be done to keep its streak: "Due today", "Due tomorrow", a weekday
// within the coming week, otherwise a short date.
private fun dueLabel(deadline: LocalDate, today: LocalDate): String {
    val daysLeft = ChronoUnit.DAYS.between(today, deadline)
    return when {
        daysLeft <= 0L -> "Due today"
        daysLeft == 1L -> "Due tomorrow"
        daysLeft < 7L -> "Due by " + deadline.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        else -> "Due by " + deadline.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }
}
