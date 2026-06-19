package com.example.habittracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "habits")
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
    val tracking: Boolean = true
)

enum class HabitFrequency {
    DAILY,
    WEEKLY,
    CUSTOM
}
