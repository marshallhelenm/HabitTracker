package com.example.habittracker.viewmodel

import com.example.habittracker.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class RewardsTest {
    private val today = LocalDate.of(2026, 10, 8)
    private fun habit(streak: Int, last: LocalDate?, rate: String, interval: Int = 1) = Habit(
        id = 1,
        title = "Read",
        currentStreak = streak,
        lastCompletedOn = last,
        rewardRate = BigDecimal(rate),
        intervalDays = interval
    )

    private fun assertMoney(expected: String, actual: BigDecimal) =
        assertEquals("expected $expected but was $actual", 0, BigDecimal(expected).compareTo(actual))

    @Test
    fun payout_isRateTimesStreak() {
        assertMoney("3.75", payoutFor(habit(0, null, "1.25"), 3))
    }

    @Test
    fun nextPayout_beforeCheckingIn_isWhatTodayPays() {
        // Done yesterday with a 4-day streak: today's check-in reaches 5.
        assertMoney("2.50", nextPayout(habit(4, today.minusDays(1), "0.50"), today))
    }

    @Test
    fun nextPayout_afterABrokenStreak_restartsAtOneStep() {
        assertMoney("0.50", nextPayout(habit(4, today.minusDays(3), "0.50"), today))
        assertMoney("2", nextPayout(habit(0, null, "2"), today))
    }

    @Test
    fun nextPayout_afterCheckingInToday_isTheFollowingCheckIn() {
        assertMoney("3", nextPayout(habit(5, today, "0.50"), today))
    }

    @Test
    fun nextPayout_forCustomInterval_countsWithinTheWindow() {
        assertMoney("6", nextPayout(habit(2, today.minusDays(3), "2", interval = 3), today))
    }
}
