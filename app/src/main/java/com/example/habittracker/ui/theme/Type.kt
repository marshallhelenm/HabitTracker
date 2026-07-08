package com.example.habittracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.habittracker.R

val Modak = FontFamily(Font(R.font.modak_regular))
val CherryBombOne = FontFamily(Font(R.font.cherry_bomb_one_regular))
val Rubik = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium)
)

private val defaultTypography = Typography()

val Typography = defaultTypography.copy(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = Rubik),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = Rubik),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = Rubik),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = Modak),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = Modak),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = Modak),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = CherryBombOne),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = CherryBombOne),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = CherryBombOne),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = Rubik),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = Rubik),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = Rubik),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = Rubik),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = Rubik),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = Rubik)
)
