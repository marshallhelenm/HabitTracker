package com.example.habittracker.viewmodel

import com.example.habittracker.model.Habit
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Streak rules. A habit's streak survives while it's done at least once every intervalDays days
// (1 for daily habits): done Monday on a 3-day interval, Thursday continues it and Friday is too late.
//
// The stored currentStreak is "the streak as of lastCompletedOn"; it isn't rewritten when the window
// is missed. These functions work out what that means relative to today, so a missed window shows as
// a broken streak immediately without any background job or database write.

fun isCompletedOn(habit: Habit, today: LocalDate): Boolean = habit.lastCompletedOn == today

// The last day the habit can be done without breaking its streak, or null if there's no live streak.
fun dueBy(habit: Habit, today: LocalDate): LocalDate? {
    val last = habit.lastCompletedOn ?: return null
    val deadline = last.plusDays(habit.intervalDays.toLong())
    return if (today.isAfter(deadline)) null else deadline
}

fun isStreakAlive(habit: Habit, today: LocalDate): Boolean = dueBy(habit, today) != null

// The streak to show the user today: the stored streak while it's alive, otherwise 0.
fun effectiveStreak(habit: Habit, today: LocalDate): Int =
    if (isStreakAlive(habit, today)) habit.currentStreak else 0

// The streak after completing the habit today: continues if the previous completion is still within
// the interval, else restarts at 1. Each completion adds one, however early it comes.
fun streakAfterCompletion(habit: Habit, today: LocalDate): Int {
    val last = habit.lastCompletedOn ?: return 1
    val daysSince = ChronoUnit.DAYS.between(last, today)
    return if (daysSince in 1..habit.intervalDays.toLong()) habit.currentStreak + 1 else 1
}
