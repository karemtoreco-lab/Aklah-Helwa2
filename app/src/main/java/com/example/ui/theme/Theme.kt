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
    secondary = SaffronAmberPrimary,
    onSecondary = EspressoInkText,
    secondaryContainer = SilkSecondarySurface,
    onSecondaryContainer = EspressoInkText,
    tertiary = MintBasilSuccess,
    onTertiary = Color.White,
    tertiaryContainer = LightBasilContainer,
    onTertiaryContainer = Color(0xFF0F5C43),
    background = AlabasterBg,
    onBackground = EspressoInkText,
    surface = PureCrispWhite,
    onSurface = EspressoInkText,
    surfaceVariant = SilkSecondarySurface,
    onSurfaceVariant = WarmTaupeText,
    outline = SilkBorder,
    error = TerracottaDeep,
    onError = Color.White
)

private val DarkBrandColorScheme = darkColorScheme(
    primary = SaffronAmberPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkSaffronContainer,
    onPrimaryContainer = Color(0xFFFFCE9E),
    secondary = HoneyGoldHighlight,
    onSecondary = ObsidianBg,
    secondaryContainer = VelvetSecondarySurface,
    onSecondaryContainer = CrispOffWhiteText,
    tertiary = MintEmeraldProfit,
    onTertiary = ObsidianBg,
    tertiaryContainer = DarkEmeraldContainer,
    onTertiaryContainer = DarkEmeraldOnContainer,
    background = ObsidianBg,
    onBackground = CrispOffWhiteText,
    surface = VelvetCardSurface,
    onSurface = CrispOffWhiteText,
    surfaceVariant = VelvetSecondarySurface,
    onSurfaceVariant = CoolSlateSecondaryText,
    outline = VelvetSubtleBorder,
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
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp)
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
