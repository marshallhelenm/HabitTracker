package com.example.habittracker.data

import com.example.habittracker.data.local.SavingsGoalDao
import com.example.habittracker.model.SavingsGoal

class RoomSavingsGoalRepository(
    private val savingsGoalDao: SavingsGoalDao
) : SavingsGoalRepository {
    override suspend fun getSavingsGoals(): List<SavingsGoal> = savingsGoalDao.getAll()

    override suspend fun getSavingsGoalById(id: Long): SavingsGoal? = savingsGoalDao.getById(id)

    override suspend fun upsertSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.upsert(goal)
    }

    override suspend fun deleteSavingsGoal(id: Long) {
        savingsGoalDao.deleteById(id)
    }
}
