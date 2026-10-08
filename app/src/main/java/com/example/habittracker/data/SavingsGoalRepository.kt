package com.example.habittracker.data

import com.example.habittracker.model.Deposit
import com.example.habittracker.model.SavingsGoal
import java.math.BigDecimal
import java.time.LocalDate

interface SavingsGoalRepository {
    suspend fun getSavingsGoals(): List<SavingsGoal>
    suspend fun getSavingsGoalById(id: Long): SavingsGoal?
    suspend fun upsertSavingsGoal(goal: SavingsGoal)
    // Habits linked to the deleted goal become unassigned (and so pay into the pinned goal).
    suspend fun deleteSavingsGoal(id: Long)

    suspend fun earnedOn(goalId: Long, date: LocalDate): BigDecimal

    // Saves the new goal balances/statuses and logs the deposits atomically.
    suspend fun applyEarnings(updatedGoals: List<SavingsGoal>, deposits: List<Deposit>)

    suspend fun depositsForHabitOn(habitId: Long, date: LocalDate): List<Deposit>
    suspend fun reverseEarnings(updatedGoals: List<SavingsGoal>, depositIds: List<Long>)
}
