package com.example.habittracker.viewmodel

import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.model.SavingsGoalStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class EarningsPlannerTest {
    private fun goal(
        id: Long,
        target: String = "100",
        saved: String = "0",
        cap: String? = null,
        pinned: Boolean = false,
        createdDay: Int = id.toInt(),
        status: SavingsGoalStatus = SavingsGoalStatus.ACTIVE
    ) = SavingsGoal(
        id = id,
        name = "Goal $id",
        targetAmount = BigDecimal(target),
        currentAmount = BigDecimal(saved),
        dailyEarningsCap = cap?.let(::BigDecimal),
        status = status,
        createdAt = LocalDateTime.of(2026, 1, createdDay, 9, 0),
        pinned = pinned
    )

    private fun amt(v: String) = BigDecimal(v)
    private fun plan(amount: String, target: SavingsGoal?, goals: List<SavingsGoal>, earned: Map<Long, String> = emptyMap()) =
        planEarnings(amt(amount), target, goals, earned.mapValues { amt(it.value) })

    @Test
    fun noCapNoTarget_depositsFullAmount() {
        val g = goal(1, pinned = true)
        val p = plan("12", g, listOf(g))
        assertEquals(listOf(PlannedDeposit(1, amt("12"), null)), p.deposits)
        assertEquals(emptyList<Long>(), p.completedGoalIds)
        assertEquals(emptyList<Long>(), p.cappedGoalIds)
    }

    @Test
    fun capLimitsDailyTotal_includingEarlierDepositsToday() {
        val g = goal(1, cap = "5", pinned = true)
        val p = plan("12", g, listOf(g), earned = mapOf(1L to "3"))
        assertEquals(listOf(PlannedDeposit(1, amt("2"), null)), p.deposits)
        assertEquals(listOf(1L), p.cappedGoalIds)
    }

    @Test
    fun capAlreadyReached_depositsNothingButReportsCap() {
        val g = goal(1, cap = "5", pinned = true)
        val p = plan("4", g, listOf(g), earned = mapOf(1L to "5"))
        assertEquals(emptyList<PlannedDeposit>(), p.deposits)
        assertEquals(listOf(1L), p.cappedGoalIds)
    }

    @Test
    fun amountExactlyFillingCap_isNotReportedAsCapped() {
        val g = goal(1, cap = "5", pinned = true)
        val p = plan("5", g, listOf(g))
        assertEquals(listOf(PlannedDeposit(1, amt("5"), null)), p.deposits)
        assertEquals(emptyList<Long>(), p.cappedGoalIds)
    }

    @Test
    fun fillingTarget_overflowsRemainderToPinnedGoal() {
        val pinned = goal(1, pinned = true)
        val bike = goal(2, target = "300", saved = "297")
        val p = plan("10", bike, listOf(pinned, bike))
        assertEquals(
            listOf(PlannedDeposit(2, amt("3"), null), PlannedDeposit(1, amt("7"), 2L)),
            p.deposits
        )
        assertEquals(listOf(2L), p.completedGoalIds)
    }

    @Test
    fun overflowRespectsReceivingGoalsCap() {
        val pinned = goal(1, cap = "5", pinned = true)
        val bike = goal(2, target = "300", saved = "297")
        val p = plan("10", bike, listOf(pinned, bike), earned = mapOf(1L to "3"))
        assertEquals(
            listOf(PlannedDeposit(2, amt("3"), null), PlannedDeposit(1, amt("2"), 2L)),
            p.deposits
        )
        assertEquals(listOf(1L), p.cappedGoalIds)
    }

    @Test
    fun pinnedGoalFilling_overflowsToOldestRemainingActiveGoal() {
        val pinned = goal(1, target = "50", saved = "48", pinned = true)
        val newer = goal(3, createdDay = 9)
        val older = goal(2, createdDay = 4)
        val p = plan("6", pinned, listOf(pinned, newer, older))
        assertEquals(
            listOf(PlannedDeposit(1, amt("2"), null), PlannedDeposit(2, amt("4"), 1L)),
            p.deposits
        )
        assertEquals(listOf(1L), p.completedGoalIds)
    }

    @Test
    fun overflowCanCascadeThroughSeveralFillingGoals() {
        val pinned = goal(1, target = "10", saved = "9", pinned = true)
        val second = goal(2, target = "10", saved = "8")
        val third = goal(3)
        val p = plan("5", pinned, listOf(pinned, second, third))
        assertEquals(
            listOf(PlannedDeposit(1, amt("1"), null), PlannedDeposit(2, amt("2"), 1L), PlannedDeposit(3, amt("2"), 2L)),
            p.deposits
        )
        assertEquals(listOf(1L, 2L), p.completedGoalIds)
    }

    @Test
    fun overflowWithNowhereToGo_isDropped() {
        val only = goal(1, target = "10", saved = "8", pinned = true)
        val p = plan("5", only, listOf(only))
        assertEquals(listOf(PlannedDeposit(1, amt("2"), null)), p.deposits)
        assertEquals(listOf(1L), p.completedGoalIds)
    }

    @Test
    fun exactFill_completesWithoutOverflow() {
        val pinned = goal(1, pinned = true)
        val bike = goal(2, target = "10", saved = "6")
        val p = plan("4", bike, listOf(pinned, bike))
        assertEquals(listOf(PlannedDeposit(2, amt("4"), null)), p.deposits)
        assertEquals(listOf(2L), p.completedGoalIds)
    }

    @Test
    fun inactiveStartingGoal_earnsNothing() {
        val done = goal(1, status = SavingsGoalStatus.COMPLETED)
        assertEquals(emptyList<PlannedDeposit>(), plan("5", done, listOf(done)).deposits)
    }

    @Test
    fun capBelowTargetRoom_capsWithoutCompleting() {
        val g = goal(1, target = "10", saved = "2", cap = "3", pinned = true)
        val p = plan("6", g, listOf(g))
        assertEquals(listOf(PlannedDeposit(1, amt("3"), null)), p.deposits)
        assertEquals(emptyList<Long>(), p.completedGoalIds)
        assertEquals(listOf(1L), p.cappedGoalIds)
    }
}
