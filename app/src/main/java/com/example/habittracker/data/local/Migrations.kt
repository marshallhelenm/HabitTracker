package com.example.habittracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `habits` ADD COLUMN `goalId` INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_goalId` ON `habits` (`goalId`)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `deposits` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`goalId` INTEGER NOT NULL, " +
                "`habitId` INTEGER, " +
                "`overflowFromGoalId` INTEGER, " +
                "`amount` TEXT NOT NULL, " +
                "`depositedOn` TEXT NOT NULL, " +
                "`createdAt` TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_deposits_goalId_depositedOn` " +
                "ON `deposits` (`goalId`, `depositedOn`)"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `habits` ADD COLUMN `prevLastCompletedOn` TEXT")
        db.execSQL("ALTER TABLE `habits` ADD COLUMN `prevCurrentStreak` INTEGER")
        db.execSQL("ALTER TABLE `habits` ADD COLUMN `prevLongestStreak` INTEGER")
    }
}
