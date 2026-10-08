package com.example.habittracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.habittracker.model.Deposit
import com.example.habittracker.model.SavingsGoal
import java.time.LocalDate

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits WHERE goalId = :goalId AND depositedOn = :date")
    suspend fun getForGoalOn(goalId: Long, date: LocalDate): List<Deposit>

    @Query("SELECT * FROM deposits ORDER BY id")
    suspend fun getAll(): List<Deposit>

    @Insert
    suspend fun insert(deposit: Deposit)

    @Upsert
    suspend fun upsertGoal(goal: SavingsGoal)

    // Balances and the deposit log change together or not at all.
    @Transaction
    suspend fun applyEarnings(updatedGoals: List<SavingsGoal>, deposits: List<Deposit>) {
        updatedGoals.forEach { upsertGoal(it) }
        deposits.forEach { insert(it) }
    }

    @Query("SELECT * FROM deposits WHERE habitId = :habitId AND depositedOn = :date")
    suspend fun getForHabitOn(habitId: Long, date: LocalDate): List<Deposit>

    @Query("DELETE FROM deposits WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    // Balances and the deposit log change together or not at all.
    @Transaction
    suspend fun reverseEarnings(updatedGoals: List<SavingsGoal>, depositIds: List<Long>) {
        updatedGoals.forEach { upsertGoal(it) }
        deleteByIds(depositIds)
    }
}
