package com.example.habittracker.ui.home

import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal

// A goal's page lists the habits that pay into it. Unassigned habits, and any linked to a goal that
// no longer exists, pay into the pinned goal, so they appear on its page.
fun habitsForGoalPage(habits: List<Habit>, goal: SavingsGoal, existingGoalIds: Set<Long>): List<Habit> =
    habits.filter { habit ->
        val goalId = habit.goalId
        goalId == goal.id || (goal.pinned && (goalId == null || goalId !in existingGoalIds))
    }
