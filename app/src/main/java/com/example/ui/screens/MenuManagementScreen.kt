package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.sp
import com.example.model.GeneratedMenuEntity
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import com.example.model.ProductEntity
import com.example.ui.components.CachedBase64DishImage
import com.example.ui.components.CategoryDishArtwork
import com.example.ui.components.StaggeredAppearItem
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MenuManagementScreen(
    menuCategories: List<MenuCategoryEntity>,
    menuItems: List<MenuItemEntity>,
    catalogProducts: List<ProductEntity>,
    primaryImages: Map<Long, String>,
    previewBitmaps: List<Bitmap>,
    selectedPreviewCategoryId: Long?,
    isGeneratingPreview: Boolean,
    generatedMenus: List<GeneratedMenuEntity>,
    onAddMenuCategory: (String) -> Unit,
    onUpdateMenuCategory: (MenuCategoryEntity) -> Unit,
    onDeleteMenuCategory: (MenuCategoryEntity) -> Unit,
    onMoveMenuCategory: (MenuCategoryEntity, Boolean) -> Unit,
    onAddCatalogProductToMenu: (ProductEntity, Long) -> Unit,
    onAddManualMenuItem: (Long, String, String, String, Double, Boolean, Boolean, Boolean) -> Unit,
    onUpdateMenuItem: (MenuItemEntity) -> Unit,
    onRestoreDefaultPrice: (MenuItemEntity) -> Unit,
    onDeleteMenuItem: (MenuItemEntity) -> Unit,
    onMoveMenuItem: (MenuItemEntity, Boolean) -> Unit,
    onAddMenuItemPhotos: (MenuItemEntity, List<Uri>) -> Unit,
    onRemoveMenuItemPhoto: (MenuItemEntity, Int) -> Unit,
    onSelectPreviewCategory: (Long?) -> Unit,
    onRefreshPreview: () -> Unit,
    onExportAllPages: ((List<String>) -> Unit) -> Unit,
    onExportSinglePage: (Int, (List<String>) -> Unit) -> Unit,
    onDeleteGeneratedMenu: (GeneratedMenuEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var subTab by remember { mutableIntStateOf(0) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<MenuCategoryEntity?>(null) }
    var editingMenuItemId by remember { mutableStateOf<Long?>(null) }
    var quickPhotoTargetItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var showPickCatalogDialog by remember { mutableStateOf(false) }
    var pickCatalogTargetMenuCatId by remember { mutableStateOf<Long?>(null) }
    var showAddManualDialog by remember { mutableStateOf(false) }
    var removingMenuItem by remember { mutableStateOf<MenuItemEntity?>(null) }

    val quickPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris ->
        val target = quickPhotoTargetItem
        quickPhotoTargetItem = null
        if (target != null && uris.isNotEmpty()) {
            onAddMenuItemPhotos(target, uris)
        }
    }

    val catalogMap by remember(catalogProducts) {
        derivedStateOf { catalogProducts.associateBy { it.id } }
    }
    val sortedCategories by remember(menuCategories) {
        derivedStateOf { menuCategories.sortedBy { it.sortOrder } }
    }
    val itemsByCat by remember(menuItems) {
        derivedStateOf { menuItems.sortedBy { it.sortOrder }.groupBy { it.menuCategoryId } }
    }
    val liveEditingItem = remember(editingMenuItemId, menuItems) {
        editingMenuItemId?.let { id -> menuItems.find { it.id == id } }
    }

    if (showCategoryDialog) {
        MenuCategoryEditorDialog(
            initialCategory = editingCategory,
            onSave = { name ->
                val target = editingCategory
                if (target == null) onAddMenuCategory(name) else onUpdateMenuCategory(target.copy(name = name))
                showCategoryDialog = false
                editingCategory = null
            },
            onDismiss = {
                showCategoryDialog = false
                editingCategory = null
            }
        )
    }

    liveEditingItem?.let { item ->
        val linked = item.productId?.let { catalogMap[it] }
        MenuItemEditorDialog(
            item = item,
            linkedProduct = linked,
            onSave = { updated ->
                onUpdateMenuItem(updated)
                editingMenuItemId = null
            },
            onRestoreDefaultPrice = {
                onRestoreDefaultPrice(item)
                editingMenuItemId = null
            },
            onAddPhotos = { uris -> onAddMenuItemPhotos(item, uris) },
            onRemovePhotoAt = { idx -> onRemoveMenuItemPhoto(item, idx) },
            onDismiss = { editingMenuItemId = null }
        )
    }

    if (showPickCatalogDialog) {
        AddCatalogProductToMenuDialog(
            menuCategories = sortedCategories,
            products = catalogProducts,
            primaryImages = primaryImages,
            initialMenuCategoryId = pickCatalogTargetMenuCatId,
            onAddProduct = { prod, menuCatId -> onAddCatalogProductToMenu(prod, menuCatId) },
            onDismiss = {
                showPickCatalogDialog = false
                pickCatalogTargetMenuCatId = null
            }
        )
    }

    if (showAddManualDialog) {
        AddManualMenuItemDialog(
            menuCategories = sortedCategories,
            onAddManual = onAddManualMenuItem,
            onDismiss = { showAddManualDialog = false }
        )
    }

    removingMenuItem?.let { item ->
        AlertDialog(
            onDismissRequest = { removingMenuItem = null },
            containerColor = colors.cardSurface,
            title = { Text("إزالة من المنيو", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "سيتم إزالة \"${item.displayName}\" من المنيو المنشور فقط. لن يتم حذف الصنف من كتالوج الأصناف أو الأوردرات السابقة.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMenuItem(item)
                        removingMenuItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand)
                ) {
                    Text("إزالة من المنيو", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { removingMenuItem = null }) { Text("إلغاء", color = colors.textSecondary) }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundGradient),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("menu_management_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Pill Segmented Switcher
            Surface(
                color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    SegmentedTabButton(
                        title = "إدارة المنيو (${menuItems.size})",
                        icon = Icons.Default.RestaurantMenu,
                        selected = subTab == 0,
                        onClick = { subTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtab_manage_menu")
                    )
                    SegmentedTabButton(
                        title = "معاينة المنيو",
                        icon = Icons.Default.Image,
                        selected = subTab == 1,
                        onClick = {
                            subTab = 1
                            onRefreshPreview()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtab_preview_menu")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (subTab == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 50.dp)
                        .wrapContentHeight()
                        .shadow(6.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(colors.warmAccentGradient)
                        .bounceClick {
                            subTab = 1
                            onExportAllPages {}
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .testTag("save_and_export_menu_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ وتصدير المنيو", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            pickCatalogTargetMenuCatId = sortedCategories.firstOrNull()?.id
                            showPickCatalogDialog = true
                        },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .bounceClick()
                            .testTag("add_catalog_item_to_menu_btn"),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("من الأصناف", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showAddManualDialog = true },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .bounceClick()
                            .testTag("add_manual_menu_item_btn"),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("صنف يدوي", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            editingCategory = null
                            showCategoryDialog = true
                        },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .bounceClick()
                            .testTag("add_menu_category_btn"),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = colors.secondaryAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("قسم جديد", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 124.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(sortedCategories, key = { _, c -> c.id }) { catIdx, cat ->
                        val catItems = itemsByCat[cat.id].orEmpty()

                        StaggeredAppearItem(index = catIdx) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (colors.isDark) colors.glassBorder else colors.cardBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .testTag("menu_category_card_${cat.id}"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(colors.warmAccentGradient, RoundedCornerShape(50))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${cat.name} (${catItems.size})",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onMoveMenuCategory(cat, true) },
                                                enabled = catIdx > 0,
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowUpward, contentDescription = "لأعلى", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { onMoveMenuCategory(cat, false) },
                                                enabled = catIdx < sortedCategories.lastIndex,
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowDownward, contentDescription = "لأسفل", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { onUpdateMenuCategory(cat.copy(isVisible = !cat.isVisible)) },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (cat.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "إظهار/إخفاء القسم",
                                                    tint = if (cat.isVisible) colors.goldAccent else colors.textSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    editingCategory = cat
                                                    showCategoryDialog = true
                                                },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "تعديل القسم", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = { onDeleteMenuCategory(cat) },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف القسم", tint = colors.primaryBrand, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (catItems.isEmpty()) {
                                        Text("لا توجد أصناف منشورة في هذا القسم حالياً", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            catItems.forEachIndexed { idx, item ->
                                                val photos = item.photoList()
                                                val localThumb = photos.firstOrNull() ?: item.productId?.let { primaryImages[it] }
                                                Surface(
                                                    color = colors.secondarySurface,
                                                    shape = RoundedCornerShape(16.dp),
                                                    border = BorderStroke(1.dp, colors.cardBorder),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight()
                                                        .bounceClick { editingMenuItemId = item.id }
                                                        .testTag("menu_item_row_${item.id}")
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 9.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                                                        ) {
                                                            if (!localThumb.isNullOrBlank()) {
                                                                CachedBase64DishImage(
                                                                    base64OrPath = localThumb,
                                                                    contentDescription = item.displayName,
                                                                    categoryName = cat.name,
                                                                    fallbackIconSize = 20.dp,
                                                                    modifier = Modifier.fillMaxSize()
                                                                )
                                                            } else {
                                                                CategoryDishArtwork(
                                                                    categoryName = cat.name,
                                                                    itemName = item.displayName,
                                                                    iconSize = 20.dp,
                                                                    modifier = Modifier.fillMaxSize()
                                                                )
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = item.displayName,
                                                                color = if (item.isVisible) colors.textPrimary else colors.textSecondary,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Row(
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = "\uD83D\uDCF7 ${photos.size}/3",
                                                                    fontSize = 10.sp,
                                                                    color = if (photos.isNotEmpty()) colors.profitGreen else colors.textSecondary,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                if (item.isPopular) {
                                                                    Text("مميز \uD83D\uDD25", fontSize = 10.sp, color = colors.goldAccent, fontWeight = FontWeight.Bold)
                                                                }
                                                                if (item.isPreOrder24h) {
                                                                    Text("24 ساعة", fontSize = 10.sp, color = colors.secondaryAccent)
                                                                }
                                                                if (!item.isVisible) {
                                                                    Text("مخفي", fontSize = 10.sp, color = colors.primaryBrand)
                                                                }
                                                            }
                                                        }

                                                        Surface(
                                                            color = colors.primaryBrand,
                                                            shape = RoundedCornerShape(50),
                                                            modifier = Modifier.padding(horizontal = 4.dp)
                                                        ) {
                                                            Text(
                                                                text = if (item.isVariablePrice && item.publishedPrice <= 0.0) {
                                                                    "حسب السعر"
                                                                } else {
                                                                    DateUtils.formatCurrency(item.publishedPrice)
                                                                },
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            )
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                quickPhotoTargetItem = item
                                                                quickPhotoPicker.launch(
                                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                                )
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.AddPhotoAlternate,
                                                                contentDescription = "رفع صور للطيق",
                                                                tint = colors.goldAccent,
                                                                modifier = Modifier.size(17.dp)
                                                            )
                                                        }

                                                        IconButton(
                                                            onClick = { onMoveMenuItem(item, true) },
                                                            enabled = idx > 0,
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.ArrowUpward, contentDescription = "لأعلى", tint = colors.textPrimary, modifier = Modifier.size(14.dp))
                                                        }
                                                        IconButton(
                                                            onClick = { onMoveMenuItem(item, false) },
                                                            enabled = idx < catItems.lastIndex,
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.ArrowDownward, contentDescription = "لأسفل", tint = colors.textPrimary, modifier = Modifier.size(14.dp))
                                                        }
                                                        IconButton(
                                                            onClick = { removingMenuItem = item },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "إزالة من المنيو", tint = colors.primaryBrand, modifier = Modifier.size(15.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp), color = colors.cardBorder)
                                    TextButton(
                                        onClick = {
                                            pickCatalogTargetMenuCatId = cat.id
                                            showPickCatalogDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("إضافة صنف لهذا القسم", color = colors.primaryBrand, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                MenuPreviewSection(
                    menuCategories = sortedCategories,
                    previewBitmaps = previewBitmaps,
                    selectedPreviewCategoryId = selectedPreviewCategoryId,
                    isGenerating = isGeneratingPreview,
                    generatedMenus = generatedMenus,
                    onSelectCategoryFilter = onSelectPreviewCategory,
                    onRefreshPreview = onRefreshPreview,
                    onExportAllPages = { onDone -> onExportAllPages(onDone) },
                    onExportSinglePage = { pageIdx, onDone -> onExportSinglePage(pageIdx, onDone) },
                    onDeleteGeneratedMenu = onDeleteGeneratedMenu,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
