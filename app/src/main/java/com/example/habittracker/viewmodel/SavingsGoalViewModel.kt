package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.SavingsGoalRepository
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

class SavingsGoalViewModel(
    private val repository: SavingsGoalRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals.asStateFlow()
    val pinnedGoal: StateFlow<SavingsGoal?> = _savingsGoals
        .map { goals -> goals.firstOrNull { it.pinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Home carousel order: pinned first, then oldest to newest. Archived goals stay off Home.
    val carouselGoals: StateFlow<List<SavingsGoal>> = _savingsGoals
        .map { goals ->
            goals
                .filter { it.status != SavingsGoalStatus.ARCHIVED }
                .sortedWith(
                    compareByDescending<SavingsGoal> { it.pinned }
                        .thenBy { it.createdAt }
                        .thenBy { it.id }
                )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        refresh()
    }

    private val earningsMutex = Mutex()

    fun refresh() {
        viewModelScope.launch {
            _savingsGoals.value = withContext(ioDispatcher) {
                ensureOneGoalPinned()
                repository.getSavingsGoals()
                    .sortedWith(
                        compareByDescending<SavingsGoal> { it.pinned }
                            .thenBy { it.id }
                    )
            }
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
                        dailyEarningsCap = earningsCap
                    )
                )
            }
            refresh()
        }
        return true
    }

    // Pays into the habit's own goal, falling back to the pinned goal for unassigned habits.
    // A future many-to-many habit-goal model only needs to change how the target is resolved here.
    fun recordEarningsForHabitCompletion(habit: Habit, goal: SavingsGoal?) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                // Serialized and read fresh from storage so back-to-back completions can't overwrite each other's deposits.
                earningsMutex.withLock {
                    val targetGoal = goal?.let { repository.getSavingsGoalById(it.id) }
                        ?: habit.goalId?.let { repository.getSavingsGoalById(it) }
                        ?: repository.getSavingsGoals().firstOrNull { it.pinned }
                        ?: return@withLock
                    val deposit = calculateEarnings(habit.currentStreak, targetGoal.dailyEarningsCap)
                    if (deposit <= BigDecimal.ZERO) {
                        return@withLock
                    }
                    repository.upsertSavingsGoal(targetGoal.copy(currentAmount = targetGoal.currentAmount + deposit))
                }
            }
            refresh()
        }
    }

    fun percentCompleted(goal: SavingsGoal?): Float {
        if (goal == null) return 0f
        val percent = goal.currentAmount.toDouble() / goal.targetAmount.toDouble()
        return percent.toFloat()
    }

    fun getCurrentSavingsGoal(): SavingsGoal? {
        return _savingsGoals.value.firstOrNull { it.pinned }
    }

    // Unassigned habits pay into the pinned goal, so whenever goals exist exactly one must be pinned.
    // Covers the first goal ever created, deleting the pinned goal, and older data saved with none pinned.
    private suspend fun ensureOneGoalPinned() {
        val goals = repository.getSavingsGoals()
        if (goals.isEmpty() || goals.any { it.pinned }) {
            return
        }
        val oldest = goals.minWith(compareBy<SavingsGoal> { it.createdAt }.thenBy { it.id })
        repository.upsertSavingsGoal(oldest.copy(pinned = true))
    }

    private fun calculateEarnings(currentStreak: Int, earningsCap: BigDecimal?): BigDecimal {
        val streakAmount = BigDecimal(currentStreak)
        return if (earningsCap == null) {
            streakAmount
        } else {
            minOf(earningsCap, streakAmount)
        }
    }

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
