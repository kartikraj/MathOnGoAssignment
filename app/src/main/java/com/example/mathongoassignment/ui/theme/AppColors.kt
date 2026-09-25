package com.example.mathongoassignment.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val background: Color,
    val appBar: Color,
    val onAppBar: Color,
    val bottomBar: Color,
    val card: Color,
    val cardBorder: Color,
    val divider: Color,
    val badgeBackground: Color,
    val badgeText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textSource: Color,
    val accent: Color,
    val accentDisabled: Color,
    val accentSurface: Color,
    val onAccent: Color,
    val correct: Color,
    val correctSurface: Color,
    val incorrect: Color,
    val incorrectSurface: Color,
    val isDark: Boolean,
)

val LightAppColors = AppColors(
    background = Neutral0,
    appBar = AppBarDark,
    onAppBar = Neutral0,
    bottomBar = Neutral50,
    card = Neutral0,
    cardBorder = Neutral200,
    divider = Neutral200,
    badgeBackground = Neutral100,
    badgeText = Neutral500,
    textPrimary = Neutral900,
    textSecondary = Neutral500,
    textSource = Neutral600,
    accent = Blue600,
    accentDisabled = Blue200,
    accentSurface = BlueSurface,
    onAccent = Neutral0,
    correct = Green600,
    correctSurface = GreenSurface,
    incorrect = Red600,
    incorrectSurface = RedSurface,
    isDark = false,
)

val DarkAppColors = AppColors(
    background = DarkBackground,
    appBar = DarkSurface,
    onAppBar = DarkTextPrimary,
    bottomBar = DarkSurface,
    card = DarkSurfaceElevated,
    cardBorder = DarkBorder,
    divider = DarkBorder,
    badgeBackground = DarkBorder,
    badgeText = DarkTextSecondary,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textSource = DarkTextSecondary,
    accent = BlueDark,
    accentDisabled = BlueDarkDisabled,
    accentSurface = BlueDarkSurface,
    onAccent = Color.White,
    correct = GreenDark,
    correctSurface = GreenDarkSurface,
    incorrect = RedDark,
    incorrectSurface = RedDarkSurface,
    isDark = true,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
