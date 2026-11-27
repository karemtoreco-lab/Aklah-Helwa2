package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CategoryEntity
import com.example.model.ProductEntity
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductPickerDialog(
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    primaryImages: Map<Long, String>,
    onSelectProduct: (ProductEntity, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var query by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    val filteredProducts by remember(products, query, selectedCategoryId) {
        derivedStateOf {
            products.filter { prod ->
                prod.isActive &&
                    (selectedCategoryId == null || prod.categoryId == selectedCategoryId) &&
                    (query.isBlank() || prod.name.contains(query, ignoreCase = true))
            }
        }
    }
    val categoryMap by remember(categories) {
        derivedStateOf { categories.associateBy { it.id } }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(14.dp)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .heightIn(max = 660.dp)
                .imePadding()
                .shadow(18.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .border(1.2.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(28.dp))
                .testTag("product_picker_dialog"),
            color = colors.cardSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.warmAccentGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = colors.onAccentText, modifier = Modifier.size(19.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "اختر من الأصناف لإضافته للأوردر",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.secondarySurface)
                            .border(1.dp, colors.cardBorder, CircleShape)
                            .bounceClick(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("ابحث باسم الصنف...", color = colors.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primaryBrand) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("picker_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CulinaryPillChip(
                        label = "الكل",
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null }
                    )
                    categories.forEach { cat ->
                        CulinaryPillChip(
                            label = cat.name,
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    itemsIndexed(filteredProducts, key = { _, p -> p.id }) { idx, product ->
                        val imagePath = primaryImages[product.id]
                        val catName = categoryMap[product.categoryId]?.name.orEmpty()

                        StaggeredAppearItem(index = idx) {
                            Surface(
                                color = colors.secondarySurface,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, colors.cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .bounceClick { onSelectProduct(product, imagePath) }
                                    .testTag("picker_product_${product.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProductThumbnail(
                                        imagePath = imagePath,
                                        contentDescription = product.name,
                                        size = 56.dp,
                                        cornerRadius = 16.dp,
                                        modifier = Modifier.clickable { onSelectProduct(product, imagePath) }
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "$catName • ${product.unit}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(5.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(color = colors.primaryContainer, shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = if (product.defaultSellingPrice > 0.0) {
                                                        "بيع: ${DateUtils.formatCurrency(product.defaultSellingPrice)}"
                                                    } else {
                                                        "حسب السعر"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            if (product.purchaseCost > 0.0) {
                                                Surface(color = colors.profitContainer, shape = RoundedCornerShape(50)) {
                                                    Text(
                                                        text = "شراء: ${DateUtils.formatCurrency(product.purchaseCost)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.onProfitContainer,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = "إضافة",
                                        tint = colors.primaryBrand,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
