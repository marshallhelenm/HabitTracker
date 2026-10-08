package com.example.habittracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

// The current date for anything date-dependent on screen (check marks, streaks). Reading
// LocalDate.now() directly during composition would go stale if the app stays open past midnight.
val LocalToday = compositionLocalOf { LocalDate.now() }

@Composable
fun ProvideToday(content: @Composable () -> Unit) {
    var today by remember { mutableStateOf(LocalDate.now()) }

    // Coming back to the app (possibly days later) picks up the new date.
    LifecycleResumeEffect(Unit) {
        today = LocalDate.now()
        onPauseOrDispose { }
    }

    // While the app stays open, roll over at midnight.
    LaunchedEffect(today) {
        val untilMidnight = Duration.between(LocalDateTime.now(), today.plusDays(1).atStartOfDay())
        delay(untilMidnight.toMillis().coerceAtLeast(0) + 1_000)
        today = LocalDate.now()
    }

    CompositionLocalProvider(LocalToday provides today, content = content)
}
