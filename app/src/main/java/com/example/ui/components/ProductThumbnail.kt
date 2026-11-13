package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.ui.theme.AppTheme
import com.example.util.Base64ImageUtils
import java.io.File

@Composable
fun ProductThumbnail(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = 16.dp
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val isFilePath = remember(imagePath) {
        !imagePath.isNullOrBlank() && imagePath.startsWith("/") && File(imagePath).exists()
    }
    val isBase64 = remember(imagePath) {
        !imagePath.isNullOrBlank() && !imagePath.startsWith("/")
    }

    val decodedBase64: ImageBitmap? by produceState(
        initialValue = if (isBase64) Base64ImageUtils.getCachedBitmap(imagePath!!) else null,
        key1 = imagePath
    ) {
        if (isBase64) {
            value = Base64ImageUtils.decodeToImageBitmap(imagePath!!)
        }
    }

    val imageRequest = remember(imagePath, isFilePath) {
        if (isFilePath) {
            ImageRequest.Builder(context)
                .data(File(imagePath!!))
                .size(240)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        } else null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(colors.primaryBrand, colors.secondaryAccent, colors.goldAccent)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(1.5.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                if (isFilePath || decodedBase64 != null) {
                    Brush.linearGradient(listOf(colors.secondarySurface, colors.cardSurface))
                } else {
                    Brush.linearGradient(
                        listOf(
                            colors.primaryBrand.copy(alpha = 0.16f),
                            colors.secondaryAccent.copy(alpha = 0.18f),
                            colors.cardSurface
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            decodedBase64 != null -> {
                Image(
                    bitmap = decodedBase64!!,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            imageRequest != null -> {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = contentDescription,
                    tint = colors.primaryBrand,
                    modifier = Modifier.size(size * 0.45f)
                )
            }
        }
    }
}
