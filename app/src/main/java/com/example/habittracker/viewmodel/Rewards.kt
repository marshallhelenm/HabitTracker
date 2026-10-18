package com.example.habittracker.viewmodel

import com.example.habittracker.model.Habit
import java.math.BigDecimal
import java.time.LocalDate

// Reward rates are between these bounds, to the cent. The stepper buttons move by REWARD_STEP;
// any other amount can be typed in.
val REWARD_STEP = BigDecimal("0.25")
val MIN_REWARD_RATE = BigDecimal("0.25")
val MAX_REWARD_RATE = BigDecimal("20")

// The reward rate typed text describes, or null if it isn't a valid amount yet. Accepts a comma
// as the decimal separator, since some keyboards offer only that.
fun rewardRateFrom(text: String): BigDecimal? =
    text.trim().replace(',', '.').toBigDecimalOrNull()
        ?.takeIf { it in MIN_REWARD_RATE..MAX_REWARD_RATE && it.stripTrailingZeros().scale() <= 2 }

// How a reward rate reads in the text field: "1.5", "2", "0.25".
fun rewardRateText(rate: BigDecimal): String = rate.stripTrailingZeros().toPlainString()

// What a check-in pays when it brings the habit's streak to `streak`. Fixed-reward habits ignore the streak.
fun payoutFor(habit: Habit, streak: Int): BigDecimal =
    if (habit.fixedReward) habit.rewardRate else habit.rewardRate * BigDecimal(streak)

// What the habit's next check-in will pay: today's if it isn't done yet, otherwise the following one,
// assuming the streak is kept. Daily caps may pay less; this is the amount before any cap.
fun nextPayout(habit: Habit, today: LocalDate): BigDecimal =
    if (isCompletedOn(habit, today)) {
        payoutFor(habit, habit.currentStreak + 1)
    } else {
        payoutFor(habit, streakAfterCompletion(habit, today))
    }
