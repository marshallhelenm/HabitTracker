package com.example.habittracker.viewmodel

import com.example.habittracker.data.SavingsGoalRepository
import com.example.habittracker.model.SavingsGoal

class FakeSavingsGoalRepository(initial: List<SavingsGoal> = emptyList()) : SavingsGoalRepository {
    private val goals = initial.associateBy { it.id }.toMutableMap()
    private var nextId = (goals.keys.maxOrNull() ?: 0L) + 1

    override suspend fun getSavingsGoals(): List<SavingsGoal> = goals.values.toList()

    override suspend fun getSavingsGoalById(id: Long): SavingsGoal? = goals[id]

    override suspend fun upsertSavingsGoal(goal: SavingsGoal) {
        val stored = if (goal.id == 0L) goal.copy(id = nextId++) else goal
        goals[stored.id] = stored
    }

    override suspend fun deleteSavingsGoal(id: Long) {
        goals.remove(id)
    }
}
