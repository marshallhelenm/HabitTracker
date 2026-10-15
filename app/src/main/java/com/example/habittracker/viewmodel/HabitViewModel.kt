package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.model.Habit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate

class HabitViewModel(
    private val repository: HabitRepository
) : ViewModel() {
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _habits.value = withContext(Dispatchers.IO) {
                repository.getHabits()
            }
        }
    }

    fun addHabit(
        title: String,
        description: String,
        intervalDays: Int = 1,
        rewardRate: BigDecimal = BigDecimal.ONE,
        fixedReward: Boolean = false,
        showStreak: Boolean = true,
        goalId: Long? = null
    ) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) {
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.upsertHabit(
                    Habit(
                        title = normalizedTitle,
                        description = description.trim(),
                        intervalDays = intervalDays,
                        rewardRate = rewardRate,
                        fixedReward = fixedReward,
                        showStreak = showStreak || !fixedReward,
                        goalId = goalId
                    )
                )
            }
            refresh()
        }
    }

    fun quickAddDailyHabit(name: String, goalId: Long? = null): Boolean {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) {
            return false
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.upsertHabit(
                    Habit(
                        title = normalizedName,
                        goalId = goalId
                    )
                )
            }
            refresh()
        }
        return true
    }

    fun updateHabit(habit: Habit) {
        val normalizedTitle = habit.title.trim()
        if (normalizedTitle.isEmpty()) {
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.upsertHabit(
                    habit.copy(
                        title = normalizedTitle,
                        description = habit.description.trim()
                    )
                )
            }
            refresh()
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteHabit(id)
            }
            refresh()
        }
    }

    fun markHabitCompletedToday(habit: Habit): Habit? {
        val today = LocalDate.now()
        val alreadyCompletedToday = habit.lastCompletedOn == today
        if (alreadyCompletedToday) {
            return null
        }

        val newStreak = streakAfterCompletion(habit, today)

        val updatedHabit = habit.copy(
            prevCurrentStreak = habit.currentStreak,
            prevLongestStreak = habit.longestStreak,
            prevLastCompletedOn = habit.lastCompletedOn,
            lastCompletedOn = today,
            currentStreak = newStreak,
            longestStreak = maxOf(habit.longestStreak, newStreak)
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.upsertHabit(updatedHabit)
            }
            refresh()
        }
        return updatedHabit
    }

    fun unMarkHabitCompletedToday(habit: Habit): Habit? {
        val today = LocalDate.now()
        val alreadyCompletedToday = habit.lastCompletedOn == today
        if (!alreadyCompletedToday) {
            return null
        }
        val hasSnapshot = habit.prevCurrentStreak != null

        val restored = habit.copy(
            lastCompletedOn = if (hasSnapshot) habit.prevLastCompletedOn
                                else if (habit.currentStreak >=2) today.minusDays(1) else null,
            currentStreak = habit.prevCurrentStreak ?: (habit.currentStreak - 1).coerceAtLeast(0),
            longestStreak = habit.prevLongestStreak ?: habit.longestStreak,
            prevLastCompletedOn = null,
            prevCurrentStreak = null,
            prevLongestStreak = null
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.upsertHabit(restored)
            }
            refresh()
        }
        return restored
    }

    companion object {
        fun factory(repository: HabitRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
                        return HabitViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
