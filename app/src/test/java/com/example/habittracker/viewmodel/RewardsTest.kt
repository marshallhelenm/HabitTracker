package com.example.habittracker.viewmodel

import com.example.habittracker.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class RewardsTest {
    private val today = LocalDate.of(2026, 10, 8)
    private fun habit(streak: Int, last: LocalDate?, rate: String, interval: Int = 1, fixed: Boolean = false) = Habit(
        id = 1,
        title = "Read",
        currentStreak = streak,
        lastCompletedOn = last,
        rewardRate = BigDecimal(rate),
        intervalDays = interval,
        fixedReward = fixed
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

    @Test
    fun fixedPayout_ignoresTheStreak() {
        assertMoney("5", payoutFor(habit(0, null, "5", fixed = true), 1))
        assertMoney("5", payoutFor(habit(0, null, "5", fixed = true), 30))
    }

    @Test
    fun fixedNextPayout_isTheSameWhetherTheStreakIsAliveOrBroken() {
        assertMoney("2", nextPayout(habit(9, today.minusDays(1), "2", fixed = true), today))
        assertMoney("2", nextPayout(habit(9, today.minusDays(5), "2", fixed = true), today))
        assertMoney("2", nextPayout(habit(9, today, "2", fixed = true), today))
    }

    @Test
    fun rewardRateFrom_acceptsAnyCentAmountInRange() {
        assertMoney("3", rewardRateFrom("3")!!)
        assertMoney("1.5", rewardRateFrom(" 1.50 ")!!)
        assertMoney("0.25", rewardRateFrom("0.25")!!)
        assertMoney("20", rewardRateFrom("20")!!)
        assertMoney("1.1", rewardRateFrom("1,10")!!)
    }

    @Test
    fun rewardRateFrom_rejectsOutOfRangeOrTooPrecise() {
        assertEquals(null, rewardRateFrom(""))
        assertEquals(null, rewardRateFrom("abc"))
        assertEquals(null, rewardRateFrom("0.10"))
        assertEquals(null, rewardRateFrom("20.01"))
        assertEquals(null, rewardRateFrom("1.234"))
        assertEquals(null, rewardRateFrom("-1"))
    }

    @Test
    fun rewardRateText_dropsTrailingZeros() {
        assertEquals("1.5", rewardRateText(BigDecimal("1.50")))
        assertEquals("2", rewardRateText(BigDecimal("2.00")))
        assertEquals("20", rewardRateText(BigDecimal("20")))
    }
}
