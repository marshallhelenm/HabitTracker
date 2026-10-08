package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.SavingsGoalRepository
import com.example.habittracker.model.Deposit
import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.model.SavingsGoalStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate

sealed interface EarningsNotice {
    // overflowGoalName is null when nothing overflowed (exact fill, or no other goal could take it).
    data class GoalCompleted(
        val goalName: String,
        val overflowAmount: BigDecimal,
        val overflowGoalName: String?
    ) : EarningsNotice

    data class DailyCapReached(val goalName: String, val cap: BigDecimal) : EarningsNotice
}

class SavingsGoalViewModel(
    private val repository: SavingsGoalRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val today: () -> LocalDate = LocalDate::now
) : ViewModel() {
    // Serialized so back-to-back completions can't overwrite each other's deposits.
    private val earningsMutex = Mutex()

    private val _notices = MutableStateFlow<List<EarningsNotice>>(emptyList())
    val notices: StateFlow<List<EarningsNotice>> = _notices.asStateFlow()

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals.asStateFlow()

    // False until goals are first read from storage, so screens don't flash an empty state on launch.
    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()
    val pinnedGoal: StateFlow<SavingsGoal?> = _savingsGoals
        .map { goals -> goals.firstOrNull { it.pinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Home carousel order: pinned first, then other active goals oldest to newest, then completed goals.
    // Archived goals stay off Home.
    val carouselGoals: StateFlow<List<SavingsGoal>> = _savingsGoals
        .map { goals ->
            goals
                .filter { it.status != SavingsGoalStatus.ARCHIVED }
                .sortedWith(
                    compareBy<SavingsGoal> { it.status != SavingsGoalStatus.ACTIVE }
                        .thenByDescending { it.pinned }
                        .thenBy { it.createdAt }
                        .thenBy { it.id }
                )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _savingsGoals.value = withContext(ioDispatcher) {
                normalizeGoals()
                repository.getSavingsGoals()
                    .sortedWith(
                        compareByDescending<SavingsGoal> { it.pinned }
                            .thenBy { it.id }
                    )
            }
            _loaded.value = true
        }
    }

    fun addSavingsGoal(name: String, targetAmountText: String): Boolean {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) {
            return false
        }

        val targetAmount = targetAmountText.trim().toBigDecimalOrNull()
        if (targetAmount == null || targetAmount <= BigDecimal.ZERO) {
            return false
        }

        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.upsertSavingsGoal(
                    SavingsGoal(
                        name = normalizedName,
                        targetAmount = targetAmount
                    )
                )
            }
            refresh()
        }
        return true
    }

    // onDeleted runs once storage is updated, so callers can reload habits whose goal link was cleared.
    fun deleteSavingsGoal(id: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.deleteSavingsGoal(id)
            }
            refresh()
            onDeleted()
        }
    }

    fun pinSavingsGoal(id: Long) {
        // Only active goals can take earnings, so only they can be pinned.
        if (_savingsGoals.value.none { it.id == id && it.status == SavingsGoalStatus.ACTIVE }) {
            return
        }
        viewModelScope.launch {
            val goals = _savingsGoals.value
            withContext(ioDispatcher) {
                goals.forEach { goal ->
                    repository.upsertSavingsGoal(goal.copy(pinned = goal.id == id))
                }
            }
            refresh()
        }
    }

    fun getGoalById(id: Long): SavingsGoal? {
        return _savingsGoals.value.firstOrNull { it.id == id }
    }

    fun updateSavingsGoal(id: Long, name: String, targetAmountText: String, earningsCapText: String): Boolean {
        val existingGoal = _savingsGoals.value.firstOrNull { it.id == id } ?: return false
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) {
            return false
        }

        val targetAmount = targetAmountText.trim().toBigDecimalOrNull()
        if (targetAmount == null || targetAmount <= BigDecimal.ZERO) {
            return false
        }

        val trimmedEarningsCap = earningsCapText.trim()
        val earningsCap: BigDecimal? = if (trimmedEarningsCap.isEmpty()) {
            null
        } else {
            val parsed = trimmedEarningsCap.toBigDecimalOrNull()
            if (parsed == null || parsed <= BigDecimal.ZERO) {
                return false
            }
            parsed
        }

        viewModelScope.launch {
            withContext(ioDispatcher) {
                repository.upsertSavingsGoal(
                    existingGoal.copy(
                        name = normalizedName,
                        targetAmount = targetAmount,
                        dailyEarningsCap = earningsCap,
                        status = statusAfterTargetChange(existingGoal, targetAmount)
                    )
                )
            }
            refresh()
        }
        return true
    }

    // Pays $1 per streak day into the habit's own goal, falling back to the pinned goal when the habit is
    // unassigned or its goal is no longer active. Daily caps, target overflow and goal completion are
    // decided by planEarnings. A future many-to-many habit-goal model only needs to change how the
    // starting goal is resolved here.
    fun recordEarningsForHabitCompletion(habit: Habit, goal: SavingsGoal?) {
        viewModelScope.launch {
            val newNotices = withContext(ioDispatcher) {
                earningsMutex.withLock {
                    val date = today()
                    val goals = repository.getSavingsGoals()
                    val byId = goals.associateBy { it.id }
                    val active = { g: SavingsGoal? -> g?.takeIf { it.status == SavingsGoalStatus.ACTIVE } }
                    val startGoal = active(goal?.let { byId[it.id] })
                        ?: active(habit.goalId?.let { byId[it] })
                        ?: nextPinnedGoal(goals)
                    val earnedToday = goals
                        .filter { it.status == SavingsGoalStatus.ACTIVE }
                        .associate { it.id to repository.earnedOn(it.id, date) }

                    val plan = planEarnings(BigDecimal(habit.currentStreak), startGoal, goals, earnedToday)
                    if (plan.deposits.isEmpty() && plan.completedGoalIds.isEmpty()) {
                        return@withLock noticesFor(plan, byId)
                    }

                    val updated = goals.associateBy { it.id }.toMutableMap()
                    plan.deposits.forEach { d ->
                        val g = updated.getValue(d.goalId)
                        updated[d.goalId] = g.copy(currentAmount = g.currentAmount + d.amount)
                    }
                    plan.completedGoalIds.forEach { id ->
                        updated[id] = updated.getValue(id).copy(status = SavingsGoalStatus.COMPLETED, pinned = false)
                    }
                    val changedIds = plan.deposits.map { it.goalId }.toSet() + plan.completedGoalIds
                    repository.applyEarnings(
                        updatedGoals = changedIds.map { updated.getValue(it) },
                        deposits = plan.deposits.map {
                            Deposit(
                                goalId = it.goalId,
                                habitId = habit.id,
                                overflowFromGoalId = it.overflowFromGoalId,
                                amount = it.amount,
                                depositedOn = date
                            )
                        }
                    )
                    noticesFor(plan, byId)
                }
            }
            if (newNotices.isNotEmpty()) {
                _notices.value = _notices.value + newNotices
            }
            refresh()
        }
    }

    fun dismissNotice(notice: EarningsNotice) {
        _notices.value = _notices.value - notice
    }

    private fun noticesFor(plan: EarningsPlan, goalsById: Map<Long, SavingsGoal>): List<EarningsNotice> {
        val completed = plan.completedGoalIds.map { id ->
            val overflow = plan.deposits.filter { it.overflowFromGoalId == id }
            EarningsNotice.GoalCompleted(
                goalName = goalsById.getValue(id).name,
                overflowAmount = overflow.fold(BigDecimal.ZERO) { sum, d -> sum + d.amount },
                overflowGoalName = overflow.firstOrNull()?.let { goalsById.getValue(it.goalId).name }
            )
        }
        val capped = plan.cappedGoalIds.mapNotNull { id ->
            val g = goalsById.getValue(id)
            g.dailyEarningsCap?.let { EarningsNotice.DailyCapReached(g.name, it) }
        }
        return completed + capped
    }

    fun percentCompleted(goal: SavingsGoal?): Float {
        if (goal == null) return 0f
        val percent = goal.currentAmount.toDouble() / goal.targetAmount.toDouble()
        return percent.toFloat()
    }

    fun getCurrentSavingsGoal(): SavingsGoal? {
        return _savingsGoals.value.firstOrNull { it.pinned }
    }

    // Keeps stored goals consistent with the money rules:
    //  - an active goal at or past its target is completed (covers data saved before completion existed);
    //  - only active goals may be pinned, and whenever an active goal exists exactly one is pinned,
    //    since unassigned habits pay into it. The pin falls to the oldest active goal.
    private suspend fun normalizeGoals() {
        val goals = repository.getSavingsGoals().map { goal ->
            if (goal.status == SavingsGoalStatus.ACTIVE && goal.currentAmount >= goal.targetAmount) {
                goal.copy(status = SavingsGoalStatus.COMPLETED).also { repository.upsertSavingsGoal(it) }
            } else {
                goal
            }
        }
        goals.filter { it.pinned && it.status != SavingsGoalStatus.ACTIVE }.forEach {
            repository.upsertSavingsGoal(it.copy(pinned = false))
        }
        val active = goals.filter { it.status == SavingsGoalStatus.ACTIVE }
        if (active.isNotEmpty() && active.none { it.pinned }) {
            val oldest = active.minWith(compareBy<SavingsGoal> { it.createdAt }.thenBy { it.id })
            repository.upsertSavingsGoal(oldest.copy(pinned = true))
        }
    }

    // Editing a target can finish a goal (target lowered to what's saved) or reopen one (target raised).
    private fun statusAfterTargetChange(goal: SavingsGoal, newTarget: BigDecimal): SavingsGoalStatus = when {
        goal.status == SavingsGoalStatus.ACTIVE && goal.currentAmount >= newTarget -> SavingsGoalStatus.COMPLETED
        goal.status == SavingsGoalStatus.COMPLETED && goal.currentAmount < newTarget -> SavingsGoalStatus.ACTIVE
        else -> goal.status
    }

    // CODE TO BE DELETED
    // // Unassigned habits pay into the pinned goal, so whenever goals exist exactly one must be pinned.
    // // Covers the first goal ever created, deleting the pinned goal, and older data saved with none pinned.
    // private suspend fun ensureOneGoalPinned() {
    //     val goals = repository.getSavingsGoals()
    //     if (goals.isEmpty() || goals.any { it.pinned }) {
    //         return
    //     }
    //     val oldest = goals.minWith(compareBy<SavingsGoal> { it.createdAt }.thenBy { it.id })
    //     repository.upsertSavingsGoal(oldest.copy(pinned = true))
    // }

    // CODE TO BE DELETED
    // private fun calculateEarnings(currentStreak: Int, earningsCap: BigDecimal?): BigDecimal {
    //     val streakAmount = BigDecimal(currentStreak)
    //     return if (earningsCap == null) {
    //         streakAmount
    //     } else {
    //         minOf(earningsCap, streakAmount)
    //     }
    // }

    companion object {
        fun factory(repository: SavingsGoalRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SavingsGoalViewModel::class.java)) {
                        return SavingsGoalViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
