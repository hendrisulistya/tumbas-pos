package com.argminres.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CleanBlueColorScheme = lightColorScheme(
    primary             = Blue600,
    onPrimary           = White,
    primaryContainer    = Blue50,
    onPrimaryContainer  = Blue700,

    secondary           = Blue500,
    onSecondary         = White,
    secondaryContainer  = Blue100,
    onSecondaryContainer= Blue700,

    tertiary            = Info,
    onTertiary          = White,
    tertiaryContainer   = Blue50,
    onTertiaryContainer = Blue700,

    error               = Error,
    onError             = White,
    errorContainer      = Color(0xFFFFDAD6),
    onErrorContainer    = Error,

    background          = White,
    onBackground        = Black,

    surface             = White,
    onSurface           = Black,
    surfaceVariant      = Blue50,
    onSurfaceVariant    = Gray600,

    outline             = Gray300,
    outlineVariant      = Blue100,

    inverseSurface      = Blue700,
    inverseOnSurface    = White,
    inversePrimary      = Blue400
)

@Composable
fun PadangPOSTheme(
    settingsRepository: com.argminres.app.data.repository.SettingsRepository,
    content: @Composable () -> Unit
) {
    val colorScheme = CleanBlueColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Blue600.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
