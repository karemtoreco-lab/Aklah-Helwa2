package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. Gourmet Espresso Dark Palette (Default)
// ==========================================
val EspressoDeepBg = Color(0xFF140D0A)           // Deep roasted espresso
val EspressoCharcoalEnd = Color(0xFF1F1410)      // Roasted charcoal gradient end
val EspressoCardSurface = Color(0xFF231712)      // Elevated dark card base
val EspressoGlassOverlay = Color(0x26FFFFFF)     // Translucent glassmorphic surface
val EspressoSecondarySurface = Color(0xFF2D1E18) // Input / secondary surface
val EspressoGlassBorder = Color(0x45FFB84D)      // Subtle 1dp glowing warm amber border
val EspressoSubtleBorder = Color(0xFF3E2B23)
val EspressoDockGlass = Color(0xFF1D130F)

val SaffronOrangeGlow = Color(0xFFFF7A1A)        // Primary brand glowing saffron-orange
val SaffronDeepFlame = Color(0xFFD95800)         // Deep roasted gradient companion
val WarmAmberGold = Color(0xFFFFB84D)            // Warm gold highlight & rating stars
val MintEmeraldProfit = Color(0xFF2EC4B6)        // Profit / online / success green
val WarmIvoryText = Color(0xFFFAF4EC)            // Primary text in Dark Mode
val WarmSandText = Color(0xFFCBB9AD)             // Secondary text in Dark Mode

val DarkSaffronContainer = Color(0xFF3D2111)
val DarkGoldContainer = Color(0xFF3A2A12)
val DarkEmeraldContainer = Color(0xFF12302C)
val DarkEmeraldOnContainer = Color(0xFF8FF2E7)

// ==========================================
// 2. Warm Ivory Culinary Light Palette
// ==========================================
val WarmIvoryBg = Color(0xFFFFF9F3)              // Warm ivory/cream scaffold
val PureCrispWhite = Color(0xFFFFFFFF)           // Crisp white card surface
val LightWarmLinen = Color(0xFFFFF2E5)           // Secondary / input surface
val LightSandBorder = Color(0xFFEDE0D1)          // Warm subtle border
val LightDockGlass = Color(0xFFFFFBF7)

val TerracottaPrimary = Color(0xFFE65C00)        // Rich terracotta/saffron
val TerracottaDeep = Color(0xFFB84500)
val ApricotGoldAccent = Color(0xFFF49D1A)
val MintBasilSuccess = Color(0xFF1E8E7E)

val EspressoInkText = Color(0xFF241610)
val WarmTaupeText = Color(0xFF6D594F)

val LightTerracottaContainer = Color(0xFFFFECE0)
val LightApricotContainer = Color(0xFFFFF3DD)
val LightBasilContainer = Color(0xFFE4F6F3)

// Backward-compatible aliases
val TomatoCoralPrimary = TerracottaPrimary
val TomatoCoralDeep = TerracottaDeep
val ApricotSaffronSecondary = SaffronOrangeGlow
val GoldBadgeHighlight = WarmAmberGold
val TomatoRedPrimary = TerracottaPrimary
val SaffronOrangeSecondary = SaffronOrangeGlow
val GoldHighlight = WarmAmberGold
val CleanWhite = Color(0xFFFFFFFF)
val BrandRedPrimary = SaffronOrangeGlow
val EmeraldProfit = MintEmeraldProfit
val WarmOrangeHighlight = WarmAmberGold

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
    val profitGreen: Color,
    val profitContainer: Color,
    val onProfitContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val heroGradient: Brush,
    val warmAccentGradient: Brush,
    val mintGradient: Brush,
    val onAccentText: Color = Color.White
)

val LightAppSemanticColors = AppSemanticColors(
    isDark = false,
    background = WarmIvoryBg,
    backgroundEnd = Color(0xFFFFF4E8),
    backgroundGradient = Brush.verticalGradient(
        colors = listOf(WarmIvoryBg, Color(0xFFFFF3E6))
    ),
    cardSurface = PureCrispWhite,
    glassSurface = Color(0xEBFFFFFF),
    secondarySurface = LightWarmLinen,
    cardBorder = LightSandBorder,
    glassBorder = Color(0x80E65C00),
    floatingDock = LightDockGlass,
    primaryBrand = TerracottaPrimary,
    primaryDeep = TerracottaDeep,
    primaryContainer = LightTerracottaContainer,
    onPrimaryContainer = TerracottaDeep,
    secondaryAccent = SaffronOrangeGlow,
    secondaryContainer = LightApricotContainer,
    goldAccent = ApricotGoldAccent,
    profitGreen = MintBasilSuccess,
    profitContainer = LightBasilContainer,
    onProfitContainer = Color(0xFF14665B),
    textPrimary = EspressoInkText,
    textSecondary = WarmTaupeText,
    heroGradient = Brush.linearGradient(
        colors = listOf(TerracottaPrimary, Color(0xFFF26A1B), Color(0xFFE04819))
    ),
    warmAccentGradient = Brush.horizontalGradient(
        colors = listOf(TerracottaPrimary, ApricotGoldAccent)
    ),
    mintGradient = Brush.horizontalGradient(
        colors = listOf(MintBasilSuccess, Color(0xFF2EB2A1))
    )
)

val DarkAppSemanticColors = AppSemanticColors(
    isDark = true,
    background = EspressoDeepBg,
    backgroundEnd = EspressoCharcoalEnd,
    backgroundGradient = Brush.verticalGradient(
        colors = listOf(EspressoDeepBg, EspressoCharcoalEnd, Color(0xFF170F0C))
    ),
    cardSurface = EspressoCardSurface,
    glassSurface = EspressoGlassOverlay,
    secondarySurface = EspressoSecondarySurface,
    cardBorder = EspressoSubtleBorder,
    glassBorder = EspressoGlassBorder,
    floatingDock = EspressoDockGlass,
    primaryBrand = SaffronOrangeGlow,
    primaryDeep = SaffronDeepFlame,
    primaryContainer = DarkSaffronContainer,
    onPrimaryContainer = Color(0xFFFFC599),
    secondaryAccent = WarmAmberGold,
    secondaryContainer = DarkGoldContainer,
    goldAccent = WarmAmberGold,
    profitGreen = MintEmeraldProfit,
    profitContainer = DarkEmeraldContainer,
    onProfitContainer = DarkEmeraldOnContainer,
    textPrimary = WarmIvoryText,
    textSecondary = WarmSandText,
    heroGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFE8630A), Color(0xFFB54106), Color(0xFF2B1A13))
    ),
    warmAccentGradient = Brush.horizontalGradient(
        colors = listOf(SaffronOrangeGlow, WarmAmberGold)
    ),
    mintGradient = Brush.horizontalGradient(
        colors = listOf(MintEmeraldProfit, Color(0xFF1F9D91))
    )
)
