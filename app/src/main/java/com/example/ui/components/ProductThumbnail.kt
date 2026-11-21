package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun ProductThumbnail(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = 16.dp
) {
    val colors = AppTheme.colors
    val innerRadius = (cornerRadius - 2.dp).coerceAtLeast(8.dp)
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(cornerRadius), spotColor = colors.primaryBrand.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(colors.primaryBrand, colors.secondaryAccent, colors.goldAccent)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(2.dp)
            .clip(RoundedCornerShape(innerRadius))
            .background(colors.secondarySurface),
        contentAlignment = Alignment.Center
    ) {
        SmartDishImage(
            imageSource = imagePath,
            contentDescription = contentDescription ?: "صورة الصنف",
            fallbackIconSize = size * 0.38f,
            modifier = Modifier.fillMaxSize()
        )
    }
}
