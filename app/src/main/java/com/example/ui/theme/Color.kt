package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ======================================================
// 1. Clean Warm Light Luxury Palette (#F7F6F2, #FFFFFF, #EAE6DF, #8B2635, #C59B27)
// ======================================================
val LightBg = Color(0xFFF7F6F2)                    // Clean warm alabaster background (#F7F6F2)
val LightCardSurface = Color(0xFFFFFFFF)           // Pure crisp white cards (#FFFFFF)
val LightSecondarySurface = Color(0xFFF2EFE9)      // Warm linen chip/input surface
val LightBorder = Color(0xFFEAE6DF)                // Thin subtle warm border (#EAE6DF)
val LightDockSurface = Color(0xFCFFFFFF)           // Clean floating dock surface

val LightPrimaryBrand = Color(0xFF8B2635)          // Royal burgundy primary (#8B2635)
val LightPrimaryDeep = Color(0xFF6B1A26)           // Deep royal burgundy (#6B1A26)
val LightSecondaryContainer = Color(0xFFF9ECEE)    // Soft warm rose-cream container
val LightOnSecondaryContainer = Color(0xFF6B1A26)  // Deep burgundy text on container

val LightTextPrimary = Color(0xFF1F1D1B)           // Warm espresso charcoal (#1F1D1B)
val LightTextSecondary = Color(0xFF6E6A64)         // Calm warm gray-taupe (#6E6A64)

val LightSuccessContainer = Color(0xFFEAF5ED)      // Soft emerald tint
val LightOnSuccessContainer = Color(0xFF1B6430)    // Deep emerald text
val LightWarningContainer = Color(0xFFFBF5E4)      // Soft warm gold tint
val LightWarningText = Color(0xFF8C6B11)           // High-contrast warm gold-ochre text
val LightErrorContainer = Color(0xFFFCECEC)        // Soft calm red tint
val LightOnErrorContainer = Color(0xFF9E1C1C)      // Deep red text

// ======================================================
// 2. Clean Dark Mode Palette (Charcoal-Black & Soft Burgundy-Rose, zero purple/blue)
// ======================================================
val DarkBg = Color(0xFF121316)                     // Deep charcoal black (#121316)
val DarkCardSurface = Color(0xFF1C1E22)            // Elevated dark slate-charcoal (#1C1E22)
val DarkSecondarySurface = Color(0xFF24272C)       // Nested input/chip surface
val DarkBorder = Color(0xFF2C2F36)                 // Subtle dark border (#2C2F36)
val DarkDockSurface = Color(0xFA1C1E22)            // Clean floating dark dock

val DarkPrimaryBrand = Color(0xFFD96574)           // Balanced warm rose-burgundy (#D96574)
val DarkPrimaryDeep = Color(0xFF8B2635)            // Rich burgundy accent
val DarkSecondaryContainer = Color(0xFF2B191D)     // Calm dark burgundy container
val DarkOnSecondaryContainer = Color(0xFFF4A8B2)   // Soft rose text on container

val DarkTextPrimary = Color(0xFFF1F3F5)            // Soft crisp off-white (#F1F3F5)
val DarkTextSecondary = Color(0xFFADB5BD)          // Clean light gray (#ADB5BD)

val DarkSuccessContainer = Color(0xFF172B1E)       // Calm dark emerald container
val DarkOnSuccessContainer = Color(0xFF7BE098)     // Readable mint-emerald text
val DarkWarningContainer = Color(0xFF2B2413)       // Calm dark gold container
val DarkWarningText = Color(0xFFE6C15A)            // Luminous warm gold text
val DarkErrorContainer = Color(0xFF2F181A)         // Calm dark red container
val DarkOnErrorContainer = Color(0xFFF88B8B)       // Readable calm red text

// ======================================================
// 3. Fixed Semantic Status & Luxury Gold Colors
// ======================================================
val StatusSuccessEmerald = Color(0xFF2B9348)       // Success / Open / Delivered (#2B9348)
val StatusWarningAmber = Color(0xFFC59B27)         // Luxury Gold / Preparing (#C59B27)
val StatusErrorRed = Color(0xFFD62828)             // Cancelled / Closed (#D62828)

// ======================================================
// Backward-compatible semantic aliases (preserved across codebase)
// ======================================================
val AlabasterBg = LightBg
val AlabasterBgEnd = LightBg
val PureCrispWhite = LightCardSurface
val SilkSecondarySurface = LightSecondarySurface
val SilkBorder = LightBorder
val SilkDockGlass = LightDockSurface

val TerracottaPrimary = LightPrimaryBrand
val TerracottaDeep = LightPrimaryDeep
val ApricotGoldAccent = StatusWarningAmber
val GoldTextOnLight = LightWarningText
val MintBasilSuccess = StatusSuccessEmerald
val EspressoInkText = LightTextPrimary
val WarmTaupeText = LightTextSecondary

val ObsidianBg = DarkBg
val ObsidianBgEnd = DarkBg
val VelvetCardSurface = DarkCardSurface
val VelvetSecondarySurface = DarkSecondarySurface
val VelvetSubtleBorder = DarkBorder
val VelvetDockGlass = DarkDockSurface
val SaffronAmberPrimary = DarkPrimaryBrand
val SaffronAmberDeep = DarkPrimaryDeep
val HoneyGoldHighlight = StatusWarningAmber
val HoneyGoldTextDark = DarkWarningText
val MintEmeraldProfit = StatusSuccessEmerald
val CrispOffWhiteText = DarkTextPrimary
val CoolSlateSecondaryText = DarkTextSecondary

val CleanWhite = Color(0xFFFFFFFF)
val BrandRedPrimary = LightPrimaryBrand
val EmeraldProfit = StatusSuccessEmerald
val WarmOrangeHighlight = StatusWarningAmber

@Immutable
data class AppSemanticColors(
    val isDark: Boolean,
    val background: Color,
    val backgroundEnd: Color,
    val backgroundGradient: Brush,
    val cardSurface: Color,
    val glassSurface: Color,
    val secondarySurface: Color,
    val cardBorder: Color,
    val glassBorder: Color,
    val floatingDock: Color,
    val primaryBrand: Color,
    val primaryDeep: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondaryAccent: Color,
    val secondaryContainer: Color,
    val goldAccent: Color,
    val goldText: Color,
    val profitGreen: Color,
    val profitContainer: Color,
    val onProfitContainer: Color,
    val errorRed: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val softShadow: Color,
    val heroGradient: Brush,
    val warmAccentGradient: Brush,
    val mintGradient: Brush,
    val cardHighlightGradient: Brush,
    val onAccentText: Color
)

val LightAppSemanticColors = AppSemanticColors(
    isDark = false,
    background = LightBg,
    backgroundEnd = LightBg,
    backgroundGradient = Brush.verticalGradient(
        colors = listOf(LightBg, LightBg)
    ),
    cardSurface = LightCardSurface,
    glassSurface = Color(0xFAFFFFFF),
    secondarySurface = LightSecondarySurface,
    cardBorder = LightBorder,
    glassBorder = LightBorder,
    floatingDock = LightDockSurface,
    primaryBrand = LightPrimaryBrand,
    primaryDeep = LightPrimaryDeep,
    primaryContainer = LightSecondaryContainer,
    onPrimaryContainer = LightOnSecondaryContainer,
    secondaryAccent = StatusWarningAmber,
    secondaryContainer = LightWarningContainer,
    goldAccent = StatusWarningAmber,
    goldText = LightWarningText,
    profitGreen = StatusSuccessEmerald,
    profitContainer = LightSuccessContainer,
    onProfitContainer = LightOnSuccessContainer,
    errorRed = StatusErrorRed,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    softShadow = Color(0x141F1D1B),
    heroGradient = Brush.verticalGradient(
        colors = listOf(LightPrimaryBrand, Color(0xFF721D2A))
    ),
    warmAccentGradient = Brush.horizontalGradient(
        colors = listOf(LightPrimaryBrand, LightPrimaryBrand)
    ),
    mintGradient = Brush.horizontalGradient(
        colors = listOf(StatusSuccessEmerald, StatusSuccessEmerald)
    ),
    cardHighlightGradient = Brush.verticalGradient(
        colors = listOf(LightCardSurface, LightCardSurface)
    ),
    onAccentText = Color.White
)

val DarkAppSemanticColors = AppSemanticColors(
    isDark = true,
    background = DarkBg,
    backgroundEnd = DarkBg,
    backgroundGradient = Brush.verticalGradient(
        colors = listOf(DarkBg, DarkBg)
    ),
    cardSurface = DarkCardSurface,
    glassSurface = Color(0xF51C1E22),
    secondarySurface = DarkSecondarySurface,
    cardBorder = DarkBorder,
    glassBorder = DarkBorder,
    floatingDock = DarkDockSurface,
    primaryBrand = DarkPrimaryBrand,
    primaryDeep = DarkPrimaryDeep,
    primaryContainer = DarkSecondaryContainer,
    onPrimaryContainer = DarkOnSecondaryContainer,
    secondaryAccent = StatusWarningAmber,
    secondaryContainer = DarkWarningContainer,
    goldAccent = StatusWarningAmber,
    goldText = DarkWarningText,
    profitGreen = StatusSuccessEmerald,
    profitContainer = DarkSuccessContainer,
    onProfitContainer = DarkOnSuccessContainer,
    errorRed = StatusErrorRed,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    softShadow = Color(0x45000000),
    heroGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF2B191D), DarkCardSurface)
    ),
    warmAccentGradient = Brush.horizontalGradient(
        colors = listOf(DarkPrimaryBrand, DarkPrimaryBrand)
    ),
    mintGradient = Brush.horizontalGradient(
        colors = listOf(StatusSuccessEmerald, StatusSuccessEmerald)
    ),
    cardHighlightGradient = Brush.verticalGradient(
        colors = listOf(DarkCardSurface, DarkCardSurface)
    ),
    onAccentText = DarkBg
)
