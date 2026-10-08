package com.example.habittracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "deposits",
    indices = [Index(value = ["goalId", "depositedOn"])]
)
data class Deposit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    // Kept as a plain id (no foreign key) so history survives the habit being deleted.
    val habitId: Long?,
    // Set when this deposit is leftover from another goal that just hit its target.
    val overflowFromGoalId: Long? = null,
    val amount: BigDecimal,
    val depositedOn: LocalDate,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
