package com.example.habittracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `habits` ADD COLUMN `goalId` INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_goalId` ON `habits` (`goalId`)")
    }
}
