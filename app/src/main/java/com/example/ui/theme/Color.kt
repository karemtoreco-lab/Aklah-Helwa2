package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ======================================================
// 1. Royal Bistro Dark Mode: Deep Espresso & Saffron Gold
// ======================================================
val ObsidianBg = Color(0xFF0C0A09)                 // Rich warm espresso-black canvas
val ObsidianBgEnd = Color(0xFF14110F)              // Subtle warm roasted depth end
val VelvetCardSurface = Color(0xFF181512)          // Elevated warm dark card surface
val VelvetGlassOverlay = Color(0xE61C1815)         // Frosted warm glass surface
val VelvetSecondarySurface = Color(0xFF231F1B)     // Input / pill / nested container surface
val VelvetSubtleBorder = Color(0xFF312B25)         // Crisp 1dp warm bronze border
val VelvetGlassBorder = Color(0x4DFF922B)          // Subtle saffron-tinted glass border
val VelvetDockGlass = Color(0xF5151210)            // Floating bottom dock surface

val SaffronAmberPrimary = Color(0xFFFF8A1E)        // Vibrant saffron-orange signature accent
val SaffronAmberDeep = Color(0xFFD96400)           // Deep caramelized amber
val HoneyGoldHighlight = Color(0xFFFFC145)         // Warm honey gold for badges, ratings & highlights
val MintEmeraldProfit = Color(0xFF22C58B)          // Fresh emerald for profit, live kitchen & success
val CrispOffWhiteText = Color(0xFFFAF6F0)          // Warm ivory-white high-contrast text
val CoolSlateSecondaryText = Color(0xFFB5ABA0)     // Warm stone secondary text

val DarkSaffronContainer = Color(0xFF2D1E11)
val DarkGoldContainer = Color(0xFF2B2212)
val DarkEmeraldContainer = Color(0xFF102A21)
val DarkEmeraldOnContainer = Color(0xFF86EFAC)

// ======================================================
// 2. Royal Bistro Light Mode: Warm Linen & Terracotta
// ======================================================
val AlabasterBg = Color(0xFFFAF7F2)                // Warm cream-linen scaffold
val AlabasterBgEnd = Color(0xFFF2ECE1)             // Soft warm sand gradient end
val PureCrispWhite = Color(0xFFFFFFFF)             // Crisp elevated card surface
val SilkSecondarySurface = Color(0xFFF3EDE4)       // Soft sand input & chip surface
val SilkBorder = Color(0xFFE5DDD0)                 // Clean warm border
val SilkDockGlass = Color(0xFAFFFFFF)

val TerracottaPrimary = Color(0xFFE05300)          // Rich clay-terracotta primary accent
val TerracottaDeep = Color(0xFFB83B00)
val ApricotGoldAccent = Color(0xFFF59E0B)
val MintBasilSuccess = Color(0xFF15805D)

val EspressoInkText = Color(0xFF1C1714)            // Deep roasted espresso ink text
val WarmTaupeText = Color(0xFF665B52)              // Balanced warm taupe secondary text

val LightTerracottaContainer = Color(0xFFFFEDE2)
val LightApricotContainer = Color(0xFFFFF5DC)
val LightBasilContainer = Color(0xFFDCFCE7)

// ======================================================
// Backward-compatible semantic aliases (preserved across all screens)
// ======================================================
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
    val cardHighlightGradient: Brush,
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
    glassSurface = Color(0xF7FFFFFF),
    secondarySurface = SilkSecondarySurface,
    cardBorder = SilkBorder,
    glassBorder = Color(0x4DE05300),
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
    onProfitContainer = Color(0xFF0F5C43),
    textPrimary = EspressoInkText,
    textSecondary = WarmTaupeText,
    heroGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFE05300), Color(0xFFF26E1B), Color(0xFFB83B00))
    ),
    warmAccentGradient = Brush.horizontalGradient(
        colors = listOf(TerracottaPrimary, ApricotGoldAccent)
    ),
    mintGradient = Brush.horizontalGradient(
        colors = listOf(MintBasilSuccess, Color(0xFF22A077))
    ),
    cardHighlightGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFFCF8F2))
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
    onPrimaryContainer = Color(0xFFFFCE9E),
    secondaryAccent = HoneyGoldHighlight,
    secondaryContainer = DarkGoldContainer,
    goldAccent = HoneyGoldHighlight,
    profitGreen = MintEmeraldProfit,
    profitContainer = DarkEmeraldContainer,
    onProfitContainer = DarkEmeraldOnContainer,
    textPrimary = CrispOffWhiteText,
    textSecondary = CoolSlateSecondaryText,
    heroGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF261E18), Color(0xFF191512), Color(0xFF3A2110))
    ),
    warmAccentGradient = Brush.horizontalGradient(
        colors = listOf(SaffronAmberPrimary, HoneyGoldHighlight)
    ),
    mintGradient = Brush.horizontalGradient(
        colors = listOf(MintEmeraldProfit, Color(0xFF179B6C))
    ),
    cardHighlightGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E1A16), Color(0xFF151210))
    )
)
