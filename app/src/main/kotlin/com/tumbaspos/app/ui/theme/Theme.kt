package com.tumbaspos.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BlibliBlue,
    onPrimary = Color.White,
    primaryContainer = BlibliBlueLight,
    onPrimaryContainer = BlibliBlueDark,
    
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = SecondaryBlueLight,
    onSecondaryContainer = SecondaryBlueDark,
    
    tertiary = Info,
    onTertiary = Color.White,
    
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = Gray50,
    onBackground = Gray900,
    
    surface = SurfaceLight,
    onSurface = Gray900,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Gray700,
    
    outline = Gray400,
    outlineVariant = Gray300,
    
    inverseSurface = Gray900,
    inverseOnSurface = Gray50,
    inversePrimary = BlibliBlueLight
)

private val DarkColorScheme = darkColorScheme(
    primary = BlibliBlueLight,
    onPrimary = BlibliBlueDark,
    primaryContainer = BlibliBlue,
    onPrimaryContainer = Color.White,
    
    secondary = SecondaryBlueLight,
    onSecondary = SecondaryBlueDark,
    secondaryContainer = SecondaryBlue,
    onSecondaryContainer = Color.White,
    
    tertiary = Info,
    onTertiary = Color.White,
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = Gray900,
    onBackground = Gray50,
    
    surface = SurfaceDark,
    onSurface = Gray50,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Gray400,
    
    outline = Gray600,
    outlineVariant = Gray700,
    
    inverseSurface = Gray50,
    inverseOnSurface = Gray900,
    inversePrimary = BlibliBlue
)

@Composable
fun TumbasPOSTheme(
    settingsRepository: com.tumbaspos.app.data.repository.SettingsRepository,
    content: @Composable () -> Unit
) {
    val themeMode by settingsRepository.themeMode.collectAsState()
    
    val darkTheme = when (themeMode) {
        com.tumbaspos.app.data.repository.SettingsRepository.ThemeMode.LIGHT -> false
        com.tumbaspos.app.data.repository.SettingsRepository.ThemeMode.DARK -> true
        com.tumbaspos.app.data.repository.SettingsRepository.ThemeMode.SYSTEM -> isSystemInDarkTheme()
        else -> false // Default to light
    }
    
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
