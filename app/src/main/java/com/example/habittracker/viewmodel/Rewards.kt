package com.example.habittracker.viewmodel

import com.example.habittracker.model.Habit
import java.math.BigDecimal
import java.time.LocalDate

// Reward rates are set in quarters between these bounds.
val REWARD_STEP = BigDecimal("0.25")
val MIN_REWARD_RATE = BigDecimal("0.25")
val MAX_REWARD_RATE = BigDecimal("20")

// What a check-in pays when it brings the habit's streak to `streak`.
fun payoutFor(habit: Habit, streak: Int): BigDecimal = habit.rewardRate * BigDecimal(streak)

// What the habit's next check-in will pay: today's if it isn't done yet, otherwise the following one,
// assuming the streak is kept. Daily caps may pay less; this is the amount before any cap.
fun nextPayout(habit: Habit, today: LocalDate): BigDecimal =
    if (isCompletedOn(habit, today)) {
        payoutFor(habit, habit.currentStreak + 1)
    } else {
        payoutFor(habit, streakAfterCompletion(habit, today))
    }
