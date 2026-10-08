package com.example.habittracker.viewmodel

import com.example.habittracker.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakRulesTest {
    private val today = LocalDate.of(2026, 10, 8)
    private fun habit(streak: Int, last: LocalDate?) = Habit(id = 1, title = "Read", currentStreak = streak, lastCompletedOn = last)

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
}
