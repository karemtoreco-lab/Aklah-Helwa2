package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Charcoal Black Palette
val CharcoalBlackBg = Color(0xFF121212)
val CharcoalSurfaceDark = Color(0xFF1A1A1A)
val CharcoalCardSurface = Color(0xFF222222)
val CharcoalCardElevated = Color(0xFF2A2A2A)
val CharcoalCardBorder = Color(0xFF383838)
val CharcoalDivider = Color(0xFF2E2E2E)

// Rich Vibrant Red (Matching brand red brush strokes)
val BrandRedPrimary = Color(0xFFD32F2F) // Vibrant Culinary Red
val BrandRedLight = Color(0xFFFF5252)
val BrandRedDark = Color(0xFFB71C1C)
val BrandRedContainer = Color(0xFF381212)
val BrandRedOnContainer = Color(0xFFFFCDD2)

// Clean White Secondary
val CleanWhite = Color(0xFFFFFFFF)
val TextWhitePrimary = Color(0xFFFFFFFF)
val TextWhiteSecondary = Color(0xFFCCCCCC)
val TextWhiteMuted = Color(0xFF888888)

// Deep Forest & Emerald Green for Profit & Success
val ForestEmerald = Color(0xFF2E7D32)
val EmeraldProfit = Color(0xFF43A047)
val EmeraldLight = Color(0xFF66BB6A)
val EmeraldProfitContainer = Color(0xFF143016)
val EmeraldProfitOnContainer = Color(0xFFA5D6A7)

// Warm Amber for Pending
val AmberPending = Color(0xFFFFA000)
val AmberPendingContainer = Color(0xFF382500)
val AmberPendingText = Color(0xFFFFD54F)

// Backward compatible aliases
val ProfitGreen = EmeraldProfit
val ProfitGreenContainer = EmeraldProfitContainer
val ProfitGreenText = EmeraldProfitOnContainer
val CoralOrangePrimary = BrandRedPrimary
val GoldenHoneySecondary = AmberPending
val WarmCardBorder = CharcoalCardBorder
val WarmCardSurface = CharcoalCardSurface


// Error & Expense Crimson
val ExpenseCrimson = Color(0xFFE53935)
val ExpenseCrimsonContainer = Color(0xFF3E1212)

// Premium Gradient Brushes
val RedGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF3D00), Color(0xFFD32F2F), Color(0xFFB71C1C))
)

val RedCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF2E1A1A), Color(0xFF1E1E1E))
)

val DarkHeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF2A1515), Color(0xFF141414))
)

val EmeraldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF43A047), Color(0xFF2E7D32))
)

val GoldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFFD54F), Color(0xFFFFA000))
)
