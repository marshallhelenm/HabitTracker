package com.example.habittracker.viewmodel

import com.example.habittracker.model.Deposit
import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.model.SavingsGoalStatus
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
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SavingsGoalViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private var currentDay = LocalDate.of(2026, 10, 7)

    private fun goal(
        id: Long,
        pinned: Boolean = false,
        createdDay: Int = 1,
        cap: String? = null,
        target: String = "100",
        saved: String = "0",
        status: SavingsGoalStatus = SavingsGoalStatus.ACTIVE,
        order: Int = 0
    ) = SavingsGoal(
        id = id,
        name = "Goal $id",
        targetAmount = BigDecimal(target),
        currentAmount = BigDecimal(saved),
        dailyEarningsCap = cap?.let(::BigDecimal),
        status = status,
        createdAt = LocalDateTime.of(2026, 1, createdDay, 9, 0),
        pinned = pinned,
        sortOrder = order
    )

    private fun habit(goalId: Long?, streak: Int, id: Long = 1, rate: String = "1") =
        Habit(id = id, title = "Habit $id", currentStreak = streak, goalId = goalId, rewardRate = BigDecimal(rate))

    private fun TestScope.viewModelWith(repo: FakeSavingsGoalRepository): SavingsGoalViewModel {
        val vm = SavingsGoalViewModel(repo, dispatcher) { currentDay }
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
    fun deletingPinnedGoal_pinsFirstRemainingGoalInOrder() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(
            listOf(goal(1, pinned = true, order = 1), goal(2, order = 9), goal(3, order = 4))
        )
        val vm = viewModelWith(repo)

        vm.deleteSavingsGoal(1)
        advanceUntilIdle()

        assertTrue(repo.getSavingsGoalById(3)!!.pinned)
        assertFalse(repo.getSavingsGoalById(2)!!.pinned)
        assertEquals(3L, vm.pinnedGoal.value?.id)
    }

    @Test
    fun loadingGoalsWithNonePinned_pinsFirstInOrder() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, order = 5), goal(2, order = 2)))
        viewModelWith(repo)

        assertTrue(repo.getSavingsGoalById(2)!!.pinned)
        assertFalse(repo.getSavingsGoalById(1)!!.pinned)
    }

    @Test
    fun dailyCapIsSharedAcrossHabits_andResetsNextDay() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, cap = "5")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 1, streak = 3, id = 1), goal = null)
        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 4, id = 2), goal = null)
        advanceUntilIdle()
        assertEquals(BigDecimal("5"), repo.amountOf(1))
        assertEquals(listOf(EarningsNotice.DailyCapReached("Goal 1", BigDecimal("5"))), vm.notices.value)

        currentDay = currentDay.plusDays(1)
        vm.recordEarningsForHabitCompletion(habit(goalId = 1, streak = 4, id = 1), goal = null)
        advanceUntilIdle()
        assertEquals(BigDecimal("9"), repo.amountOf(1))
    }

    @Test
    fun depositsAreLoggedWithHabitDateAndOverflowSource() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true), goal(2, target = "10", saved = "8")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 2, streak = 5, id = 42), goal = null)
        advanceUntilIdle()

        assertEquals(
            listOf(Triple(2L, BigDecimal("2"), null), Triple(1L, BigDecimal("3"), 2L)),
            repo.deposits.map { Triple(it.goalId, it.amount, it.overflowFromGoalId) }
        )
        assertTrue(repo.deposits.all { it.habitId == 42L && it.depositedOn == currentDay })
    }

    @Test
    fun fillingAGoal_completesIt_overflowsToPinned_andAnnouncesIt() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true), goal(2, target = "10", saved = "8")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 2, streak = 5), goal = null)
        advanceUntilIdle()

        assertEquals(SavingsGoalStatus.COMPLETED, repo.getSavingsGoalById(2)!!.status)
        assertEquals(BigDecimal("10"), repo.amountOf(2))
        assertEquals(BigDecimal("3"), repo.amountOf(1))
        assertEquals(
            listOf(EarningsNotice.GoalCompleted("Goal 2", BigDecimal("3"), "Goal 1")),
            vm.notices.value
        )
    }

    @Test
    fun pinnedGoalCompleting_movesPinToFirstActiveGoalInOrder() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(
            listOf(goal(1, pinned = true, target = "10", saved = "9"), goal(2, order = 8), goal(3, order = 3))
        )
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 4), goal = null)
        advanceUntilIdle()

        assertFalse(repo.getSavingsGoalById(1)!!.pinned)
        assertTrue(repo.getSavingsGoalById(3)!!.pinned)
        assertEquals(BigDecimal("3"), repo.amountOf(3))
        assertEquals(3L, vm.pinnedGoal.value?.id)
    }

    @Test
    fun habitOfCompletedGoal_paysIntoPinnedGoal() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(
            listOf(goal(1, pinned = true), goal(2, target = "10", saved = "10", status = SavingsGoalStatus.COMPLETED))
        )
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 2, streak = 4), goal = null)
        advanceUntilIdle()

        assertEquals(BigDecimal("4"), repo.amountOf(1))
        assertEquals(BigDecimal("10"), repo.amountOf(2))
    }

    @Test
    fun loadingGoalAlreadyPastTarget_completesIt_keepsAmount_andMovesPin() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(
            listOf(goal(1, pinned = true, target = "150", saved = "162"), goal(2, order = 2))
        )
        viewModelWith(repo)

        val concert = repo.getSavingsGoalById(1)!!
        assertEquals(SavingsGoalStatus.COMPLETED, concert.status)
        assertEquals(BigDecimal("162"), concert.currentAmount)
        assertFalse(concert.pinned)
        assertTrue(repo.getSavingsGoalById(2)!!.pinned)
    }

    @Test
    fun editingTarget_reopensOrCompletesGoal() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(
            listOf(
                goal(1, pinned = true, target = "100", saved = "40"),
                goal(2, target = "50", saved = "50", status = SavingsGoalStatus.COMPLETED)
            )
        )
        val vm = viewModelWith(repo)

        assertTrue(vm.updateSavingsGoal(2, "Goal 2", "80", ""))
        advanceUntilIdle()
        assertEquals(SavingsGoalStatus.ACTIVE, repo.getSavingsGoalById(2)!!.status)

        assertTrue(vm.updateSavingsGoal(1, "Goal 1", "40", ""))
        advanceUntilIdle()
        assertEquals(SavingsGoalStatus.COMPLETED, repo.getSavingsGoalById(1)!!.status)
        assertTrue("pin moves to the reopened goal", repo.getSavingsGoalById(2)!!.pinned)
    }

    @Test
    fun completedGoalsCannotBePinned_andDoNotAppearInCarousel() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(
            listOf(
                goal(1, order = 1, target = "10", saved = "10", status = SavingsGoalStatus.COMPLETED),
                goal(2, order = 5, pinned = true),
                goal(3, order = 3)
            )
        )
        val vm = viewModelWith(repo)

        vm.pinSavingsGoal(1)
        advanceUntilIdle()

        assertFalse(repo.getSavingsGoalById(1)!!.pinned)
        assertEquals(listOf(2L, 3L), vm.carouselGoals.value.map { it.id })
    }

    @Test
    fun dismissingNoticeRemovesIt() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, cap = "1")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 3), goal = null)
        advanceUntilIdle()
        val notice = vm.notices.value.single()

        vm.dismissNotice(notice)
        assertTrue(vm.notices.value.isEmpty())
    }

    @Test
    fun loadedFlipsOnlyAfterGoalsAreRead() = runTest(dispatcher) {
        val vm = SavingsGoalViewModel(FakeSavingsGoalRepository(listOf(goal(1, pinned = true))), dispatcher) { currentDay }
        assertFalse(vm.loaded.value)

        advanceUntilIdle()

        assertTrue(vm.loaded.value)
        assertEquals(1, vm.savingsGoals.value.size)
    }

    @Test
    fun reversingAHabit_removesItsDepositsAndRestoresTheBalance() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, saved = "20")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 4), goal = null)
        advanceUntilIdle()
        assertEquals(BigDecimal("24"), repo.amountOf(1))

        vm.reverseEarningsForHabit(habit(goalId = null, streak = 4))
        advanceUntilIdle()

        assertEquals(BigDecimal("20"), repo.amountOf(1))
        assertTrue(repo.deposits.isEmpty())
    }

    @Test
    fun reversingOnlyTouchesThatHabitsDepositsFromToday() = runTest(dispatcher) {
        val yesterday = currentDay.minusDays(1)
        val repo = FakeSavingsGoalRepository(
            listOf(goal(1, pinned = true, saved = "13")),
            initialDeposits = listOf(
                // Same habit, but yesterday: must stay.
                Deposit(id = 1, goalId = 1, habitId = 1, amount = BigDecimal("3"), depositedOn = yesterday)
            )
        )
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 4, id = 1), goal = null)
        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 2, id = 2), goal = null)
        advanceUntilIdle()
        assertEquals(BigDecimal("19"), repo.amountOf(1))

        vm.reverseEarningsForHabit(habit(goalId = null, streak = 4, id = 1))
        advanceUntilIdle()

        assertEquals(BigDecimal("15"), repo.amountOf(1))
        assertEquals(
            listOf(Pair(1L, yesterday), Pair(2L, currentDay)),
            repo.deposits.map { it.habitId to it.depositedOn }
        )
    }

    @Test
    fun reversingTheCompletionThatFilledAGoal_reopensItAndTakesBackTheOverflow() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true), goal(2, target = "10", saved = "8")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 2, streak = 5), goal = null)
        advanceUntilIdle()
        assertEquals(SavingsGoalStatus.COMPLETED, repo.getSavingsGoalById(2)!!.status)
        assertEquals(BigDecimal("3"), repo.amountOf(1))

        vm.reverseEarningsForHabit(habit(goalId = 2, streak = 5))
        advanceUntilIdle()

        assertEquals(SavingsGoalStatus.ACTIVE, repo.getSavingsGoalById(2)!!.status)
        assertEquals(BigDecimal("8"), repo.amountOf(2))
        assertEquals(BigDecimal("0"), repo.amountOf(1))
        assertTrue(repo.deposits.isEmpty())
    }

    @Test
    fun reversingAfterReversing_doesNothing() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, saved = "20")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 4), goal = null)
        advanceUntilIdle()
        vm.reverseEarningsForHabit(habit(goalId = null, streak = 4))
        vm.reverseEarningsForHabit(habit(goalId = null, streak = 4))
        advanceUntilIdle()

        assertEquals(BigDecimal("20"), repo.amountOf(1))
    }

    @Test
    fun reversingFreesUpTheDailyCapForLaterCompletions() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, cap = "5")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 5, id = 1), goal = null)
        advanceUntilIdle()
        vm.reverseEarningsForHabit(habit(goalId = null, streak = 5, id = 1))
        vm.recordEarningsForHabitCompletion(habit(goalId = null, streak = 4, id = 2), goal = null)
        advanceUntilIdle()

        assertEquals(BigDecimal("4"), repo.amountOf(1))
    }

    @Test
    fun carousel_showsPinnedFirst_thenActiveGoalsInUserOrder() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(
            listOf(
                goal(1, order = 2),
                goal(2, pinned = true, order = 3),
                goal(3, order = 0),
                goal(4, order = 1, status = SavingsGoalStatus.COMPLETED)
            )
        )
        val vm = viewModelWith(repo)

        assertEquals(listOf(2L, 3L, 1L), vm.carouselGoals.value.map { it.id })
    }

    @Test
    fun reorderGoals_savesNewOrder_andLeavesOtherGoalsInTheirSlots() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(
            listOf(goal(1, pinned = true, order = 0), goal(2, order = 1), goal(3, order = 2), goal(4, order = 3))
        )
        val vm = viewModelWith(repo)

        // Drag goal 4 to the top of the draggable goals (everything but the pinned one).
        vm.reorderGoals(listOf(4L, 2L, 3L))
        advanceUntilIdle()

        assertEquals(listOf(1L, 4L, 2L, 3L), vm.carouselGoals.value.map { it.id })
        // Unpinning goal 1 later puts it back in first place, since its slot was kept.
        assertEquals(0, repo.getSavingsGoalById(1)!!.sortOrder)
    }

    @Test
    fun newGoal_goesToTheEndOfTheOrder() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, order = 4), goal(2, order = 7)))
        val vm = viewModelWith(repo)

        assertTrue(vm.addSavingsGoal("Amp", "50"))
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L, 3L), vm.carouselGoals.value.map { it.id })
        assertEquals(8, repo.getSavingsGoalById(3)!!.sortOrder)
    }

    @Test
    fun whenNoGoalIsPinned_thePinFallsToTheFirstGoalInUserOrder() = runTest(dispatcher) {
        // Goal 1 is older, but the user put goal 2 first.
        val repo = FakeSavingsGoalRepository(listOf(goal(1, createdDay = 1, order = 1), goal(2, createdDay = 5, order = 0)))
        viewModelWith(repo)

        assertTrue(repo.getSavingsGoalById(2)!!.pinned)
        assertFalse(repo.getSavingsGoalById(1)!!.pinned)
    }

    @Test
    fun orderWithMoved_handlesTiedPositionsFromBeforeReordering() {
        // Goals that all share sortOrder 0 fall back to id order before being renumbered.
        val goals = listOf(goal(3), goal(1), goal(2))
        assertEquals(listOf(2L, 1L, 3L), orderWithMoved(goals, listOf(2L, 1L, 3L)))
        assertEquals(listOf(1L, 3L, 2L), orderWithMoved(goals, listOf(3L, 2L)))
    }

    @Test
    fun rewardRate_multipliesTheStreak() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true), goal(2)))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 1, streak = 3, id = 1, rate = "0.25"), goal = null)
        vm.recordEarningsForHabitCompletion(habit(goalId = 2, streak = 4, id = 2, rate = "2.50"), goal = null)
        advanceUntilIdle()

        assertEquals(0, BigDecimal("0.75").compareTo(repo.amountOf(1)))
        assertEquals(0, BigDecimal("10").compareTo(repo.amountOf(2)))
    }

    @Test
    fun highRewardRate_isStillLimitedByTheDailyCap() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, cap = "5")))
        val vm = viewModelWith(repo)

        vm.recordEarningsForHabitCompletion(habit(goalId = 1, streak = 2, rate = "4"), goal = null)
        advanceUntilIdle()

        assertEquals(0, BigDecimal("5").compareTo(repo.amountOf(1)))
        assertEquals(listOf(EarningsNotice.DailyCapReached("Goal 1", BigDecimal("5"))), vm.notices.value)
    }

    @Test
    fun editingBalance_setsAmountWithoutLoggingADeposit() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, saved = "40", cap = "5")))
        val vm = viewModelWith(repo)

        assertTrue(vm.updateSavingsGoal(1, "Goal 1", "100", "5", balanceText = "62.50"))
        advanceUntilIdle()

        assertEquals(0, BigDecimal("62.50").compareTo(repo.amountOf(1)))
        assertTrue("manual changes aren't deposits", repo.deposits.isEmpty())
        // The daily cap is untouched, so a habit can still earn the full 5 coins today.
        vm.recordEarningsForHabitCompletion(habit(goalId = 1, streak = 5), goal = null)
        advanceUntilIdle()
        assertEquals(0, BigDecimal("67.50").compareTo(repo.amountOf(1)))
    }

    @Test
    fun editingBalance_withoutBalanceText_keepsTheAmount() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, saved = "40")))
        val vm = viewModelWith(repo)

        assertTrue(vm.updateSavingsGoal(1, "Renamed", "100", ""))
        advanceUntilIdle()

        assertEquals(0, BigDecimal("40").compareTo(repo.amountOf(1)))
    }

    @Test
    fun editingBalance_toTheTarget_completesTheGoal_andBelowItReopens() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, saved = "40", order = 0), goal(2, order = 1)))
        val vm = viewModelWith(repo)

        assertTrue(vm.updateSavingsGoal(1, "Goal 1", "100", "", balanceText = "120"))
        advanceUntilIdle()
        assertEquals(SavingsGoalStatus.COMPLETED, repo.getSavingsGoalById(1)!!.status)
        assertEquals("extra stays in the goal", 0, BigDecimal("120").compareTo(repo.amountOf(1)))
        assertTrue("pin moves on", repo.getSavingsGoalById(2)!!.pinned)

        assertTrue(vm.updateSavingsGoal(1, "Goal 1", "100", "", balanceText = "99.99"))
        advanceUntilIdle()
        assertEquals(SavingsGoalStatus.ACTIVE, repo.getSavingsGoalById(1)!!.status)
    }

    @Test
    fun editingBalance_rejectsNegativeAndFractionalCents() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true, saved = "40")))
        val vm = viewModelWith(repo)

        assertFalse(vm.updateSavingsGoal(1, "Goal 1", "100", "", balanceText = "-1"))
        assertFalse(vm.updateSavingsGoal(1, "Goal 1", "100", "", balanceText = "1.005"))
        assertFalse(vm.updateSavingsGoal(1, "Goal 1", "100", "", balanceText = "lots"))
        assertTrue(vm.updateSavingsGoal(1, "Goal 1", "100", "", balanceText = "0"))
        assertTrue(vm.updateSavingsGoal(1, "Goal 1", "100", "", balanceText = "1.50"))
    }

    @Test
    fun uncheckingAfterBalanceWasLowered_neverGoesBelowZero() = runTest(dispatcher) {
        val repo = FakeSavingsGoalRepository(listOf(goal(1, pinned = true)))
        val vm = viewModelWith(repo)
        vm.recordEarningsForHabitCompletion(habit(goalId = 1, streak = 4), goal = null)
        advanceUntilIdle()

        assertTrue(vm.updateSavingsGoal(1, "Goal 1", "100", "", balanceText = "1"))
        advanceUntilIdle()
        vm.reverseEarningsForHabit(habit(goalId = 1, streak = 4))
        advanceUntilIdle()

        assertEquals(0, BigDecimal.ZERO.compareTo(repo.amountOf(1)))
    }
}
