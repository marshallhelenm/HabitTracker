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
    fun migrate1To2_givesExistingHabitsADollarRewardRate() {
        helper.createDatabase(testDb, 1).apply {
            execSQL(
                """
                INSERT INTO habits (id, title, description, intervalDays, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking, goalId, prevLastCompletedOn, prevCurrentStreak, prevLongestStreak)
                VALUES (7, 'Stretch', 'Morning routine', 3, 4, 9, '2026-09-01T08:30', '2026-10-06', 1, 2, '2026-10-04', 3, 9)
                """.trimIndent()
            )
            close()
        }

        // Validates against 2.json, so a mismatch between the migration and the entity fails here.
        val db = helper.runMigrationsAndValidate(testDb, 2, true, MIGRATION_1_2)

        db.query(
            "SELECT title, description, intervalDays, currentStreak, longestStreak, createdAt, lastCompletedOn, " +
                "tracking, goalId, prevLastCompletedOn, prevCurrentStreak, prevLongestStreak, rewardRate " +
                "FROM habits WHERE id = 7"
        ).use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Stretch", c.getString(0))
            assertEquals("Morning routine", c.getString(1))
            assertEquals(3, c.getInt(2))
            assertEquals(4, c.getInt(3))
            assertEquals(9, c.getInt(4))
            assertEquals("2026-09-01T08:30", c.getString(5))
            assertEquals("2026-10-06", c.getString(6))
            assertEquals(1, c.getInt(7))
            assertEquals(2, c.getInt(8))
            assertEquals("2026-10-04", c.getString(9))
            assertEquals(3, c.getInt(10))
            assertEquals(9, c.getInt(11))
            assertEquals("1", c.getString(12))
        }
    }
}
