package com.example.habittracker.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

// The habits whose streak-saver prompt was answered No, and the day that happened. A No only
// lasts for that day, since by tomorrow the missed day can no longer be saved anyway.
data class DeclinedStreakSaves(val date: LocalDate? = null, val habitIds: Set<Long> = emptySet()) {
    fun contains(habitId: Long, today: LocalDate): Boolean = date == today && habitId in habitIds
}

// Small app preferences, kept in SharedPreferences rather than the database since they're a few
// independent values with no relations or history.
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _streakSaverEnabled = MutableStateFlow(prefs.getBoolean(KEY_STREAK_SAVER, true))
    val streakSaverEnabled: StateFlow<Boolean> = _streakSaverEnabled.asStateFlow()

    private val _declinedStreakSaves = MutableStateFlow(
        DeclinedStreakSaves(
            date = prefs.getString(KEY_DECLINED_ON, null)?.let(LocalDate::parse),
            habitIds = prefs.getStringSet(KEY_DECLINED_IDS, emptySet()).orEmpty().map(String::toLong).toSet()
        )
    )
    val declinedStreakSaves: StateFlow<DeclinedStreakSaves> = _declinedStreakSaves.asStateFlow()

    fun setStreakSaverEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_STREAK_SAVER, enabled) }
        _streakSaverEnabled.value = enabled
    }

    fun declineStreakSave(habitId: Long, today: LocalDate) {
        val current = _declinedStreakSaves.value
        // Yesterday's answers no longer matter, so starting a new day replaces the set.
        val ids = if (current.date == today) current.habitIds + habitId else setOf(habitId)
        prefs.edit {
            putString(KEY_DECLINED_ON, today.toString())
            putStringSet(KEY_DECLINED_IDS, ids.map(Long::toString).toSet())
        }
        _declinedStreakSaves.value = DeclinedStreakSaves(today, ids)
    }

    private companion object {
        const val KEY_STREAK_SAVER = "streak_saver_enabled"
        const val KEY_DECLINED_ON = "streak_saver_declined_on"
        const val KEY_DECLINED_IDS = "streak_saver_declined_ids"
    }
}
