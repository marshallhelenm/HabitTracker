package com.example.habittracker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal = BigDecimal.ZERO,
    val dueDate: LocalDate? = null,
    val dailyEarningsCap: BigDecimal? = null,
    val status: SavingsGoalStatus = SavingsGoalStatus.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val pinned: Boolean = false,
    // Position in the user's chosen order (Home carousel after the pinned goal, Goals screen "My order").
    @ColumnInfo(defaultValue = "0")
    val sortOrder: Int = 0
)

enum class SavingsGoalStatus {
    ACTIVE,
    COMPLETED,
    PAUSED,
    ARCHIVED
}
