package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun CheckeredRibbon(
    modifier: Modifier = Modifier,
    height: Dp = 3.dp,
    @Suppress("UNUSED_PARAMETER") squareSize: Dp = 4.dp,
    primaryColor: Color = AppTheme.colors.primaryBrand,
    @Suppress("UNUSED_PARAMETER") secondaryColor: Color = AppTheme.colors.goldAccent
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.coerceAtMost(3.dp))
            .background(primaryColor)
    )
}
