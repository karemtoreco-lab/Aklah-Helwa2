package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ======================================================
// 1. Gourmet Bistro Dark Mode: Midnight Velvet & Saffron Gold (Default)
// ======================================================
val ObsidianBg = Color(0xFF0F1115)                 // Deep obsidian-charcoal scaffold
val ObsidianBgEnd = Color(0xFF151922)              // Subtle obsidian depth gradient end
val VelvetCardSurface = Color(0xFF181C23)          // Rich elevated dark card base
val VelvetGlassOverlay = Color(0xD9181C23)         // Frosted-glass elevated card surface
val VelvetSecondarySurface = Color(0xFF212631)     // Input / secondary surface
val VelvetSubtleBorder = Color(0xFF2A313D)         // Crisp 1dp velvet border
val VelvetGlassBorder = Color(0xFF2A313D)          // Subtle frosted glass 1dp border
val VelvetDockGlass = Color(0xFF161A22)            // Floating bottom dock background

val SaffronAmberPrimary = Color(0xFFFF8C24)        // Vibrant saffron/amber primary accent
val SaffronAmberDeep = Color(0xFFD96B08)           // Deep roasted amber companion
val HoneyGoldHighlight = Color(0xFFFFB84D)         // Warm honey gold badges & stars
val MintEmeraldProfit = Color(0xFF2EC4B6)          // Profit / live kitchen / success mint
val CrispOffWhiteText = Color(0xFFF8F9FA)          // High-contrast primary text
val CoolSlateSecondaryText = Color(0xFFA0AAB8)     // Readable secondary slate text

val DarkSaffronContainer = Color(0xFF2E2216)
val DarkGoldContainer = Color(0xFF2B2415)
val DarkEmeraldContainer = Color(0xFF112C2A)
val DarkEmeraldOnContainer = Color(0xFF8FF2E7)

// ======================================================
// 2. Gourmet Bistro Light Mode: Warm Silk & Terracotta
// ======================================================
val AlabasterBg = Color(0xFFFAF8F5)                // Clean warm alabaster scaffold
val AlabasterBgEnd = Color(0xFFF3EEE7)             // Soft silk gradient end
val PureCrispWhite = Color(0xFFFFFFFF)             // Pure white elevated card surface
val SilkSecondarySurface = Color(0xFFF3EEE7)       // Input / secondary silk surface
val SilkBorder = Color(0xFFE7DDD2)                 // Subtle warm border
val SilkDockGlass = Color(0xFFFCFBF9)

val TerracottaPrimary = Color(0xFFE65100)          // Rich terracotta-orange primary accent
val TerracottaDeep = Color(0xFFBF360C)
val ApricotGoldAccent = Color(0xFFF59E0B)
val MintBasilSuccess = Color(0xFF1B8A7A)

val EspressoInkText = Color(0xFF1A1614)            // Deep espresso primary text
val WarmTaupeText = Color(0xFF5E524B)              // Clean warm taupe secondary text

val LightTerracottaContainer = Color(0xFFFFEDE2)
val LightApricotContainer = Color(0xFFFFF4DE)
val LightBasilContainer = Color(0xFFE2F5F2)

// Backward-compatible semantic aliases (preserved across all screens)
val EspressoDeepBg = ObsidianBg
val EspressoCharcoalEnd = ObsidianBgEnd
val EspressoCardSurface = VelvetCardSurface
val EspressoGlassOverlay = VelvetGlassOverlay
val EspressoSecondarySurface = VelvetSecondarySurface
val EspressoGlassBorder = VelvetGlassBorder
val EspressoSubtleBorder = VelvetSubtleBorder
val EspressoDockGlass = VelvetDockGlass
val SaffronOrangeGlow = SaffronAmberPrimary
val SaffronDeepFlame = SaffronAmberDeep
val WarmAmberGold = HoneyGoldHighlight
val WarmIvoryText = CrispOffWhiteText
val WarmSandText = CoolSlateSecondaryText
val WarmIvoryBg = AlabasterBg
val LightWarmLinen = SilkSecondarySurface
val LightSandBorder = SilkBorder
val LightDockGlass = SilkDockGlass

val TomatoCoralPrimary = TerracottaPrimary
val TomatoCoralDeep = TerracottaDeep
val ApricotSaffronSecondary = SaffronAmberPrimary
val GoldBadgeHighlight = HoneyGoldHighlight
val TomatoRedPrimary = TerracottaPrimary
val SaffronOrangeSecondary = SaffronAmberPrimary
val GoldHighlight = HoneyGoldHighlight
val CleanWhite = Color(0xFFFFFFFF)
val BrandRedPrimary = SaffronAmberPrimary
val EmeraldProfit = MintEmeraldProfit
val WarmOrangeHighlight = HoneyGoldHighlight
val CharcoalBlackBg = ObsidianBg
val CharcoalCardSurface = VelvetCardSurface
val CharcoalCardElevated = VelvetSecondarySurface
val CharcoalCardBorder = VelvetSubtleBorder
val EmeraldProfitContainer = DarkEmeraldContainer
val EmeraldProfitOnContainer = DarkEmeraldOnContainer
val TextWhitePrimary = CrispOffWhiteText
val TextWhiteSecondary = CoolSlateSecondaryText

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
    background = AlabasterBg,
    backgroundEnd = AlabasterBgEnd,
    backgroundGradient = Brush.verticalGradient(
        colors = listOf(AlabasterBg, AlabasterBgEnd)
    ),
    cardSurface = PureCrispWhite,
    glassSurface = Color(0xF5FFFFFF),
    secondarySurface = SilkSecondarySurface,
    cardBorder = SilkBorder,
    glassBorder = Color(0x66E65100),
    floatingDock = SilkDockGlass,
    primaryBrand = TerracottaPrimary,
    primaryDeep = TerracottaDeep,
    primaryContainer = LightTerracottaContainer,
    onPrimaryContainer = TerracottaDeep,
    secondaryAccent = SaffronAmberPrimary,
    secondaryContainer = LightApricotContainer,
    goldAccent = ApricotGoldAccent,
    profitGreen = MintBasilSuccess,
    profitContainer = LightBasilContainer,
    onProfitContainer = Color(0xFF116155),
    textPrimary = EspressoInkText,
    textSecondary = WarmTaupeText,
    heroGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFE65100), Color(0xFFF26D21), Color(0xFFC83E06))
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
    background = ObsidianBg,
    backgroundEnd = ObsidianBgEnd,
    backgroundGradient = Brush.verticalGradient(
        colors = listOf(ObsidianBg, ObsidianBgEnd, ObsidianBg)
    ),
    cardSurface = VelvetCardSurface,
    glassSurface = VelvetGlassOverlay,
    secondarySurface = VelvetSecondarySurface,
    cardBorder = VelvetSubtleBorder,
    glassBorder = VelvetGlassBorder,
    floatingDock = VelvetDockGlass,
    primaryBrand = SaffronAmberPrimary,
    primaryDeep = SaffronAmberDeep,
    primaryContainer = DarkSaffronContainer,
    onPrimaryContainer = Color(0xFFFFCA99),
    secondaryAccent = HoneyGoldHighlight,
    secondaryContainer = DarkGoldContainer,
    goldAccent = HoneyGoldHighlight,
    profitGreen = MintEmeraldProfit,
    profitContainer = DarkEmeraldContainer,
    onProfitContainer = DarkEmeraldOnContainer,
    textPrimary = CrispOffWhiteText,
    textSecondary = CoolSlateSecondaryText,
    heroGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF232936), Color(0xFF181C24), Color(0xFF3A2414))
    ),
    warmAccentGradient = Brush.horizontalGradient(
        colors = listOf(SaffronAmberPrimary, HoneyGoldHighlight)
    ),
    mintGradient = Brush.horizontalGradient(
        colors = listOf(MintEmeraldProfit, Color(0xFF1FA699))
    )
)
