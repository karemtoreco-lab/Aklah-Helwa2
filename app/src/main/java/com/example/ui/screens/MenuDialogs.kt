package com.example.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import com.example.model.ProductEntity
import com.example.ui.components.CulinaryPillChip
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.SmartDishImage
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@Composable
fun MenuCategoryEditorDialog(
    initialCategory: MenuCategoryEntity? = null,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        modifier = Modifier.imePadding(),
        title = {
            Text(
                text = if (initialCategory == null) "إضافة قسم جديد للمنيو" else "تعديل قسم المنيو",
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("اسم القسم في المنيو") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("menu_category_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.secondarySurface,
                    unfocusedContainerColor = colors.secondarySurface,
                    focusedBorderColor = colors.primaryBrand,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name.trim()) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .bounceClick()
            ) {
                Text("حفظ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = colors.textSecondary) }
        }
    )
}

@Composable
fun MenuItemEditorDialog(
    item: MenuItemEntity,
    linkedProduct: ProductEntity?,
    onSave: (MenuItemEntity) -> Unit,
    onRestoreDefaultPrice: () -> Unit,
    onAddPhotos: (List<Uri>) -> Unit,
    onRemovePhotoAt: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var displayName by remember(item.id) { mutableStateOf(item.displayName) }
    var unitLabel by remember(item.id) { mutableStateOf(item.unitLabel) }
    var description by remember(item.id) { mutableStateOf(item.description) }
    var priceText by remember(item.id) {
        mutableStateOf(if (item.publishedPrice > 0.0) item.publishedPrice.toString().removeSuffix(".0") else "0")
    }
    var isVariablePrice by remember(item.id) { mutableStateOf(item.isVariablePrice) }
    var isVisible by remember(item.id) { mutableStateOf(item.isVisible) }
    var isPopular by remember(item.id) { mutableStateOf(item.isPopular) }
    var isPreOrder24h by remember(item.id) { mutableStateOf(item.isPreOrder24h) }

    val photos = remember(item.photosJson) { item.photoList() }
    val multiPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAddPhotos(uris)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        modifier = Modifier.imePadding(),
        title = { Text("تعديل صنف في المنيو", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Multi-photo gallery (up to 3 compressed Base64 Data URIs synced to Firestore)
                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "صور الطبق (${photos.size}/3)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "تزامن سحابي فوري ☁️",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.profitGreen
                            )
                        }

                        if (photos.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(photos) { idx, imgSrc ->
                                    Box(
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(1.dp, colors.goldAccent.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                    ) {
                                        SmartDishImage(
                                            imageSource = imgSrc,
                                            contentDescription = "${item.displayName} - صورة ${idx + 1}",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        IconButton(
                                            onClick = { onRemovePhotoAt(idx) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.7f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "حذف الصورة",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                multiPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = photos.size < 3,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, colors.primaryBrand.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick()
                                .testTag("pick_menu_item_photos_btn")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (photos.size >= 3) "تم الوصول للحد الأقصى (3 صور)" else "اختيار صور من المعرض (حد أقصى 3)",
                                color = colors.primaryBrand,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("اسم الصنف في المنيو") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("السعر المنشور بالمنيو (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = !isVariablePrice,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("menu_item_price_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                if (linkedProduct != null) {
                    OutlinedButton(
                        onClick = {
                            isVariablePrice = linkedProduct.defaultSellingPrice <= 0.0
                            priceText = linkedProduct.defaultSellingPrice.toString().removeSuffix(".0")
                            onRestoreDefaultPrice()
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .bounceClick()
                            .testTag("restore_default_price_btn"),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = colors.goldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "استعادة السعر الافتراضي (${DateUtils.formatCurrency(linkedProduct.defaultSellingPrice)})",
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "تعديل السعر هنا يحدث المنيو والكتالوج والسحابة معاً بشكل موحد.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }

                OutlinedTextField(
                    value = unitLabel,
                    onValueChange = { unitLabel = it },
                    label = { Text("الوحدة") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف إضافي بالمنيو (اختياري)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("صنف مميز (الأكثر طلباً \uD83D\uDD25)", color = colors.textPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Switch(checked = isPopular, onCheckedChange = { isPopular = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("يتطلب حجز مسبق 24 ساعة ⏳", color = colors.textPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Switch(checked = isPreOrder24h, onCheckedChange = { isPreOrder24h = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("سعر متغير (حسب السعر)", color = colors.textPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Switch(checked = isVariablePrice, onCheckedChange = { isVariablePrice = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ظاهر في المنيو المصدر وقسم العملاء", color = colors.textPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Switch(checked = isVisible, onCheckedChange = { isVisible = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = if (isVariablePrice) 0.0 else (priceText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                    onSave(
                        item.copy(
                            displayName = displayName.ifBlank { item.displayName }.trim(),
                            unitLabel = unitLabel.ifBlank { "كيلو" }.trim(),
                            description = description.trim(),
                            publishedPrice = price,
                            isVariablePrice = isVariablePrice,
                            isVisible = isVisible,
                            isPopular = isPopular,
                            isPreOrder24h = isPreOrder24h
                        )
                    )
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .bounceClick()
                    .testTag("save_menu_item_btn")
            ) {
                Text("حفظ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = colors.textSecondary) }
        }
    )
}

@Composable
fun AddCatalogProductToMenuDialog(
    menuCategories: List<MenuCategoryEntity>,
    products: List<ProductEntity>,
    primaryImages: Map<Long, String>,
    initialMenuCategoryId: Long?,
    onAddProduct: (ProductEntity, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var selectedMenuCatId by remember {
        mutableLongStateOf(initialMenuCategoryId ?: menuCategories.firstOrNull()?.id ?: 0L)
    }
    var search by remember { mutableStateOf("") }

    val filtered by remember(products, search) {
        derivedStateOf {
            products.filter { search.isBlank() || it.name.contains(search, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        modifier = Modifier.imePadding(),
        title = { Text("إضافة صنف من الكتالوج للمنيو", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("اختر القسم في المنيو:", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(menuCategories, key = { it.id }) { cat ->
                        CulinaryPillChip(
                            label = cat.name,
                            selected = selectedMenuCatId == cat.id,
                            onClick = { selectedMenuCatId = cat.id }
                        )
                    }
                }

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("ابحث بالاسم...", color = colors.textSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.id }) { prod ->
                        Surface(
                            color = colors.secondarySurface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colors.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .bounceClick {
                                    if (selectedMenuCatId > 0L) {
                                        onAddProduct(prod, selectedMenuCatId)
                                        onDismiss()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductThumbnail(
                                    imagePath = primaryImages[prod.id],
                                    contentDescription = prod.name,
                                    size = 40.dp,
                                    cornerRadius = 12.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = prod.name,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp
                                )
                                Surface(color = colors.primaryContainer, shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = if (prod.defaultSellingPrice > 0.0) DateUtils.formatCurrency(prod.defaultSellingPrice) else "حسب السعر",
                                        color = colors.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق", color = colors.textSecondary) }
        }
    )
}

@Composable
fun AddManualMenuItemDialog(
    menuCategories: List<MenuCategoryEntity>,
    onAddManual: (
        menuCatId: Long,
        name: String,
        unit: String,
        desc: String,
        price: Double,
        isVar: Boolean,
        isPopular: Boolean,
        isPreOrder24h: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var selectedMenuCatId by remember { mutableLongStateOf(menuCategories.firstOrNull()?.id ?: 0L) }
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("كيلو") }
    var desc by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var isVariable by remember { mutableStateOf(false) }
    var isPopular by remember { mutableStateOf(false) }
    var isPreOrder24h by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        modifier = Modifier.imePadding(),
        title = { Text("إضافة صنف يدوي للمنيو", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(menuCategories, key = { it.id }) { cat ->
                        CulinaryPillChip(
                            label = cat.name,
                            selected = selectedMenuCatId == cat.id,
                            onClick = { selectedMenuCatId = cat.id }
                        )
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الصنف *") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("الوحدة") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("السعر المنشور (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !isVariable,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("وصف (اختياري)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("صنف مميز (الأكثر طلباً \uD83D\uDD25)", color = colors.textPrimary, fontSize = 13.sp)
                    Switch(checked = isPopular, onCheckedChange = { isPopular = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("يتطلب حجز مسبق 24 ساعة ⏳", color = colors.textPrimary, fontSize = 13.sp)
                    Switch(checked = isPreOrder24h, onCheckedChange = { isPreOrder24h = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("حسب السعر", color = colors.textPrimary, fontSize = 13.sp)
                    Switch(checked = isVariable, onCheckedChange = { isVariable = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedMenuCatId > 0L) {
                        val price = if (isVariable) 0.0 else (priceText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                        onAddManual(selectedMenuCatId, name, unit, desc, price, isVariable, isPopular, isPreOrder24h)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .bounceClick()
            ) {
                Text("إضافة للمنيو", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = colors.textSecondary) }
        }
    )
}
