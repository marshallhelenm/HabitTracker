package com.example.habittracker.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HabitTrackerDatabase::class.java
    )

    @Test
    fun migrate2To3_keepsExistingRowsAndLeavesHabitsUnassigned() {
        helper.createDatabase(testDb, 2).apply {
            execSQL(
                """
                INSERT INTO habits (id, title, description, frequency, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking)
                VALUES (7, 'Stretch', 'Morning routine', 'DAILY', 4, 9, '2026-09-01T08:30', '2026-10-06', 1)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO savings_goals (id, name, targetAmount, currentAmount, dueDate, dailyEarningsCap, status, createdAt, pinned)
                VALUES (3, 'Danger Noodle', '2000', '13.00', NULL, '5', 'ACTIVE', '2026-09-02T10:00', 1)
                """.trimIndent()
            )
            close()
        }

        // Validates the migrated schema against 3.json, so a mismatch with the entity fails here.
        val db = helper.runMigrationsAndValidate(testDb, 3, true, MIGRATION_2_3)

        db.query("SELECT title, description, frequency, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking, goalId FROM habits WHERE id = 7").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Stretch", c.getString(0))
            assertEquals("Morning routine", c.getString(1))
            assertEquals("DAILY", c.getString(2))
            assertEquals(4, c.getInt(3))
            assertEquals(9, c.getInt(4))
            assertEquals("2026-09-01T08:30", c.getString(5))
            assertEquals("2026-10-06", c.getString(6))
            assertEquals(1, c.getInt(7))
            assertTrue("goalId should be null after migration", c.isNull(8))
        }

        db.query("SELECT name, targetAmount, currentAmount, dailyEarningsCap, status, pinned FROM savings_goals WHERE id = 3").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Danger Noodle", c.getString(0))
            assertEquals("2000", c.getString(1))
            assertEquals("13.00", c.getString(2))
            assertEquals("5", c.getString(3))
            assertEquals("ACTIVE", c.getString(4))
            assertEquals(1, c.getInt(5))
        }
    }

    @Test
    fun migrate3To4_addsEmptyDepositsTableAndKeepsRows() {
        helper.createDatabase(testDb, 3).apply {
            execSQL(
                """
                INSERT INTO habits (id, title, description, frequency, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking, goalId)
                VALUES (7, 'Stretch', '', 'DAILY', 4, 9, '2026-09-01T08:30', '2026-10-06', 1, 3)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO savings_goals (id, name, targetAmount, currentAmount, dueDate, dailyEarningsCap, status, createdAt, pinned)
                VALUES (3, 'Danger Noodle', '2000', '412.50', NULL, '5', 'ACTIVE', '2026-09-02T10:00', 1)
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 4, true, MIGRATION_3_4)

        db.query("SELECT title, goalId FROM habits WHERE id = 7").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Stretch", c.getString(0))
            assertEquals(3, c.getInt(1))
        }
        db.query("SELECT currentAmount, pinned FROM savings_goals WHERE id = 3").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("412.50", c.getString(0))
            assertEquals(1, c.getInt(1))
        }
        db.query("SELECT COUNT(*) FROM deposits").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(0, c.getInt(0))
        }
    }

    @Test
    fun migrate4To5_addsEmptySnapshotColumnsAndKeepsRows() {
        helper.createDatabase(testDb, 4).apply {
            execSQL(
                """
                INSERT INTO habits (id, title, description, frequency, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking, goalId)
                VALUES (7, 'Stretch', 'Morning routine', 'WEEKLY', 4, 9, '2026-09-01T08:30', '2026-10-06', 0, 3)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO savings_goals (id, name, targetAmount, currentAmount, dueDate, dailyEarningsCap, status, createdAt, pinned)
                VALUES (3, 'Danger Noodle', '2000', '412.50', NULL, '5', 'ACTIVE', '2026-09-02T10:00', 1)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO deposits (id, goalId, habitId, overflowFromGoalId, amount, depositedOn, createdAt)
                VALUES (11, 3, 7, NULL, '4', '2026-10-06', '2026-10-06T07:45')
                """.trimIndent()
            )
            close()
        }

        // Validates against 5.json, so a mismatch between the migration and the entity fails here.
        val db = helper.runMigrationsAndValidate(testDb, 5, true, MIGRATION_4_5)

        db.query(
            "SELECT title, description, frequency, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking, goalId, " +
                "prevLastCompletedOn, prevCurrentStreak, prevLongestStreak FROM habits WHERE id = 7"
        ).use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Stretch", c.getString(0))
            assertEquals("Morning routine", c.getString(1))
            assertEquals("WEEKLY", c.getString(2))
            assertEquals(4, c.getInt(3))
            assertEquals(9, c.getInt(4))
            assertEquals("2026-09-01T08:30", c.getString(5))
            assertEquals("2026-10-06", c.getString(6))
            assertEquals(0, c.getInt(7))
            assertEquals(3, c.getInt(8))
            // No snapshot exists for completions made before the upgrade.
            assertTrue("prevLastCompletedOn should be null", c.isNull(9))
            assertTrue("prevCurrentStreak should be null", c.isNull(10))
            assertTrue("prevLongestStreak should be null", c.isNull(11))
        }
        db.query("SELECT currentAmount, pinned FROM savings_goals WHERE id = 3").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("412.50", c.getString(0))
            assertEquals(1, c.getInt(1))
        }
        db.query("SELECT goalId, habitId, amount, depositedOn FROM deposits WHERE id = 11").use { c ->
            assertTrue("existing deposits must survive", c.moveToFirst())
            assertEquals(3, c.getInt(0))
            assertEquals(7, c.getInt(1))
            assertEquals("4", c.getString(2))
            assertEquals("2026-10-06", c.getString(3))
        }
    }

    @Test
    fun migrate2To5_chainUpgradesAnOldInstall() {
        helper.createDatabase(testDb, 2).apply {
            execSQL(
                """
                INSERT INTO habits (id, title, description, frequency, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking)
                VALUES (1, 'PT', '', 'DAILY', 4, 4, '2026-06-19T15:09:42.130054', '2026-07-08', 1)
                """.trimIndent()
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 5, true, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)

        db.query("SELECT title, currentStreak, lastCompletedOn, goalId, prevCurrentStreak FROM habits WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("PT", c.getString(0))
            assertEquals(4, c.getInt(1))
            assertEquals("2026-07-08", c.getString(2))
            assertTrue(c.isNull(3))
            assertTrue(c.isNull(4))
        }
        db.query("SELECT COUNT(*) FROM deposits").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(0, c.getInt(0))
        }
    }
}
