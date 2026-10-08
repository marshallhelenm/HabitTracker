package com.example.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.habittracker.ui.theme.CoinGoldDark
import com.example.habittracker.ui.theme.CoinFace
import com.example.habittracker.ui.theme.CoinHighlight
import com.example.habittracker.ui.theme.CoinShade

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
            // Outlined so the bar's full length stays visible against pale cards.
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                // Keep any non-zero progress visible as at least a round nub; tiny early savings otherwise vanish.
                .then(if (fraction > 0f) Modifier.widthIn(min = height) else Modifier)
                .fillMaxWidth(fraction)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(listOf(CoinHighlight, CoinFace, CoinShade))
                )
                // The dark rim carries the 3:1 contrast against the track, so the inside can stay bright.
                .border(1.5.dp, CoinGoldDark, CircleShape)
        )
    }
}
