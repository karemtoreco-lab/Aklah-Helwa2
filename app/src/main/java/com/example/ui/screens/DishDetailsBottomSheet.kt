package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MenuItemEntity
import com.example.ui.components.CulinaryQuantityStepper
import com.example.ui.components.DishPhotoCarousel
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import com.example.util.WhatsAppUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DishDetailsBottomSheet(
    menuItem: MenuItemEntity,
    categoryName: String,
    photos: List<String>,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onAddQuantityToCart: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var quantity by remember(menuItem.id) { mutableDoubleStateOf(1.0) }
    val favScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.16f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sheet_fav_bounce"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardSurface,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("dish_details_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                DishPhotoCarousel(
                    photos = photos,
                    categoryName = categoryName,
                    itemName = menuItem.displayName,
                    cornerRadius = 24.dp,
                    fallbackIconSize = 54.dp,
                    modifier = Modifier
                        .matchParentSize()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(24.dp))
                )

                // Subtle bottom scrim for depth
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.25f))
                            )
                        )
                )

                // Top Badges Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (menuItem.isPopular) {
                        Surface(
                            color = colors.goldAccent,
                            shape = RoundedCornerShape(50),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFF1A1614), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الأكثر طلباً", color = Color(0xFF1A1614), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleFavorite()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer {
                                scaleX = favScale
                                scaleY = favScale
                            }
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.56f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                            .testTag("dish_sheet_favorite_btn")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة",
                            tint = if (isFavorite) Color(0xFFFF4D6D) else Color.White
                        )
                    }
                }
            }

            // Title & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (categoryName.isNotBlank()) {
                        Surface(
                            color = colors.primaryContainer,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = menuItem.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    color = colors.secondaryContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        val ratingText = String.format(Locale.US, "%.1f", menuItem.ratingAvg)
                        val countSuffix = if (menuItem.ratingCount > 0) " (${menuItem.ratingCount})" else ""
                        Text(
                            text = "$ratingText$countSuffix",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.goldAccent
                        )
                    }
                }
            }

            // Glowing Price + Prep Badge Row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = colors.primaryContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colors.primaryBrand.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = if (menuItem.isVariablePrice && menuItem.publishedPrice <= 0.0) {
                            "السعر حسب الوزن واليوم"
                        } else {
                            "${DateUtils.formatCurrency(menuItem.publishedPrice)} / ${menuItem.unitLabel}"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }

                Surface(
                    color = if (menuItem.isPreOrder24h) colors.secondaryContainer else colors.profitContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp,
                        if (menuItem.isPreOrder24h) colors.goldAccent.copy(alpha = 0.4f) else colors.profitGreen.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (menuItem.isPreOrder24h) colors.goldAccent else colors.profitGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (menuItem.isPreOrder24h) "حجز مسبق 24 ساعة" else "جاهز للتسليم",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (menuItem.isPreOrder24h) colors.goldAccent else colors.onProfitContainer
                        )
                    }
                }
            }

            Surface(
                color = colors.secondarySurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = menuItem.description.ifBlank {
                        "تجهيز بيتي طازج يومياً بمكونات طبيعية مضمونة ونظافة تامة من مطبخ أ/ نهى ماهر."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }

            HorizontalDivider(color = colors.cardBorder)

            // Quantity Stepper + Add to Cart CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CulinaryQuantityStepper(
                    quantityText = WhatsAppUtils.formatQuantity(quantity),
                    onDecrease = { if (quantity > 0.5) quantity -= 0.5 },
                    onIncrease = { quantity += 0.5 }
                )

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddQuantityToCart(quantity)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 52.dp)
                        .shadow(8.dp, RoundedCornerShape(50), spotColor = colors.primaryBrand.copy(alpha = 0.45f))
                        .bounceClick()
                        .testTag("dish_sheet_add_to_cart_btn")
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    val lineSubtotal = if (menuItem.isVariablePrice && menuItem.publishedPrice <= 0.0) {
                        ""
                    } else {
                        " • ${DateUtils.formatCurrency(quantity * menuItem.publishedPrice)}"
                    }
                    Text(
                        text = "أضف للسلة$lineSubtotal",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
