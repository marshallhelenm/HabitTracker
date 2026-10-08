package com.example.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.habittracker.ui.theme.RoyalGold

@Composable
fun GoalProgressBar(
    percentFilled: Float,
    modifier: Modifier = Modifier,
    height: Dp = 20.dp
) {
    val fraction = percentFilled.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                // Keep any non-zero progress visible as at least a round nub; tiny early savings otherwise vanish.
                .then(if (fraction > 0f) Modifier.widthIn(min = height) else Modifier)
                .fillMaxWidth(fraction)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(RoyalGold, MaterialTheme.colorScheme.tertiary)
                    )
                )
        )
    }
}
