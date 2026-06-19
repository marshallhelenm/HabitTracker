package com.example.habittracker.data

import com.example.habittracker.model.Habit

interface HabitRepository {
    suspend fun getHabits(): List<Habit>
    suspend fun getHabitById(id: Long): Habit?
    suspend fun upsertHabit(habit: Habit)
    suspend fun deleteHabit(id: Long)
}
