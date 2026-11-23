package com.hamudenes.fsolat.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.hamudenes.fsolat.data.ThemeMode

val LocalMono = staticCompositionLocalOf { LightMono }

private fun buildScheme(mono: MonoColors, dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = mono.invertedBackground,
        onPrimary = mono.invertedContent,
        primaryContainer = if (dark) Color(0xFF1F1F1D) else Color(0xFFEDEDE9),
        onPrimaryContainer = mono.onBackground,
        secondary = mono.onSurfaceVariant,
        onSecondary = mono.background,
        secondaryContainer = mono.surfaceHigh,
        onSecondaryContainer = mono.onBackground,
        tertiary = mono.onSurfaceVariant,
        onTertiary = mono.background,
        background = mono.background,
        onBackground = mono.onBackground,
        surface = mono.background,
        onSurface = mono.onBackground,
        surfaceVariant = mono.surfaceHigh,
        onSurfaceVariant = mono.onSurfaceVariant,
        surfaceTint = Color.Transparent,
        outline = mono.outline,
        outlineVariant = mono.outline,
        inverseSurface = mono.invertedBackground,
        inverseOnSurface = mono.invertedContent,
        error = mono.onBackground,
        onError = mono.background
    )
}

/**
 * App theme. Each theme mode maps to an explicit monochrome scheme,
 * independently of the system setting.
 */
@Composable
fun FsolatTheme(
    mode: ThemeMode,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val mono = if (dark) DarkMono else LightMono
    val scheme = buildScheme(mono, dark)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(LocalMono provides mono) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content
        )
    }
}

/** Convenience accessor for the monochrome tokens. */
val mono: MonoColors
    @Composable get() = LocalMono.current
