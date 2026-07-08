package com.example.habittracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.viewmodel.SavingsGoalViewModel

@Composable
fun GoalProgressBar(percentFilled: Float,
                    modifier: Modifier = Modifier
                    ){
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.onSecondaryContainer), shape),
        contentAlignment = Alignment.BottomCenter
    ){
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(percentFilled)
                .background(MaterialTheme.colorScheme.tertiary)
        )
    }
}
