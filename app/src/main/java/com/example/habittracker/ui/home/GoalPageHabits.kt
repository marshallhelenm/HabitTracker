package com.example.habittracker.ui.home

import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal

// A goal's page lists the habits that pay into it. Unassigned habits, and any linked to a goal that is
// no longer active (completed, archived or deleted), pay into the pinned goal, so they appear on its
// page after the habits explicitly assigned to it. Order within each group is preserved.
fun habitsForGoalPage(habits: List<Habit>, goal: SavingsGoal, activeGoalIds: Set<Long>): List<Habit> =
    habits
        .filter { habit ->
            val goalId = habit.goalId
            goalId == goal.id || (goal.pinned && (goalId == null || goalId !in activeGoalIds))
        }
        .sortedBy { habit -> if (habit.goalId == goal.id) 0 else 1 }
