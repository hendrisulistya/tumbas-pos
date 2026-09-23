package com.argminres.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── tambooPOS Cyan Palette (Base: #0095DA from logo.svg) ───────────────────────

// Primary Cyan / Cerulean Shades
val Cyan900 = Color(0xFF003D5B)   // deep navy / dark text contrast
val Cyan800 = Color(0xFF005A87)   // rich dark cerulean
val Cyan700 = Color(0xFF0077B3)   // deep brand primary (high contrast)
val Cyan600 = Color(0xFF0088CC)   // vivid cerulean
val Cyan500 = Color(0xFF0095DA)   // exact base color from logo.svg
val Cyan400 = Color(0xFF33AAEC)   // bright cerulean accent
val Cyan300 = Color(0xFF66BFF2)   // light cerulean
val Cyan200 = Color(0xFF99D4F7)   // soft sky
val Cyan100 = Color(0xFFD2EFFD)   // soft cyan accent / chip
val Cyan50  = Color(0xFFF0F9FE)   // gentle ice-blue card bg / surface variant

// Theme Aliases (Redirect legacy color tokens to tambooPOS palette)
val Blue900   = Cyan900
val Blue800   = Cyan800
val Blue700   = Cyan700
val Blue600   = Cyan600
val Blue500   = Cyan500
val Blue400   = Cyan400
val Blue100   = Cyan100
val Blue50    = Cyan50

// Backward-compatibility aliases for Yellow tokens
val Yellow900 = Cyan900
val Yellow800 = Cyan800
val Yellow700 = Cyan700
val Yellow600 = Cyan600
val Yellow500 = Cyan500
val Yellow400 = Cyan400
val Yellow300 = Cyan300
val Yellow100 = Cyan100
val Yellow50  = Cyan50

// Neutral
val Black     = Color(0xFF18181B)   // deep zinc black
val Gray800   = Color(0xFF3F3F46)
val Gray600   = Color(0xFF71717A)
val Gray400   = Color(0xFFA1A1AA)
val Gray300   = Color(0xFFE4E4E7)
val Gray100   = Color(0xFFF4F4F5)
val White     = Color(0xFFFFFFFF)

// Semantic
val Success   = Color(0xFF16A34A)
val Warning   = Color(0xFFEA580C)
val Error     = Color(0xFFDC2626)
val Info      = Color(0xFF0095DA)

// Surface
val SurfaceLight        = White
val SurfaceVariantLight = Cyan50

// ── Material 3 (Material You) Color Schemes ────────────────────────────────────

val TambooLightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Cyan600,
    onPrimary = White,
    primaryContainer = Cyan50,
    onPrimaryContainer = Cyan900,
    inversePrimary = Cyan300,

    secondary = Cyan700,
    onSecondary = White,
    secondaryContainer = Cyan100,
    onSecondaryContainer = Cyan900,

    tertiary = Color(0xFF0284C7),
    onTertiary = White,
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF0369A1),

    background = Color(0xFFF8FAFC),
    onBackground = Black,

    surface = White,
    onSurface = Black,
    surfaceVariant = Cyan50,
    onSurfaceVariant = Gray600,
    inverseSurface = Cyan900,
    inverseOnSurface = White,

    error = Error,
    onError = White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),

    outline = Gray300,
    outlineVariant = Gray100,
    scrim = Black
)

val TambooDarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Cyan400,
    onPrimary = Color(0xFF00334D),
    primaryContainer = Cyan900,
    onPrimaryContainer = Cyan100,
    inversePrimary = Cyan700,

    secondary = Cyan300,
    onSecondary = Color(0xFF00334D),
    secondaryContainer = Cyan800,
    onSecondaryContainer = Cyan100,

    tertiary = Color(0xFF7DD3FC),
    onTertiary = Color(0xFF082F49),
    tertiaryContainer = Color(0xFF0369A1),
    onTertiaryContainer = Color(0xFFE0F2FE),

    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),

    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    inverseSurface = Color(0xFFF1F5F9),
    inverseOnSurface = Color(0xFF0F172A),

    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),

    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    scrim = Black
)
