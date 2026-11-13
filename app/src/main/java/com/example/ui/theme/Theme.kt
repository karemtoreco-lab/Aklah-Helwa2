package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightBrandColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = LightTerracottaContainer,
    onPrimaryContainer = TerracottaDeep,
    secondary = SaffronOrangeGlow,
    onSecondary = EspressoInkText,
    secondaryContainer = LightWarmLinen,
    onSecondaryContainer = EspressoInkText,
    tertiary = MintBasilSuccess,
    onTertiary = Color.White,
    tertiaryContainer = LightBasilContainer,
    onTertiaryContainer = Color(0xFF14665B),
    background = WarmIvoryBg,
    onBackground = EspressoInkText,
    surface = PureCrispWhite,
    onSurface = EspressoInkText,
    surfaceVariant = LightWarmLinen,
    onSurfaceVariant = WarmTaupeText,
    outline = LightSandBorder,
    error = TerracottaDeep,
    onError = Color.White
)

private val DarkBrandColorScheme = darkColorScheme(
    primary = SaffronOrangeGlow,
    onPrimary = Color.White,
    primaryContainer = DarkSaffronContainer,
    onPrimaryContainer = Color(0xFFFFC599),
    secondary = WarmAmberGold,
    onSecondary = EspressoDeepBg,
    secondaryContainer = EspressoSecondarySurface,
    onSecondaryContainer = WarmIvoryText,
    tertiary = MintEmeraldProfit,
    onTertiary = EspressoDeepBg,
    tertiaryContainer = DarkEmeraldContainer,
    onTertiaryContainer = DarkEmeraldOnContainer,
    background = EspressoDeepBg,
    onBackground = WarmIvoryText,
    surface = EspressoCardSurface,
    onSurface = WarmIvoryText,
    surfaceVariant = EspressoSecondarySurface,
    onSurfaceVariant = WarmSandText,
    outline = EspressoSubtleBorder,
    error = Color(0xFFFF6B6B),
    onError = Color.White
)

val LocalAppColors = staticCompositionLocalOf { DarkAppSemanticColors }

object AppTheme {
    val colors: AppSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkBrandColorScheme else LightBrandColorScheme
    val semanticColors = if (darkTheme) DarkAppSemanticColors else LightAppSemanticColors

    CompositionLocalProvider(LocalAppColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
