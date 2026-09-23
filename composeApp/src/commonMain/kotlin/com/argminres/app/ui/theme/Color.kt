package com.argminres.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── YellowPos Warm Golden Palette ─────────────────────────────────────────────

// Primary Yellow & Amber Shades
val Yellow900 = Color(0xFF78350F)   // deep amber / dark text contrast
val Yellow800 = Color(0xFF92400E)   // rich dark amber
val Yellow700 = Color(0xFFB45309)   // deep golden primary (high contrast)
val Yellow600 = Color(0xFFD97706)   // warm golden amber
val Yellow500 = Color(0xFFF59E0B)   // vibrant gold
val Yellow400 = Color(0xFFFBBF24)   // bright sunshine gold
val Yellow300 = Color(0xFFFCD34D)   // light gold
val Yellow100 = Color(0xFFFEF3C7)   // soft yellow accent
val Yellow50  = Color(0xFFFFFBEB)   // warm cream / soft yellow card bg

// Theme Aliases (Redirect legacy color tokens to YellowPos palette)
val Blue800   = Yellow800
val Blue700   = Yellow700
val Blue600   = Yellow600
val Blue500   = Yellow500
val Blue400   = Yellow400
val Blue100   = Yellow100
val Blue50    = Yellow50

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
val Info      = Color(0xFFD97706)

// Surface
val SurfaceLight        = White
val SurfaceVariantLight = Yellow50
