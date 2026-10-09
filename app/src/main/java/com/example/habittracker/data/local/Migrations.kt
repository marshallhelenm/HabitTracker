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

// Replaces the `frequency` enum with `intervalDays`. SQLite on older Android can't drop a column, so
// the table is rebuilt. WEEKLY becomes every 7 days; CUSTOM had no schedule and behaved as daily.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `habits_new` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`intervalDays` INTEGER NOT NULL DEFAULT 1, " +
                "`currentStreak` INTEGER NOT NULL, " +
                "`longestStreak` INTEGER NOT NULL, " +
                "`createdAt` TEXT NOT NULL, " +
                "`lastCompletedOn` TEXT, " +
                "`tracking` INTEGER NOT NULL, " +
                "`goalId` INTEGER, " +
                "`prevLastCompletedOn` TEXT, " +
                "`prevCurrentStreak` INTEGER, " +
                "`prevLongestStreak` INTEGER)"
        )
        db.execSQL(
            "INSERT INTO `habits_new` (`id`, `title`, `description`, `intervalDays`, `currentStreak`, " +
                "`longestStreak`, `createdAt`, `lastCompletedOn`, `tracking`, `goalId`, " +
                "`prevLastCompletedOn`, `prevCurrentStreak`, `prevLongestStreak`) " +
                "SELECT `id`, `title`, `description`, CASE `frequency` WHEN 'WEEKLY' THEN 7 ELSE 1 END, " +
                "`currentStreak`, `longestStreak`, `createdAt`, `lastCompletedOn`, `tracking`, `goalId`, " +
                "`prevLastCompletedOn`, `prevCurrentStreak`, `prevLongestStreak` FROM `habits`"
        )
        db.execSQL("DROP TABLE `habits`")
        db.execSQL("ALTER TABLE `habits_new` RENAME TO `habits`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_goalId` ON `habits` (`goalId`)")
    }
}

// Adds the user's goal order. Existing goals are numbered oldest to newest, matching the carousel
// order they had before.
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `savings_goals` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "UPDATE `savings_goals` SET `sortOrder` = (" +
                "SELECT COUNT(*) FROM `savings_goals` AS `older` " +
                "WHERE `older`.`createdAt` < `savings_goals`.`createdAt` " +
                "OR (`older`.`createdAt` = `savings_goals`.`createdAt` AND `older`.`id` < `savings_goals`.`id`))"
        )
    }
}
