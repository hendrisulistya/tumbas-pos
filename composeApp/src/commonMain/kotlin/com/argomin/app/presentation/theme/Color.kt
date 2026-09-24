package com.argomin.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ═════════════════════════════════════════════════════════════════════════════════
// ── tambooPOS Centralized Design Tokens (Harmonious Contrast & Accessibility) ──
// ═════════════════════════════════════════════════════════════════════════════════

// ── Primary Brand Palette (Cyan / Cerulean - Base: #0095DA from logo.svg) ───────
val Cyan950 = Color(0xFF00273B)   // Ultra deep navy slate
val Cyan900 = Color(0xFF003D5B)   // Deep navy / high contrast dark text
val Cyan800 = Color(0xFF005885)   // Rich dark cerulean
val Cyan700 = Color(0xFF0277B6)   // Main brand primary (balanced contrast on white)
val Cyan600 = Color(0xFF0088CC)   // Vivid cerulean action
val Cyan500 = Color(0xFF0095DA)   // Logo exact base color
val Cyan400 = Color(0xFF38BDF8)   // Sky accent
val Cyan300 = Color(0xFF7DD3FC)   // Light cerulean
val Cyan200 = Color(0xFFBAE6FD)   // Chip outline & subtle border
val Cyan100 = Color(0xFFE0F2FE)   // Soft active tint / tag background
val Cyan50  = Color(0xFFF0F9FF)   // Tinted card surface / selected table row

// ── Neutral Scale (Slate - High legibility, no washed-out text) ──────────────────
val Neutral950 = Color(0xFF020617)  // Deepest slate
val Neutral900 = Color(0xFF0F172A)  // Slate 900 for dark surfaces & high emphasis titles
val Neutral800 = Color(0xFF1E293B)  // Slate 800 standard body text (crisp & comfortable)
val Neutral700 = Color(0xFF334155)  // Slate 700 secondary body & strong labels
val Neutral600 = Color(0xFF475569)  // Slate 600 secondary text (WCAG AA compliant >4.5:1)
val Neutral500 = Color(0xFF64748B)  // Slate 500 placeholder & muted icons
val Neutral400 = Color(0xFF94A3B8)  // Slate 400 medium borders & inactive icons
val Neutral300 = Color(0xFFCBD5E1)  // Slate 300 visible borders & empty PIN dots
val Neutral200 = Color(0xFFE2E8F0)  // Slate 200 card borders & table dividers
val Neutral100 = Color(0xFFF1F5F9)  // Slate 100 input backgrounds & inactive buttons
val Neutral50  = Color(0xFFF8FAFC)  // Slate 50 app root canvas background
val White      = Color(0xFFFFFFFF)
val Black      = Neutral900

// Clean Slate mappings for standard gray aliases
val Gray800 = Neutral800
val Gray700 = Neutral700
val Gray600 = Neutral600
val Gray500 = Neutral500
val Gray400 = Neutral400
val Gray300 = Neutral300
val Gray200 = Neutral200
val Gray100 = Neutral100

// ── Semantic Tokens (Balanced for high legibility without eye fatigue) ───────────

// Success (Emerald Green)
val SuccessBase        = Color(0xFF15803D) // Green 700 - high contrast text
val Success            = Color(0xFF16A34A) // Green 600 - icons / action buttons
val SuccessBorder      = Color(0xFF86EFAC) // Green 300 - clear visible badge border
val SuccessContainer   = Color(0xFFDCFCE7) // Green 100 - soft badge & status background
val OnSuccess          = White
val OnSuccessContainer = Color(0xFF14532D) // Green 900 - dark text on container

// Warning (Warm Amber)
val WarningBase        = Color(0xFFB45309) // Amber 700
val Warning            = Color(0xFFD97706) // Amber 600
val WarningBorder      = Color(0xFFFCD34D) // Amber 300
val WarningContainer   = Color(0xFFFEF3C7) // Amber 100
val OnWarning          = White
val OnWarningContainer = Color(0xFF78350F) // Amber 900

// Danger / Error (Rose Crimson - Not jarring neon)
val ErrorBase          = Color(0xFFB91C1C) // Red 700 - high contrast text
val Error              = Color(0xFFDC2626) // Red 600 - icons / delete buttons
val ErrorBorder        = Color(0xFFFCA5A5) // Red 300 - danger badge border
val ErrorContainer     = Color(0xFFFEE2E2) // Red 100 - soft danger badge background
val OnError            = White
val OnErrorContainer   = Color(0xFF7F1D1D) // Red 900 - dark text on container

// Info / Accent
val InfoBase           = Cyan800
val Info               = Cyan600
val InfoBorder         = Cyan200
val InfoContainer      = Cyan100
val OnInfo             = White
val OnInfoContainer    = Cyan900

// ── Surface & Component Tokens ──────────────────────────────────────────────────
val SurfaceLight        = White
val SurfaceVariantLight = Cyan50
val SurfaceCard         = White
val SurfaceInput        = White
val SurfaceSelected     = Cyan50

// ── Central Metro / Dashboard Tile Colors (Harmonious & Professional) ───────────
object MetroThemeColors {
    // POS & Penjualan (Warm Amber / Bronze)
    val SalesCategory   = Color(0xFFD97706)
    val CashierTile     = Color(0xFFC2410C)
    val SalesOrderTile  = Color(0xFFD97706)
    val SessionTile     = Color(0xFF9A3412)

    // Operasional Harian (Teal / Forest Emerald)
    val DailyCategory   = Color(0xFF0F766E)
    val ShowcaseTile    = Color(0xFF0D9488)
    val IngredientTile  = Color(0xFF059669)
    val EndOfDayTile    = Color(0xFF047857)

    // Data Master (Indigo / Royal Slate)
    val MasterCategory  = Color(0xFF4338CA)
    val DishMasterTile  = Color(0xFF4F46E5)
    val IngMasterTile   = Color(0xFF3730A3)

    // Manajemen & Reporting (Slate Navy / Steel Blue)
    val MgmtCategory    = Color(0xFF0369A1)
    val EmployerTile    = Color(0xFF0284C7)
    val ReportingTile   = Color(0xFF0369A1)
    val WipTile         = Color(0xFF475569)
    val AuditTile       = Color(0xFF334155)
    val SettingsTile    = Color(0xFF1E293B)
}

// ── Material 3 Color Schemes ────────────────────────────────────────────────────

val TambooLightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Cyan700,
    onPrimary = White,
    primaryContainer = Cyan50,
    onPrimaryContainer = Cyan900,
    inversePrimary = Cyan300,

    secondary = Cyan800,
    onSecondary = White,
    secondaryContainer = Cyan100,
    onSecondaryContainer = Cyan900,

    tertiary = Color(0xFF0284C7),
    onTertiary = White,
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF0369A1),

    background = Neutral50,
    onBackground = Neutral800,

    surface = White,
    onSurface = Neutral800,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral600,
    inverseSurface = Neutral900,
    inverseOnSurface = White,

    error = Error,
    onError = White,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    outline = Neutral300,
    outlineVariant = Neutral200,
    scrim = Neutral900
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
    scrim = Color(0xFF000000)
)
