package com.example.habittracker.ui

import com.example.habittracker.model.Habit
import com.example.habittracker.viewmodel.HabitViewModel
import com.example.habittracker.viewmodel.SavingsGoalViewModel
import java.time.LocalDate

fun completeHabit(habitViewModel: HabitViewModel, savingsGoalViewModel: SavingsGoalViewModel, habit: Habit) {
    val updatedHabit = habitViewModel.markHabitCompletedToday(habit) ?: return
    savingsGoalViewModel.recordEarningsForHabitCompletion(updatedHabit, goal = null)
}

// The streak saver's Yes: counts the habit as done yesterday, reward included.
fun completeHabitYesterday(habitViewModel: HabitViewModel, savingsGoalViewModel: SavingsGoalViewModel, habit: Habit) {
    val updatedHabit = habitViewModel.markHabitCompletedYesterday(habit) ?: return
    savingsGoalViewModel.recordEarningsForHabitCompletion(updatedHabit, goal = null, date = updatedHabit.lastCompletedOn!!)
}

fun toggleHabitToday(habitViewModel: HabitViewModel, savingsGoalViewModel: SavingsGoalViewModel, habit: Habit) {
    if (habit.lastCompletedOn == LocalDate.now()) {
        habitViewModel.unMarkHabitCompletedToday(habit) ?: return
        savingsGoalViewModel.reverseEarningsForHabit(habit)
    } else {
        completeHabit(habitViewModel, savingsGoalViewModel, habit)
    }
}