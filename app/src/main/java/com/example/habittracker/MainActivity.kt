package com.example.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.data.AppSettings
import com.example.habittracker.data.RoomHabitRepository
import com.example.habittracker.data.RoomSavingsGoalRepository
import com.example.habittracker.data.local.HabitTrackerDatabase
import com.example.habittracker.ui.HabitTrackerApp
import com.example.habittracker.ui.ProvideToday
import com.example.habittracker.ui.theme.HabitTrackerTheme
import com.example.habittracker.viewmodel.HabitViewModel
import com.example.habittracker.viewmodel.SavingsGoalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val database = remember { HabitTrackerDatabase.getInstance(applicationContext) }
            val habitRepository = remember { RoomHabitRepository(database.habitDao()) }
            val savingsGoalRepository = remember { RoomSavingsGoalRepository(database.savingsGoalDao(), database.depositDao()) }
            val appSettings = remember { AppSettings(applicationContext) }

            val habitViewModel: HabitViewModel = viewModel(
                factory = HabitViewModel.factory(habitRepository)
            )
            val savingsGoalViewModel: SavingsGoalViewModel = viewModel(
                factory = SavingsGoalViewModel.factory(savingsGoalRepository)
            )

            HabitTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProvideToday {
                        HabitTrackerApp(
                            habitViewModel = habitViewModel,
                            savingsGoalViewModel = savingsGoalViewModel,
                            appSettings = appSettings
                        )
                    }
                }
            }
        }
    }
}
