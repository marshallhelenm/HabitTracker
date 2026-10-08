package com.example.habittracker.viewmodel

import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SavingsGoalViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun goal(id: Long, pinned: Boolean = false, createdDay: Int = 1, cap: String? = null) = SavingsGoal(
        id = id,
        name = "Goal $id",
        targetAmount = BigDecimal("100"),
        currentAmount = BigDecimal.ZERO,
        dailyEarningsCap = cap?.let(::BigDecimal),
        createdAt = LocalDateTime.of(2026, 1, createdDay, 9, 0),
        pinned = pinned
    )

    private fun habit(goalId: Long?, streak: Int) = Habit(id = 1, title = "Stretch", currentStreak = streak, goalId = goalId)

    private fun TestScope.viewModelWith(repo: FakeSavingsGoalRepository): SavingsGoalViewModel {
        val vm = SavingsGoalViewModel(repo, dispatcher)
        advanceUntilIdle()
        return vm
    }

    private suspend fun FakeSavingsGoalRepository.amountOf(id: Long) = getSavingsGoalById(id)!!.currentAmount

    @Test
    fun completingAssignedHabit_paysIntoItsOwnGoal() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true), goal(2)))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 2, streak = 3), goal = null)
        advanceUntilIdle()

        assertEquals(BigDecimal("3"), repo.amountOf(2))
        assertEquals(BigDecimal.ZERO, repo.amountOf(1))
    }

    @Test
    fun completingUnassignedHabit_paysIntoPinnedGoal() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1), goal(2, pinned = true)))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 2), goal = null)
        advanceUntilIdle()

        assertEquals(BigDecimal("2"), repo.amountOf(2))
        assertEquals(BigDecimal.ZERO, repo.amountOf(1))
    }

    @Test
    fun habitLinkedToMissingGoal_fallsBackToPinnedGoal() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true)))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 99, streak = 4), goal = null)
        advanceUntilIdle()

        assertEquals(BigDecimal("4"), repo.amountOf(1))
    }

    @Test
    fun earningsAreLimitedByTargetGoalsCap() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, cap = "10"), goal(2, cap = "2")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 2, streak = 5), goal = null)
        advanceUntilIdle()

        assertEquals(BigDecimal("2"), repo.amountOf(2))
    }

    @Test
    fun backToBackCompletions_bothDepositsLand() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true)))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 2), goal = null)
        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 3), goal = null)
        advanceUntilIdle()

        assertEquals(BigDecimal("5"), repo.amountOf(1))
    }

    @Test
    fun firstGoalCreated_isPinned_laterGoalsAreNot() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository()
        val vm = viewModelWith(repo)

        assertTrue(vm.addSavingsGoal("Bike", "300"))
        advanceUntilIdle()
        assertTrue(vm.addSavingsGoal("Trip", "900"))
        advanceUntilIdle()

        val goals = vm.savingsGoals.value
        assertTrue(goals.single { it.name == "Bike" }.pinned)
        assertFalse(goals.single { it.name == "Trip" }.pinned)
    }

    @Test
    fun deletingPinnedGoal_pinsOldestRemainingGoal() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(
            listOf(goal(1, pinned = true, createdDay = 1), goal(2, createdDay = 9), goal(3, createdDay = 4))
        )
        val vm = viewModelWith(repo)

        vm.deleteSavingsGoal(1)
        advanceUntilIdle()

        assertTrue(repo.getSavingsGoalById(3)!!.pinned)
        assertFalse(repo.getSavingsGoalById(2)!!.pinned)
        assertEquals(3L, vm.pinnedGoal.value?.id)
    }

    @Test
    fun loadingGoalsWithNonePinned_pinsOldest() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, createdDay = 5), goal(2, createdDay = 2)))
        viewModelWith(repo)

        assertTrue(repo.getSavingsGoalById(2)!!.pinned)
        assertFalse(repo.getSavingsGoalById(1)!!.pinned)
    }
}
