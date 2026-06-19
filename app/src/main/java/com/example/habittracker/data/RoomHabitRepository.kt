package com.example.habittracker.data

import com.example.habittracker.data.local.HabitDao
import com.example.habittracker.model.Habit

class RoomHabitRepository(
    private val habitDao: HabitDao
) : HabitRepository {
    override suspend fun getHabits(): List<Habit> = habitDao.getAll()

    override suspend fun getHabitById(id: Long): Habit? = habitDao.getById(id)

    override suspend fun upsertHabit(habit: Habit) {
        habitDao.upsert(habit)
    }

    override suspend fun deleteHabit(id: Long) {
        habitDao.deleteById(id)
    }
}
