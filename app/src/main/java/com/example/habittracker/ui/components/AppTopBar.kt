package com.example.habittracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.habittracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    selectedMenu: String,
    menuExpanded: Boolean,
    onPigClick: () -> Unit,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onGoHome: () -> Unit,
    onGoHabits: () -> Unit,
    onGoGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = { if (title == "Home") {Text(text = "Habit Piggy")} else {
            Text(text = title)
        } },
        navigationIcon = {
            IconButton(onClick = onPigClick) {
                Image(
                    painter = painterResource(id = R.drawable.piggy),
                    contentDescription = "Go to home",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(5.dp)
                )
            }
        },
        actions = {
            MenuButton(onClick = onMenuClick)
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = onDismissMenu
            ) {
                val selectedColor = MaterialTheme.colorScheme.primary
                val defaultColor = MaterialTheme.colorScheme.onSurface

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Home",
                            color = if (selectedMenu == "Home") selectedColor else defaultColor,
                            fontWeight = if (selectedMenu == "Home") FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onGoHome()
                        onDismissMenu()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Habits",
                            color = if (selectedMenu == "Habits") selectedColor else defaultColor,
                            fontWeight = if (selectedMenu == "Habits") FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onGoHabits()
                        onDismissMenu()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Goals",
                            color = if (selectedMenu == "Goals") selectedColor else defaultColor,
                            fontWeight = if (selectedMenu == "Goals") FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onGoGoals()
                        onDismissMenu()
                    }
                )
            }
        }
    )
}