package com.example.habittracker.ui.components

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Material's arrows are drawn with a thin stroke; these use a heavy stroke with round caps and
// joins to match the app's chubby, rounded style. The colour comes from Icon's tint.
private fun chunkyArrow(name: String, pointsDown: Boolean): ImageVector {
    val tipY = if (pointsDown) 19f else 5f
    val tailY = if (pointsDown) 5f else 19f
    val wingY = if (pointsDown) 13f else 11f
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 3.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, tailY)
        lineTo(12f, tipY)
        moveTo(6f, wingY)
        lineTo(12f, tipY)
        lineTo(18f, wingY)
    }.build()
}

val ChunkyArrowDown: ImageVector by lazy { chunkyArrow("ChunkyArrowDown", pointsDown = true) }
val ChunkyArrowUp: ImageVector by lazy { chunkyArrow("ChunkyArrowUp", pointsDown = false) }
