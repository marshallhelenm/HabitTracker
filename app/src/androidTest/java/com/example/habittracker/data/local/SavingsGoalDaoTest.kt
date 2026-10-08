package com.example.habittracker.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.habittracker.model.Habit
import com.example.habittracker.model.SavingsGoal
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class SavingsGoalDaoTest {
    private lateinit var db: HabitTrackerDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            HabitTrackerDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun deletingGoal_unassignsOnlyItsHabits() = runBlocking {
        val goals = db.savingsGoalDao()
        val habits = db.habitDao()
        goals.upsert(SavingsGoal(id = 1, name = "Bike", targetAmount = BigDecimal("300")))
        goals.upsert(SavingsGoal(id = 2, name = "Trip", targetAmount = BigDecimal("900")))
        habits.upsert(Habit(id = 10, title = "Run", goalId = 1))
        habits.upsert(Habit(id = 11, title = "Read", goalId = 2))

        goals.deleteAndUnassignHabits(1)

        assertNull(goals.getById(1))
        assertNull(habits.getById(10)!!.goalId)
        assertEquals(2L, habits.getById(11)!!.goalId)
    }
}
