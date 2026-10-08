package com.example.habittracker.viewmodel

import com.example.habittracker.model.Habit
import java.time.LocalDate

// Streak rules for daily habits. Weekly and custom frequencies are treated as daily for now.
//
// The stored currentStreak is "the streak as of lastCompletedOn"; it isn't rewritten when a day is
// missed. These functions work out what that means relative to today, so a skipped day shows as a
// broken streak immediately without any background job or database write.

fun isCompletedOn(habit: Habit, today: LocalDate): Boolean = habit.lastCompletedOn == today

// A streak survives until the end of the day after its last completion: done yesterday means today
// can still continue it.
fun isStreakAlive(habit: Habit, today: LocalDate): Boolean =
    habit.lastCompletedOn == today || habit.lastCompletedOn == today.minusDays(1)

// The streak to show the user today: the stored streak while it's alive, otherwise 0.
fun effectiveStreak(habit: Habit, today: LocalDate): Int =
    if (isStreakAlive(habit, today)) habit.currentStreak else 0

// The streak after completing the habit today: continues if it was last done yesterday, else restarts at 1.
fun streakAfterCompletion(habit: Habit, today: LocalDate): Int =
    if (habit.lastCompletedOn == today.minusDays(1)) habit.currentStreak + 1 else 1
