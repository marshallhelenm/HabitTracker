package com.example.habittracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habittracker.data.AppSettings

@Composable
fun SettingsScreen(
    appSettings: AppSettings,
    modifier: Modifier = Modifier
) {
    val streakSaverEnabled by appSettings.streakSaverEnabled.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(vertical = 8.dp)) {
        ListItem(
            headlineContent = { Text("Streak saver") },
            supportingContent = {
                Text("If a streak broke because yesterday wasn't checked off, ask on Home whether you did it.")
            },
            trailingContent = {
                Switch(
                    checked = streakSaverEnabled,
                    onCheckedChange = appSettings::setStreakSaverEnabled
                )
            },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
        )
    }
}
