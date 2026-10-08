package com.example.habittracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "habits",
    indices = [Index(value = ["goalId"])]
)
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastCompletedOn: LocalDate? = null,
    val tracking: Boolean = true,
    // Null means the habit pays into the pinned goal.
    val goalId: Long? = null,
    val prevLastCompletedOn: LocalDate? = null,
    val prevCurrentStreak: Int? = null,
    val prevLongestStreak: Int? = null
)

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    CUSTOM
}
