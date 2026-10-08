package com.example.habittracker.ui.goals

import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.model.SavingsGoalStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class GoalListOrderTest {
    private fun goal(
        id: Long,
        name: String,
        saved: String,
        createdDay: Int,
        pinned: Boolean = false,
        status: SavingsGoalStatus = SavingsGoalStatus.ACTIVE
    ) = SavingsGoal(
        id = id,
        name = name,
        targetAmount = BigDecimal("100"),
        currentAmount = BigDecimal(saved),
        status = status,
        createdAt = LocalDateTime.of(2026, 1, createdDay, 9, 0),
        pinned = pinned
    )

    private val trip = goal(1, "trip", saved = "10", createdDay = 2)
    private val bike = goal(2, "Bike", saved = "90", createdDay = 5, status = SavingsGoalStatus.COMPLETED)
    private val noodle = goal(3, "Danger Noodle", saved = "20", createdDay = 9, pinned = true)
    private val laptop = goal(4, "Laptop", saved = "100", createdDay = 1, status = SavingsGoalStatus.ARCHIVED)
    private val amp = goal(5, "Amp", saved = "60", createdDay = 4)
    private val all = listOf(trip, bike, noodle, laptop, amp)

    private fun ids(sort: GoalSort, hide: Boolean = false) = goalsForList(all, sort, hide).map { it.id }

    @Test
    fun pinnedFirst_thenOldest_withCompletedThenArchivedAtBottom() {
        assertEquals(listOf(3L, 1L, 5L, 2L, 4L), ids(GoalSort.PINNED_FIRST))
    }

    @Test
    fun alphabetical_isCaseInsensitive_withinEachGroup() {
        assertEquals(listOf(5L, 3L, 1L, 2L, 4L), ids(GoalSort.NAME))
    }

    @Test
    fun reverseAlphabetical_withinEachGroup() {
        assertEquals(listOf(1L, 3L, 5L, 2L, 4L), ids(GoalSort.NAME_DESCENDING))
    }

    @Test
    fun progress_highestFirst_withinEachGroup() {
        assertEquals(listOf(5L, 3L, 1L, 2L, 4L), ids(GoalSort.PROGRESS))
    }

    @Test
    fun progressAscending_lowestFirst_withinEachGroup() {
        assertEquals(listOf(1L, 3L, 5L, 2L, 4L), ids(GoalSort.PROGRESS_ASCENDING))
    }

    @Test
    fun reversiblePairsPointAtEachOther() {
        assertEquals(GoalSort.NAME_DESCENDING, GoalSort.NAME.reversed)
        assertEquals(GoalSort.NAME, GoalSort.NAME_DESCENDING.reversed)
        assertEquals(GoalSort.PROGRESS_ASCENDING, GoalSort.PROGRESS.reversed)
        assertEquals(GoalSort.PROGRESS, GoalSort.PROGRESS_ASCENDING.reversed)
        assertEquals(null, GoalSort.PINNED_FIRST.reversed)
    }

    @Test
    fun hideCompleted_leavesOutCompletedAndArchived() {
        assertEquals(listOf(3L, 1L, 5L), ids(GoalSort.PINNED_FIRST, hide = true))
    }
}
