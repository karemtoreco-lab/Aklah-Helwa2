package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand visual identity: Charcoal Black, Vibrant Red, and Clean White
private val BrandDarkColorScheme = darkColorScheme(
    primary = BrandRedPrimary,
    onPrimary = CleanWhite,
    primaryContainer = BrandRedContainer,
    onPrimaryContainer = BrandRedOnContainer,
    secondary = CleanWhite,
    onSecondary = CharcoalBlackBg,
    secondaryContainer = CharcoalCardElevated,
    onSecondaryContainer = CleanWhite,
    tertiary = EmeraldProfit,
    onTertiary = CleanWhite,
    tertiaryContainer = EmeraldProfitContainer,
    onTertiaryContainer = EmeraldProfitOnContainer,
    background = CharcoalBlackBg,
    onBackground = TextWhitePrimary,
    surface = CharcoalCardSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = CharcoalCardElevated,
    onSurfaceVariant = TextWhiteSecondary,
    outline = CharcoalCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force brand charcoal black identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BrandDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
