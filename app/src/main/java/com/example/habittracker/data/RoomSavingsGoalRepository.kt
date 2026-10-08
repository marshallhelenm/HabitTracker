package com.example.habittracker.data

import com.example.habittracker.data.local.DepositDao
import com.example.habittracker.data.local.SavingsGoalDao
import com.example.habittracker.model.Deposit
import com.example.habittracker.model.SavingsGoal
import java.math.BigDecimal
import java.time.LocalDate

class RoomSavingsGoalRepository(
    private val savingsGoalDao: SavingsGoalDao,
    private val depositDao: DepositDao
) : SavingsGoalRepository {
    override suspend fun getSavingsGoals(): List<SavingsGoal> = savingsGoalDao.getAll()

    override suspend fun getSavingsGoalById(id: Long): SavingsGoal? = savingsGoalDao.getById(id)

    override suspend fun upsertSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.upsert(goal)
    }

    override suspend fun deleteSavingsGoal(id: Long) {
        savingsGoalDao.deleteAndUnassignHabits(id)
    }

    override suspend fun earnedOn(goalId: Long, date: LocalDate): BigDecimal =
        depositDao.getForGoalOn(goalId, date).fold(BigDecimal.ZERO) { sum, deposit -> sum + deposit.amount }

    override suspend fun applyEarnings(updatedGoals: List<SavingsGoal>, deposits: List<Deposit>) {
        depositDao.applyEarnings(updatedGoals, deposits)
    }

    override suspend fun depositsForHabitOn(habitId: Long, date: LocalDate): List<Deposit> =
        depositDao.getForHabitOn(habitId, date)

    override suspend fun reverseEarnings(updatedGoals: List<SavingsGoal>, depositIds: List<Long>) {
        depositDao.reverseEarnings(updatedGoals, depositIds)
    }
}
