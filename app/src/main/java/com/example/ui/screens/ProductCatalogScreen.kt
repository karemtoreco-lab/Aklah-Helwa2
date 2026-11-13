package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.model.CategoryEntity
import com.example.model.ProductEntity
import com.example.model.ProductImageEntity
import com.example.ui.components.CulinaryPillChip
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.StaggeredAppearItem
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductCatalogScreen(
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    primaryImages: Map<Long, String>,
    imagesByProduct: Map<Long, List<ProductImageEntity>>,
    onSaveCategory: (id: Long, name: String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onSaveProduct: (
        id: Long,
        name: String,
        categoryId: Long,
        unit: String,
        description: String,
        purchaseCost: Double,
        defaultSellingPrice: Double,
        isActive: Boolean
    ) -> Unit,
    onToggleProductActive: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onAddImagesForProduct: (productId: Long, uris: List<Uri>) -> Unit,
    onSetPrimaryImage: (productId: Long, imageId: Long) -> Unit,
    onDeleteImage: (ProductImageEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    var subTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showCategoriesDialog by remember { mutableStateOf(false) }
    var deletingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var pickerTargetProductId by remember { mutableLongStateOf(0L) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        if (uris.isNotEmpty() && pickerTargetProductId > 0L) {
            onAddImagesForProduct(pickerTargetProductId, uris)
        }
    }

    val categoryMap by remember(categories) {
        derivedStateOf { categories.associateBy { it.id } }
    }
    val filteredProducts by remember(products, searchQuery, selectedCategoryId) {
        derivedStateOf {
            products.filter { prod ->
                (selectedCategoryId == null || prod.categoryId == selectedCategoryId) &&
                    (searchQuery.isBlank() || prod.name.contains(searchQuery, ignoreCase = true))
            }
        }
    }

    if (showProductDialog) {
        ProductEditorDialog(
            initialProduct = editingProduct,
            categories = categories,
            onSave = { id, name, catId, unit, desc, purchase, selling, active ->
                onSaveProduct(id, name, catId, unit, desc, purchase, selling, active)
                showProductDialog = false
                editingProduct = null
            },
            onDismiss = {
                showProductDialog = false
                editingProduct = null
            }
        )
    }

    if (showCategoriesDialog) {
        ManageCategoriesDialog(
            categories = categories,
            onSaveCategory = onSaveCategory,
            onDeleteCategory = onDeleteCategory,
            onDismiss = { showCategoriesDialog = false }
        )
    }

    deletingProduct?.let { prod ->
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            containerColor = colors.cardSurface,
            title = { Text("حذف الصنف", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "هل تريد حذف \"${prod.name}\" من الكتالوج؟ لن تتأثر الأوردرات السابقة المحفوظة.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(prod)
                        deletingProduct = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand)
                ) {
                    Text("حذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProduct = null }) {
                    Text("إلغاء", color = colors.textSecondary)
                }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("product_catalog_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Pill Segmented Bar
            Surface(
                color = colors.cardSurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    SegmentedTabButton(
                        title = "إدارة الأصناف (${products.size})",
                        icon = Icons.Default.Inventory2,
                        selected = subTab == 0,
                        onClick = { subTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtab_products")
                    )
                    SegmentedTabButton(
                        title = "معرض الصور",
                        icon = Icons.Default.Collections,
                        selected = subTab == 1,
                        onClick = { subTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtab_gallery")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن صنف...", color = colors.textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primaryBrand) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_search_input"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardSurface,
                    unfocusedContainerColor = colors.cardSurface,
                    focusedBorderColor = colors.primaryBrand,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Responsive FlowRow Category Chips
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

            if (subTab == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            editingProduct = null
                            showProductDialog = true
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .defaultMinSize(minHeight = 48.dp)
                            .bounceClick()
                            .testTag("add_product_btn"),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("صنف جديد", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showCategoriesDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .bounceClick()
                            .testTag("manage_categories_btn"),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = colors.secondaryAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الأقسام", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 124.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(filteredProducts, key = { _, p -> p.id }) { index, prod ->
                        val catName = categoryMap[prod.categoryId]?.name.orEmpty()
                        val imgPath = primaryImages[prod.id]

                        StaggeredAppearItem(index = index) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .shadow(3.dp, RoundedCornerShape(20.dp))
                                    .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                                    .bounceClick {
                                        editingProduct = prod
                                        showProductDialog = true
                                    }
                                    .testTag("product_card_${prod.id}"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.cardSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ProductThumbnail(
                                            imagePath = imgPath,
                                            contentDescription = prod.name,
                                            size = 62.dp,
                                            cornerRadius = 16.dp,
                                            modifier = Modifier.clickable {
                                                pickerTargetProductId = prod.id
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prod.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$catName • ${prod.unit}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.textSecondary
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Surface(
                                                    color = colors.primaryBrand,
                                                    shape = RoundedCornerShape(50)
                                                ) {
                                                    Text(
                                                        text = if (prod.defaultSellingPrice > 0.0) {
                                                            DateUtils.formatCurrency(prod.defaultSellingPrice)
                                                        } else "حسب السعر",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                                    )
                                                }
                                                Surface(
                                                    color = colors.secondarySurface,
                                                    shape = RoundedCornerShape(50),
                                                    border = BorderStroke(1.dp, colors.cardBorder)
                                                ) {
                                                    Text(
                                                        text = "شراء: ${DateUtils.formatCurrency(prod.purchaseCost)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = colors.textSecondary,
                                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Switch(
                                            checked = prod.isActive,
                                            onCheckedChange = { onToggleProductActive(prod) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        TextButton(
                                            onClick = {
                                                pickerTargetProductId = prod.id
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("إضافة صورة", fontSize = 12.sp, color = colors.goldAccent, fontWeight = FontWeight.Bold)
                                        }
                                        TextButton(
                                            onClick = {
                                                editingProduct = prod
                                                showProductDialog = true
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تعديل", fontSize = 12.sp, color = colors.textPrimary)
                                        }
                                        IconButton(
                                            onClick = { deletingProduct = prod },
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف الصنف", tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Sub-tab 1: معرض الصور with Coil downsampling
                val totalImagesCount = imagesByProduct.values.sumOf { it.size }
                Surface(
                    color = colors.cardSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (totalImagesCount == 0) {
                            "معرض الصور فارغ حالياً — ارفع صور الأصناف من جهازك"
                        } else {
                            "إجمالي الصور المحفوظة: $totalImagesCount صورة"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 124.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(filteredProducts, key = { _, p -> p.id }) { index, prod ->
                        val imgs = imagesByProduct[prod.id].orEmpty()
                        val catName = categoryMap[prod.categoryId]?.name.orEmpty()

                        StaggeredAppearItem(index = index) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                                    .testTag("gallery_product_${prod.id}"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.cardSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                            Text("$catName • ${imgs.size} صورة", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                                        }
                                        Button(
                                            onClick = {
                                                pickerTargetProductId = prod.id
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp)
                                                .bounceClick()
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("رفع صور", color = Color.White, fontSize = 12.sp)
                                        }
                                    }

                                    if (imgs.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            items(imgs, key = { it.id }) { img ->
                                                val imgRequest = remember(img.imagePath) {
                                                    ImageRequest.Builder(context)
                                                        .data(File(img.imagePath))
                                                        .size(240)
                                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .crossfade(true)
                                                        .build()
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(112.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .border(
                                                            width = if (img.isPrimary) 2.dp else 1.dp,
                                                            color = if (img.isPrimary) colors.profitGreen else colors.cardBorder,
                                                            shape = RoundedCornerShape(16.dp)
                                                        )
                                                ) {
                                                    AsyncImage(
                                                        model = imgRequest,
                                                        contentDescription = prod.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clickable { onSetPrimaryImage(prod.id, img.id) }
                                                    )

                                                    IconButton(
                                                        onClick = { onDeleteImage(img) },
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(4.dp)
                                                            .size(28.dp)
                                                            .background(colors.cardSurface.copy(alpha = 0.9f), CircleShape)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "حذف الصورة", tint = colors.primaryBrand, modifier = Modifier.size(15.dp))
                                                    }

                                                    Surface(
                                                        color = if (img.isPrimary) colors.profitContainer else colors.cardSurface.copy(alpha = 0.92f),
                                                        shape = RoundedCornerShape(50),
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .padding(4.dp)
                                                            .clickable { onSetPrimaryImage(prod.id, img.id) }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = if (img.isPrimary) colors.profitGreen else colors.goldAccent,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = if (img.isPrimary) "رئيسية" else "تعيين رئيسية",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (img.isPrimary) colors.onProfitContainer else colors.textPrimary
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
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedTabButton(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val bgColor by animateColorAsState(
        targetValue = if (selected) colors.primaryBrand else colors.secondarySurface.copy(alpha = 0.65f),
        animationSpec = tween(220),
        label = "seg_bg"
    )

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(50))
            .bounceClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else colors.textSecondary,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
