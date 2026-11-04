package com.example.habittracker.viewmodel

import com.example.habittracker.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakRulesTest {
    private val today = LocalDate.of(2026, 10, 8)
    private fun habit(streak: Int, last: LocalDate?, interval: Int = 1) =
        Habit(id = 1, title = "Read", currentStreak = streak, lastCompletedOn = last, intervalDays = interval)

    @Test
    fun streakDoneToday_isShownAsIs() {
        assertEquals(5, effectiveStreak(habit(5, today), today))
    }

    @Test
    fun streakDoneYesterday_isStillAlive() {
        val h = habit(5, today.minusDays(1))
        assertTrue(isStreakAlive(h, today))
        assertEquals(5, effectiveStreak(h, today))
    }

    @Test
    fun missingAWholeDay_breaksTheStreak() {
        val h = habit(12, today.minusDays(2))
        assertFalse(isStreakAlive(h, today))
        assertEquals(0, effectiveStreak(h, today))
    }

    @Test
    fun neverCompleted_hasNoStreak() {
        assertEquals(0, effectiveStreak(habit(0, null), today))
    }

    @Test
    fun completingTheDayAfter_continuesTheStreak() {
        assertEquals(6, streakAfterCompletion(habit(5, today.minusDays(1)), today))
    }

    @Test
    fun completingAfterAGap_restartsAtOne() {
        assertEquals(1, streakAfterCompletion(habit(12, today.minusDays(3)), today))
        assertEquals(1, streakAfterCompletion(habit(0, null), today))
    }

    @Test
    fun completedOn_onlyMatchesTheSameDay() {
        assertTrue(isCompletedOn(habit(1, today), today))
        assertFalse(isCompletedOn(habit(1, today.minusDays(1)), today))
        assertFalse(isCompletedOn(habit(0, null), today))
    }

    @Test
    fun customInterval_survivesThroughItsLastDay() {
        // Done Monday on a 3-day interval: still alive on Thursday.
        val h = habit(4, today.minusDays(3), interval = 3)
        assertTrue(isStreakAlive(h, today))
        assertEquals(today, dueBy(h, today))
        assertEquals(5, streakAfterCompletion(h, today))
    }

    @Test
    fun customInterval_breaksTheDayAfterItsWindow() {
        val h = habit(4, today.minusDays(4), interval = 3)
        assertFalse(isStreakAlive(h, today))
        assertNull(dueBy(h, today))
        assertEquals(0, effectiveStreak(h, today))
        assertEquals(1, streakAfterCompletion(h, today))
    }

    @Test
    fun customInterval_countsEveryCompletion_evenEarlyOnes() {
        assertEquals(5, streakAfterCompletion(habit(4, today.minusDays(1), interval = 3), today))
    }

    @Test
    fun dueBy_isLastCompletionPlusInterval() {
        assertEquals(today.plusDays(2), dueBy(habit(2, today.minusDays(1), interval = 3), today))
        assertEquals(today.plusDays(1), dueBy(habit(2, today), today))
        assertNull(dueBy(habit(0, null, interval = 3), today))
    }

    @Test
    fun dailyHabitLastDoneTwoDaysAgo_brokeYesterday() {
        val h = habit(4, today.minusDays(2))
        assertTrue(streakBrokeYesterday(h, today))
        // Counting yesterday continues the streak rather than restarting it.
        assertEquals(5, streakAfterCompletion(h, today.minusDays(1)))
    }

    @Test
    fun dailyHabitDoneYesterday_orMissedLonger_isNotOfferedASave() {
        assertFalse(streakBrokeYesterday(habit(4, today.minusDays(1)), today))
        assertFalse(streakBrokeYesterday(habit(4, today.minusDays(3)), today))
        assertFalse(streakBrokeYesterday(habit(0, null), today))
    }

    @Test
    fun customHabitWhoseLastAllowedDayWasYesterday_brokeYesterday() {
        assertTrue(streakBrokeYesterday(habit(2, today.minusDays(4), interval = 3), today))
        // Still inside its window, so nothing is broken yet.
        assertFalse(streakBrokeYesterday(habit(2, today.minusDays(3), interval = 3), today))
    }

    @Test
    fun hiddenStreak_isNotOfferedASave() {
        val h = habit(4, today.minusDays(2)).copy(fixedReward = true, showStreak = false)
        assertFalse(streakBrokeYesterday(h, today))
    }
}
