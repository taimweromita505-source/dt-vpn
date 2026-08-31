package com.taimwe.vpn.ui.theme

import androidx.compose.ui.graphics.Color

val NeonBlue = Color(0xFF00D4FF)
val NeonPink = Color(0xFFFF006E)
val NeonPurple = Color(0xFF8338EC)
val NeonGreen = Color(0xFF06D6A0)
val DarkBackground = Color(0xFF0A0E27)
val CardBackground = Color(0xFF151932)
val SurfaceVariant = Color(0xFF1E2340)

data class AppTheme(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val isDark: Boolean = true
)

val RainbowMatrixTheme = AppTheme(
    name = "Rainbow Matrix",
    primary = NeonGreen,
    secondary = NeonBlue,
    tertiary = NeonPurple,
    background = DarkBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

val allThemes = listOf(RainbowMatrixTheme)
