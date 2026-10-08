package com.example.habittracker.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.habittracker.model.SavingsGoal

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY createdAt DESC")
    suspend fun getAll(): List<SavingsGoal>

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavingsGoal?

    @Upsert
    suspend fun upsert(goal: SavingsGoal)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE habits SET goalId = NULL WHERE goalId = :goalId")
    suspend fun unassignHabitsFromGoal(goalId: Long)

    @Query("DELETE FROM deposits WHERE goalId = :goalId")
    suspend fun deleteDepositsForGoal(goalId: Long)

    // One transaction so a habit never points at a goal that no longer exists,
    // and no deposit history is left behind for it.
    @Transaction
    suspend fun deleteAndUnassignHabits(id: Long) {
        unassignHabitsFromGoal(id)
        deleteDepositsForGoal(id)
        deleteById(id)
    }
}
