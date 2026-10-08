package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.SavingsGoalRepository
import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class SavingsGoalViewModel(
    private val repository: SavingsGoalRepository
) : ViewModel() {
    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals.asStateFlow()
    val pinnedGoal: StateFlow<SavingsGoal?> = _savingsGoals
        .map { goals -> goals.firstOrNull { it.pinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _savingsGoals.value = withContext(Dispatchers.IO) {
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
            withContext(Dispatchers.IO) {
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

    fun deleteSavingsGoal(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteSavingsGoal(id)
            }
            refresh()
        }
    }

    fun pinSavingsGoal(id: Long) {
        viewModelScope.launch {
            val goals = _savingsGoals.value
            withContext(Dispatchers.IO) {
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
            withContext(Dispatchers.IO) {
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

    // Today: resolve the single pinned goal. Later, once a habit-goal join model exists,
    // this can look up the goal(s) linked to `habit.id` instead — callers won't need to change.
    fun recordEarningsForHabitCompletion(habit: Habit, goal: SavingsGoal?) {
        val targetGoal = goal ?: getCurrentSavingsGoal() ?: return
        val deposit = calculateEarnings(habit.currentStreak, targetGoal.dailyEarningsCap)
        if (deposit <= BigDecimal.ZERO) {
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.upsertSavingsGoal(targetGoal.copy(currentAmount = targetGoal.currentAmount + deposit))
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
