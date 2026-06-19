package com.example.habittracker.data.local

import androidx.room.Dao
import androidx.room.Query
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
}
