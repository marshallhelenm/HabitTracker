package com.example.habittracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.habittracker.model.Deposit
import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal

@Database(
    entities = [Habit::class, SavingsGoal::class, Deposit::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HabitTrackerDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun depositDao(): DepositDao

    companion object {
        @Volatile
        private var INSTANCE: HabitTrackerDatabase? = null

        fun getInstance(context: Context): HabitTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HabitTrackerDatabase::class.java,
                    "habit_tracker.db"
                )
                    // CODE TO BE DELETED
                    // .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
