package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.CategoryEntity
import com.example.model.ProductEntity
import com.example.ui.components.CulinaryPillChip
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductEditorDialog(
    initialProduct: ProductEntity? = null,
    categories: List<CategoryEntity>,
    onSave: (
        id: Long,
        name: String,
        categoryId: Long,
        unit: String,
        description: String,
        purchaseCost: Double,
        defaultSellingPrice: Double,
        isActive: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var selectedCategoryId by remember {
        mutableLongStateOf(initialProduct?.categoryId ?: categories.firstOrNull()?.id ?: 0L)
    }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "كيلو") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var purchaseText by remember {
        mutableStateOf(
            if (initialProduct != null && initialProduct.purchaseCost > 0.0)
                initialProduct.purchaseCost.toString().removeSuffix(".0")
            else "0"
        )
    }
    var sellingText by remember {
        mutableStateOf(
            if (initialProduct != null && initialProduct.defaultSellingPrice > 0.0)
                initialProduct.defaultSellingPrice.toString().removeSuffix(".0")
            else "0"
        )
    }
    var isActive by remember { mutableStateOf(initialProduct?.isActive ?: true) }
    var nameError by remember { mutableStateOf(false) }

    val purchaseVal = (purchaseText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val sellingVal = (sellingText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val estimatedUnitProfit = (sellingVal - purchaseVal).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        modifier = Modifier.imePadding(),
        title = {
            Text(
                text = if (initialProduct == null) "إضافة صنف جديد للكتالوج" else "تعديل بيانات الصنف",
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("اسم الصنف *") },
                    isError = nameError,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Text("القسم:", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        CulinaryPillChip(
                            label = cat.name,
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("الوحدة") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f),
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
                        value = purchaseText,
                        onValueChange = { purchaseText = it },
                        label = { Text("تكلفة الشراء") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_purchase_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.secondarySurface,
                            unfocusedContainerColor = colors.secondarySurface,
                            focusedBorderColor = colors.primaryBrand,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = sellingText,
                    onValueChange = { sellingText = it },
                    label = { Text("سعر البيع الافتراضي (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_selling_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                if (sellingVal > 0.0) {
                    Surface(
                        color = colors.profitContainer,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = " صافي ربح الوحدة المتوقع:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onProfitContainer
                                )
                            }
                            Text(
                                text = "+${DateUtils.formatCurrency(estimatedUnitProfit)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.profitGreen
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف مختصر (اختياري)") },
                    shape = RoundedCornerShape(16.dp),
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
                    Text("الصنف نشط ومتاح بالكتالوج", color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }

                if (initialProduct == null) {
                    Text(
                        text = "ملاحظة: الأصناف الجديدة تُحفظ في الكتالوج فقط ولا تُضاف للمنيو تلقائياً إلا باختيارك.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else if (selectedCategoryId > 0L) {
                        onSave(
                            initialProduct?.id ?: 0L,
                            name,
                            selectedCategoryId,
                            unit,
                            description,
                            purchaseVal,
                            sellingVal,
                            isActive
                        )
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp).bounceClick().testTag("save_product_btn")
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
fun ManageCategoriesDialog(
    categories: List<CategoryEntity>,
    onSaveCategory: (id: Long, name: String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var newCategoryName by remember { mutableStateOf("") }
    var editingCat by remember { mutableStateOf<CategoryEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        modifier = Modifier.imePadding(),
        title = { Text("إدارة أقسام الأصناف", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text(if (editingCat == null) "قسم جديد" else "تعديل القسم") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.secondarySurface,
                            unfocusedContainerColor = colors.secondarySurface,
                            focusedBorderColor = colors.primaryBrand,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                onSaveCategory(editingCat?.id ?: 0L, newCategoryName)
                                newCategoryName = ""
                                editingCat = null
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp).bounceClick()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "حفظ القسم", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                categories.forEach { cat ->
                    Surface(
                        color = colors.secondarySurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.name,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    editingCat = cat
                                    newCategoryName = cat.name
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { onDeleteCategory(cat) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = colors.primaryBrand, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = colors.primaryBrand, fontWeight = FontWeight.Bold)
            }
        }
    )
}
