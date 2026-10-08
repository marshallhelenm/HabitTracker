package com.example.habittracker.ui.home

import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class GoalPageHabitsTest {
    private val pinned = SavingsGoal(id = 1, name = "Bike", targetAmount = BigDecimal.TEN, pinned = true)
    private val other = SavingsGoal(id = 2, name = "Trip", targetAmount = BigDecimal.TEN)
    private val existingIds = setOf(1L, 2L)

    private val forPinned = Habit(id = 10, title = "Run", goalId = 1)
    private val forOther = Habit(id = 11, title = "Read", goalId = 2)
    private val unassigned = Habit(id = 12, title = "Stretch", goalId = null)
    private val orphaned = Habit(id = 13, title = "Floss", goalId = 99)
    private val all = listOf(forPinned, forOther, unassigned, orphaned)

    @Test
    fun pinnedGoalPage_showsItsHabitsPlusUnassignedAndOrphaned() {
        assertEquals(
            listOf(forPinned, unassigned, orphaned),
            habitsForGoalPage(all, pinned, existingIds)
        )
    }

    @Test
    fun pinnedGoalPage_listsAssignedHabitsBeforeUnassigned_keepingOrderWithinEach() {
        val laterAssigned = Habit(id = 14, title = "Budget review", goalId = 1)
        val mixed = listOf(unassigned, forPinned, orphaned, forOther, laterAssigned)

        assertEquals(
            listOf(forPinned, laterAssigned, unassigned, orphaned),
            habitsForGoalPage(mixed, pinned, existingIds)
        )
    }

    @Test
    fun habitOfInactiveGoal_appearsOnPinnedPage() {
        val forCompleted = Habit(id = 15, title = "Save receipts", goalId = 2)
        val activeOnlyPinned = setOf(1L)

        assertEquals(
            listOf(forPinned, forCompleted),
            habitsForGoalPage(listOf(forCompleted, forPinned), pinned, activeOnlyPinned)
        )
    }

    @Test
    fun otherGoalPage_showsOnlyItsOwnHabits() {
        assertEquals(listOf(forOther), habitsForGoalPage(all, other, existingIds))
    }
}
