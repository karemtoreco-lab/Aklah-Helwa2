package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.ui.theme.AppTheme
import com.example.util.ImageUtils
import java.io.File

private data class CategoryVisualStyle(
    val gradientColors: List<Color>,
    val accentColor: Color,
    val icon: ImageVector
)

@Composable
private fun rememberCategoryVisualStyle(categoryName: String, itemName: String): CategoryVisualStyle {
    val combined = "$categoryName $itemName"
    return remember(combined) {
        when {
            combined.contains("محاشي") || combined.contains("محشي") || combined.contains("ورق عنب") || combined.contains("كرنب") ->
                CategoryVisualStyle(
                    gradientColors = listOf(Color(0xFF1A4332), Color(0xFF2D6A4F), Color(0xFFD97B1C)),
                    accentColor = Color(0xFFFFD166),
                    icon = Icons.Default.Spa
                )
            combined.contains("قبل الحشو") || combined.contains("متقورة") || combined.contains("مقور") ->
                CategoryVisualStyle(
                    gradientColors = listOf(Color(0xFF3E311B), Color(0xFF8C5D19), Color(0xFFD9902B)),
                    accentColor = Color(0xFFFFDE8A),
                    icon = Icons.Default.Restaurant
                )
            combined.contains("خضار") || combined.contains("سبانخ") || combined.contains("فاصوليا") || combined.contains("بامية") || combined.contains("بسلة") ->
                CategoryVisualStyle(
                    gradientColors = listOf(Color(0xFF103E2E), Color(0xFF1B6E4E), Color(0xFF4CA456)),
                    accentColor = Color(0xFFA7F3D0),
                    icon = Icons.Default.Eco
                )
            combined.contains("تتبيلات") || combined.contains("بانيه") || combined.contains("استربس") || combined.contains("شيش") || combined.contains("شاورما") || combined.contains("كفتة") || combined.contains("حواوشي") ->
                CategoryVisualStyle(
                    gradientColors = listOf(Color(0xFF4A180E), Color(0xFFA63612), Color(0xFFFF8C24)),
                    accentColor = Color(0xFFFFD580),
                    icon = Icons.Default.LocalFireDepartment
                )
            combined.contains("فراخ") || combined.contains("دباس") || combined.contains("وراك") || combined.contains("أجنحة") ->
                CategoryVisualStyle(
                    gradientColors = listOf(Color(0xFF49290C), Color(0xFF9E5614), Color(0xFFEFA133)),
                    accentColor = Color(0xFFFFECC2),
                    icon = Icons.Default.SetMeal
                )
            combined.contains("صلصات") || combined.contains("صلصة") || combined.contains("تسيكة") ->
                CategoryVisualStyle(
                    gradientColors = listOf(Color(0xFF4F1319), Color(0xFF991F28), Color(0xFFE05232)),
                    accentColor = Color(0xFFFFC8A2),
                    icon = Icons.Default.SoupKitchen
                )
            else ->
                CategoryVisualStyle(
                    gradientColors = listOf(Color(0xFF232936), Color(0xFF6B3410), Color(0xFFE65100)),
                    accentColor = Color(0xFFFFD180),
                    icon = Icons.Default.Restaurant
                )
        }
    }
}

/**
 * Rich Category-Tailored Default Dish Illustration used strictly when no real photo is available.
 */
@Composable
fun CategoryDishArtwork(
    categoryName: String,
    itemName: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp
) {
    val style = rememberCategoryVisualStyle(categoryName, itemName)
    Box(
        modifier = modifier
            .background(Brush.linearGradient(style.gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val minDim = size.minDimension
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = minDim * 0.42f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = style.accentColor.copy(alpha = 0.22f),
                radius = minDim * 0.31f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = minDim * 0.24f,
                center = Offset(size.width * 0.18f, size.height * 0.18f)
            )
        }
        Surface(
            color = Color.Black.copy(alpha = 0.28f),
            shape = CircleShape,
            modifier = Modifier
                .size(iconSize * 1.48f)
                .border(1.dp, style.accentColor.copy(alpha = 0.45f), CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = itemName,
                    tint = style.accentColor,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

/**
 * Unified Smart Dish Image renderer:
 * - Decodes `"data:image/..."` or Base64 strings into a cached [ImageBitmap] (`ContentScale.Crop`).
 * - Loads `http://`, `https://`, or internal file paths via Coil [AsyncImage].
 * - Displays the category-tailored [CategoryDishArtwork] ONLY when the image source is empty or fails to load.
 */
@Composable
fun SmartDishImage(
    imageSource: String?,
    contentDescription: String,
    categoryName: String = "",
    fallbackIconSize: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cleanSource = remember(imageSource) {
        imageSource?.trim()?.takeIf {
            it.isNotBlank() && !it.startsWith("content://", ignoreCase = true)
        }
    }

    val isRemoteUrl = remember(cleanSource) {
        cleanSource != null && (
            cleanSource.startsWith("http://", ignoreCase = true) ||
                cleanSource.startsWith("https://", ignoreCase = true)
            )
    }
    val isLocalFile = remember(cleanSource) {
        cleanSource != null && cleanSource.startsWith("/") && File(cleanSource).exists()
    }
    val isBase64 = remember(cleanSource, isRemoteUrl, isLocalFile) {
        cleanSource != null && !isRemoteUrl && !isLocalFile && ImageUtils.isBase64OrDataUri(cleanSource)
    }

    val decodedBitmap: ImageBitmap? by produceState(
        initialValue = if (isBase64) ImageUtils.getCachedImageBitmap(cleanSource) else null,
        key1 = cleanSource,
        key2 = isBase64
    ) {
        value = if (isBase64) ImageUtils.decodeToImageBitmap(cleanSource) else null
    }

    var coilLoadFailed by remember(cleanSource) { mutableStateOf(false) }

    val coilRequest = remember(cleanSource, isRemoteUrl, isLocalFile) {
        when {
            isRemoteUrl -> ImageRequest.Builder(context)
                .data(cleanSource)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            isLocalFile -> ImageRequest.Builder(context)
                .data(File(cleanSource!!))
                .size(640)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            else -> null
        }
    }

    when {
        decodedBitmap != null -> {
            Image(
                bitmap = decodedBitmap!!,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        coilRequest != null && !coilLoadFailed -> {
            AsyncImage(
                model = coilRequest,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                onError = { coilLoadFailed = true },
                modifier = modifier
            )
        }
        else -> {
            CategoryDishArtwork(
                categoryName = categoryName,
                itemName = contentDescription,
                iconSize = fallbackIconSize,
                modifier = modifier
            )
        }
    }
}

/**
 * Displays either a category-tailored gourmet visual when [photos] is empty,
 * or a swipeable HorizontalPager (1–3 photos) backed by [SmartDishImage] with page indicator dots.
 */
@Composable
fun DishPhotoCarousel(
    photos: List<String>,
    categoryName: String,
    itemName: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    fallbackIconSize: Dp = 34.dp
) {
    val colors = AppTheme.colors
    val validPhotos = remember(photos) {
        photos.filter { it.isNotBlank() && !it.startsWith("content://", ignoreCase = true) }.take(3)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.secondarySurface)
    ) {
        if (validPhotos.isEmpty()) {
            CategoryDishArtwork(
                categoryName = categoryName,
                itemName = itemName,
                iconSize = fallbackIconSize,
                modifier = Modifier.fillMaxSize()
            )
        } else if (validPhotos.size == 1) {
            SmartDishImage(
                imageSource = validPhotos.first(),
                contentDescription = itemName,
                categoryName = categoryName,
                fallbackIconSize = fallbackIconSize,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { validPhotos.size })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                SmartDishImage(
                    imageSource = validPhotos[page],
                    contentDescription = "$itemName - صورة ${page + 1}",
                    categoryName = categoryName,
                    fallbackIconSize = fallbackIconSize,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.52f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(validPhotos.size) { idx ->
                    val dotColor by animateColorAsState(
                        targetValue = if (pagerState.currentPage == idx) colors.goldAccent else Color.White.copy(alpha = 0.45f),
                        animationSpec = tween(200),
                        label = "pager_dot"
                    )
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == idx) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

@Composable
fun CachedBase64DishImage(
    base64OrPath: String,
    contentDescription: String,
    categoryName: String = "",
    fallbackIconSize: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    SmartDishImage(
        imageSource = base64OrPath,
        contentDescription = contentDescription,
        categoryName = categoryName,
        fallbackIconSize = fallbackIconSize,
        modifier = modifier
    )
}
