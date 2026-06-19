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

    fun addHabit(title: String, description: String) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) {
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.upsertHabit(
                    Habit(
                        title = normalizedTitle,
                        description = description.trim()
                    )
                )
            }
            refresh()
        }
    }

    fun quickAddDailyHabit(name: String): Boolean {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) {
            return false
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.upsertHabit(
                    Habit(
                        title = normalizedName
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

    fun markHabitCompletedToday(habit: Habit) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val alreadyCompletedToday = habit.lastCompletedOn == today
            if (alreadyCompletedToday) {
                return@launch
            }

            val newStreak = when (habit.lastCompletedOn) {
                today.minusDays(1) -> habit.currentStreak + 1
                else -> 1
            }

            withContext(Dispatchers.IO) {
                repository.upsertHabit(
                    habit.copy(
                        lastCompletedOn = today,
                        currentStreak = newStreak,
                        longestStreak = maxOf(habit.longestStreak, newStreak)
                    )
                )
            }
            refresh()
        }
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
