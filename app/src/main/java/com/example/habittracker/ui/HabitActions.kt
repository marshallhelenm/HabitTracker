package com.example.habittracker.ui

import com.example.habittracker.model.Habit
import com.example.habittracker.viewmodel.HabitViewModel
import com.example.habittracker.viewmodel.SavingsGoalViewModel

fun completeHabit(habitViewModel: HabitViewModel, savingsGoalViewModel: SavingsGoalViewModel, habit: Habit) {
    val updatedHabit = habitViewModel.markHabitCompletedToday(habit) ?: return
    savingsGoalViewModel.recordEarningsForHabitCompletion(updatedHabit, goal = null)
}