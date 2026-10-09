package com.example.habittracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Adds each habit's reward rate. Existing habits keep earning $1 per streak step, as before.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `habits` ADD COLUMN `rewardRate` TEXT NOT NULL DEFAULT '1'")
    }
}
