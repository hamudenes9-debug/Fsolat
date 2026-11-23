package com.hamudenes.fsolat.ui.theme

import androidx.compose.ui.graphics.Color

// Pure monochrome palette. Light = ink on paper, Dark = paper on ink.
val Ink = Color(0xFF0A0A0A)
val Paper = Color(0xFFFFFFFF)
val SoftPaper = Color(0xFFF4F4F2)
val Graphite = Color(0xFF161616)
val GraphiteSoft = Color(0xFF1E1E1E)
val Mist = Color(0xFFE6E6E3)
val Smoke = Color(0xFF8A8A86)
val Fog = Color(0xFFB9B9B4)
val Dim = Color(0xFF5A5A57)

// Semantic tokens used across screens. They are resolved by the theme.
data class MonoColors(
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val onBackground: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val invertedBackground: Color,
    val invertedContent: Color,
    val invertedMuted: Color,
    val accent: Color
)

val LightMono = MonoColors(
    background = Color(0xFFF7F7F5),
    surface = Paper,
    surfaceHigh = SoftPaper,
    onBackground = Ink,
    onSurfaceVariant = Dim,
    outline = Mist,
    invertedBackground = Ink,
    invertedContent = Paper,
    invertedMuted = Color(0xFFB5B5B0),
    accent = Ink
)

val DarkMono = MonoColors(
    background = Color(0xFF000000),
    surface = Color(0xFF0D0D0D),
    surfaceHigh = Color(0xFF171717),
    onBackground = Color(0xFFF5F5F2),
    onSurfaceVariant = Color(0xFF9A9A96),
    outline = Color(0xFF262624),
    invertedBackground = Color(0xFFF5F5F2),
    invertedContent = Ink,
    invertedMuted = Color(0xFF4A4A47),
    accent = Color(0xFFF5F5F2)
)
