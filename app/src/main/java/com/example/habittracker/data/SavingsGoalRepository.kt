package com.example.habittracker.data

import com.example.habittracker.model.SavingsGoal

interface SavingsGoalRepository {
    suspend fun getSavingsGoals(): List<SavingsGoal>
    suspend fun getSavingsGoalById(id: Long): SavingsGoal?
    suspend fun upsertSavingsGoal(goal: SavingsGoal)
    // Habits linked to the deleted goal become unassigned (and so pay into the pinned goal).
    suspend fun deleteSavingsGoal(id: Long)
}
