package com.example.habittracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Adds each habit's reward rate. Existing habits keep earning 1 coin per streak step, as before.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `habits` ADD COLUMN `rewardRate` TEXT NOT NULL DEFAULT '1'")
    }
}

// Adds fixed-reward habits and the option to hide their streak. Existing habits keep growing with their streak.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `habits` ADD COLUMN `fixedReward` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `habits` ADD COLUMN `showStreak` INTEGER NOT NULL DEFAULT 1")
    }
}
