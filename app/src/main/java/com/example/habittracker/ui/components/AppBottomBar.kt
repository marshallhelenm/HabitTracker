package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

// The app's top-level screens, one per bottom bar item.
enum class AppDestination(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Habits("Habits", Icons.Filled.Checklist),
    Goals("Goals", Icons.Filled.Savings)
}

@Composable
fun AppBottomBar(
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    // The bar is the same cream as the page, so a faint line marks where it starts.
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AppDestination.entries.forEach { destination ->
                NavigationBarItem(
                    selected = destination == current,
                    onClick = { onSelect(destination) },
                    icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                    label = { Text(destination.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}
