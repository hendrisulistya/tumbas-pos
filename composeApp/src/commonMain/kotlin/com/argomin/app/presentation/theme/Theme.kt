package com.argomin.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// ── tambooPOS Material 3 (Material You) Unified Theme ──────────────────────────

@Composable
fun TambooPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TambooDarkColorScheme else TambooLightColorScheme

    // Sync status bar with Material 3 surface (dark icons in light mode, light icons in dark mode)
    PlatformStatusBar(
        color = colorScheme.surface,
        darkIcons = !darkTheme
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TambooPosTypography,
        shapes = TambooPosShapes,
        content = content
    )
}
