package com.example.habittracker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
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
    // The streak survives while the habit is done at least once every this many days; 1 means daily.
    @ColumnInfo(defaultValue = "1")
    val intervalDays: Int = 1,
    // Coins earned per streak step: a check-in pays rewardRate × the streak it reaches.
    // For a fixed-reward habit, the flat amount every check-in pays instead.
    @ColumnInfo(defaultValue = "'1'")
    val rewardRate: BigDecimal = BigDecimal.ONE,
    @ColumnInfo(defaultValue = "0")
    val fixedReward: Boolean = false,
    // Only fixed-reward habits can hide their streak. It's still counted, just not shown.
    @ColumnInfo(defaultValue = "1")
    val showStreak: Boolean = true,
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
