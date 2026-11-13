package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

/**
 * A subtle decorative red-and-white checkered ribbon (bistro / Egyptian homemade brand accent).
 */
@Composable
fun CheckeredRibbon(
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    squareSize: Dp = 6.dp,
    primaryColor: Color = AppTheme.colors.primaryBrand,
    secondaryColor: Color = AppTheme.colors.cardSurface.copy(alpha = 0.9f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val sizePx = squareSize.toPx()
        val numColumns = (size.width / sizePx).toInt() + 1
        val numRows = (size.height / sizePx).toInt().coerceAtLeast(1)

        for (row in 0 until numRows) {
            for (col in 0 until numColumns) {
                val isRed = (row + col) % 2 == 0
                val color = if (isRed) primaryColor else secondaryColor
                drawRect(
                    color = color,
                    topLeft = Offset(col * sizePx, row * sizePx),
                    size = Size(sizePx, sizePx)
                )
            }
        }
    }
}
