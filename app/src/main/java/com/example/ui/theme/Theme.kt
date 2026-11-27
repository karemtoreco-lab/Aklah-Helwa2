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
    primary = LightPrimaryBrand,
    onPrimary = Color.White,
    primaryContainer = LightSecondaryContainer,
    onPrimaryContainer = LightOnSecondaryContainer,
    secondary = StatusWarningAmber,
    onSecondary = LightTextPrimary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = StatusSuccessEmerald,
    onTertiary = Color.White,
    tertiaryContainer = LightSuccessContainer,
    onTertiaryContainer = LightOnSuccessContainer,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightCardSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSecondarySurface,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = StatusErrorRed,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

private val DarkBrandColorScheme = darkColorScheme(
    primary = DarkPrimaryBrand,
    onPrimary = DarkBg,
    primaryContainer = DarkSecondaryContainer,
    onPrimaryContainer = DarkOnSecondaryContainer,
    secondary = StatusWarningAmber,
    onSecondary = DarkBg,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = StatusSuccessEmerald,
    onTertiary = Color.White,
    tertiaryContainer = DarkSuccessContainer,
    onTertiaryContainer = DarkOnSuccessContainer,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkCardSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSecondarySurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = StatusErrorRed,
    onError = Color.White,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

val LocalAppColors = staticCompositionLocalOf { LightAppSemanticColors }

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
    darkTheme: Boolean = false,
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
