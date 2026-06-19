package com.example.habittracker.data

import com.example.habittracker.model.SavingsGoal

interface SavingsGoalRepository {
    suspend fun getSavingsGoals(): List<SavingsGoal>
    suspend fun getSavingsGoalById(id: Long): SavingsGoal?
    suspend fun upsertSavingsGoal(goal: SavingsGoal)
    suspend fun deleteSavingsGoal(id: Long)
}
