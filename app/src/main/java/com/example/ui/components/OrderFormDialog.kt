package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CategoryEntity
import com.example.model.DraftOrderLineItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.model.ProductEntity
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderFormDialog(
    initialOrder: Order? = null,
    initialDraftItems: List<DraftOrderLineItem> = emptyList(),
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    primaryImages: Map<Long, String>,
    onDismiss: () -> Unit,
    onSaveOrder: (
        id: Long,
        customerName: String,
        customerPhone: String,
        lineItems: List<DraftOrderLineItem>,
        orderDate: Long,
        status: String,
        paymentStatus: String,
        notes: String
    ) -> Unit
) {
    val colors = AppTheme.colors
    var customerName by remember { mutableStateOf(initialOrder?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(initialOrder?.customerPhone ?: "") }
    var lineItems by remember(initialOrder, initialDraftItems) { mutableStateOf(initialDraftItems) }
    var orderDate by remember { mutableLongStateOf(initialOrder?.orderDate ?: System.currentTimeMillis()) }
    var status by remember { mutableStateOf(initialOrder?.status ?: OrderStatus.DELIVERED.name) }
    var paymentStatus by remember { mutableStateOf(initialOrder?.paymentStatus ?: PaymentStatus.PAID.name) }
    var notes by remember { mutableStateOf(initialOrder?.notes ?: "") }

    var showProductPicker by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var itemsError by remember { mutableStateOf(false) }

    val totalPurchase = lineItems.sumOf { it.totalPurchaseCost }
    val totalSelling = lineItems.sumOf { it.totalSellingPrice }
    val grossProfit = totalSelling - totalPurchase

    if (showProductPicker) {
        ProductPickerDialog(
            categories = categories,
            products = products,
            primaryImages = primaryImages,
            onSelectProduct = { product, imagePath ->
                val existingIdx = lineItems.indexOfFirst { it.productId == product.id }
                lineItems = if (existingIdx >= 0) {
                    lineItems.toMutableList().also { list ->
                        val cur = list[existingIdx]
                        list[existingIdx] = cur.copy(quantity = cur.quantity + 1.0)
                    }
                } else {
                    lineItems + DraftOrderLineItem(
                        productId = product.id,
                        productNameSnapshot = product.name,
                        productImagePathSnapshot = imagePath,
                        unitLabel = product.unit,
                        quantity = 1.0,
                        unitPurchaseCost = product.purchaseCost,
                        unitSellingPrice = product.defaultSellingPrice
                    )
                }
                itemsError = false
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false }
        )
    }

    if (showCustomItemDialog) {
        CustomOrderItemDialog(
            onAdd = { name, qty, purchase, selling ->
                lineItems = lineItems + DraftOrderLineItem(
                    productId = null,
                    productNameSnapshot = name,
                    productImagePathSnapshot = null,
                    unitLabel = "طلب",
                    quantity = qty,
                    unitPurchaseCost = purchase,
                    unitSellingPrice = selling
                )
                itemsError = false
                showCustomItemDialog = false
            },
            onDismiss = { showCustomItemDialog = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(12.dp)
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .imePadding()
                .shadow(18.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .border(1.2.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(28.dp))
                .testTag("order_form_dialog"),
            color = colors.cardSurface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .shadow(6.dp, CircleShape, spotColor = colors.primaryBrand.copy(alpha = 0.35f))
                                    .background(colors.warmAccentGradient, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (initialOrder == null) Icons.Default.Add else Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (initialOrder == null) "تسجيل أوردر جديد" else "تعديل الأوردر",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "أضف صنف واحد أو عدة أصناف للأوردر",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.cardSurface)
                                .border(1.dp, colors.cardBorder, CircleShape)
                                .bounceClick(onClick = onDismiss)
                                .testTag("close_order_dialog_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = {
                        customerName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("اسم العميل *") },
                    placeholder = { Text("مثال: أم محمد", color = colors.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.primaryBrand) },
                    isError = nameError,
                    supportingText = if (nameError) ({ Text("يرجى إدخال اسم العميل") }) else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_name_input"),
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("رقم الهاتف (اختياري)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = colors.secondaryAccent) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_phone_input"),
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

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "أصناف الأوردر (${lineItems.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { showCustomItemDialog = true },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, colors.cardBorder),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .bounceClick()
                                .testTag("add_custom_item_btn")
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("صنف خاص", fontSize = 11.sp, color = colors.textPrimary)
                        }
                        Button(
                            onClick = { showProductPicker = true },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .bounceClick()
                                .testTag("open_product_picker_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة صنف", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                if (itemsError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يرجى إضافة صنف واحد على الأقل للأوردر",
                        color = colors.primaryBrand,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (lineItems.isEmpty()) {
                    Surface(
                        color = colors.secondarySurface,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("لم يتم إضافة أصناف بعد", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("اضغط على (إضافة صنف) لاختيار المنتجات بالصور والأسعار", color = colors.textSecondary, fontSize = 11.sp)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        lineItems.forEach { item ->
                            OrderLineItemEditorCard(
                                item = item,
                                onUpdate = { updated ->
                                    lineItems = lineItems.map { if (it.tempId == item.tempId) updated else it }
                                },
                                onRemove = {
                                    lineItems = lineItems.filterNot { it.tempId == item.tempId }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.profitGreen.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                        .testTag("order_totals_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.profitContainer.copy(alpha = 0.78f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SummaryRow("إجمالي تكلفة الشراء:", DateUtils.formatCurrencyWithDecimal(totalPurchase), colors.textSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        SummaryRow("إجمالي البيع للعميل:", DateUtils.formatCurrencyWithDecimal(totalSelling), colors.primaryBrand)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colors.cardBorder)
                        SummaryRow(
                            "إجمالي ربح الأوردر (قبل المصاريف):",
                            DateUtils.formatCurrencyWithDecimal(grossProfit),
                            if (grossProfit >= 0) colors.profitGreen else colors.primaryBrand,
                            isLarge = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("حالة الطلب", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusChoices = if (initialOrder?.isFromCustomer == true || status == OrderStatus.NEW_CUSTOMER.name) {
                        listOf(OrderStatus.NEW_CUSTOMER, OrderStatus.PENDING, OrderStatus.DELIVERED, OrderStatus.CANCELLED)
                    } else {
                        listOf(OrderStatus.DELIVERED, OrderStatus.PENDING, OrderStatus.CANCELLED)
                    }
                    statusChoices.forEach { st ->
                        CulinaryPillChip(
                            label = st.titleAr,
                            selected = status == st.name,
                            onClick = { status = st.name },
                            selectedColor = colors.primaryBrand
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("حالة الدفع", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PaymentStatus.PAID, PaymentStatus.PARTIAL, PaymentStatus.UNPAID).forEach { pay ->
                        CulinaryPillChip(
                            label = pay.titleAr,
                            selected = paymentStatus == pay.name,
                            onClick = { paymentStatus = pay.name },
                            selectedColor = colors.profitGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات (اختياري)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = colors.goldAccent) },
                    modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 54.dp)
                        .wrapContentHeight()
                        .shadow(10.dp, RoundedCornerShape(50), spotColor = colors.primaryBrand.copy(alpha = 0.45f))
                        .clip(RoundedCornerShape(50))
                        .background(colors.warmAccentGradient)
                        .bounceClick {
                            val validName = customerName.isNotBlank()
                            val validItems = lineItems.isNotEmpty()
                            nameError = !validName
                            itemsError = !validItems
                            if (validName && validItems) {
                                onSaveOrder(
                                    initialOrder?.id ?: 0L,
                                    customerName,
                                    customerPhone,
                                    lineItems,
                                    orderDate,
                                    status,
                                    paymentStatus,
                                    notes
                                )
                            }
                        }
                        .padding(vertical = 14.dp, horizontal = 16.dp)
                        .testTag("save_order_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (initialOrder == null) "حفظ الأوردر" else "حفظ التعديلات",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderLineItemEditorCard(
    item: DraftOrderLineItem,
    onUpdate: (DraftOrderLineItem) -> Unit,
    onRemove: () -> Unit
) {
    val colors = AppTheme.colors
    var purchaseText by remember(item.tempId) {
        mutableStateOf(if (item.unitPurchaseCost == 0.0) "0" else item.unitPurchaseCost.toString().removeSuffix(".0"))
    }
    var sellingText by remember(item.tempId) {
        mutableStateOf(if (item.unitSellingPrice == 0.0) "0" else item.unitSellingPrice.toString().removeSuffix(".0"))
    }
    val qtyLabel = remember(item.quantity) {
        if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .testTag("draft_line_item_${item.productId ?: item.tempId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.background)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductThumbnail(
                    imagePath = item.productImagePathSnapshot,
                    contentDescription = item.productNameSnapshot,
                    size = 46.dp,
                    cornerRadius = 16.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productNameSnapshot,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "الوحدة: ${item.unitLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف الصنف", tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الكمية (${item.unitLabel}):", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                CulinaryQuantityStepper(
                    quantityText = qtyLabel,
                    onDecrease = {
                        val next = (item.quantity - 0.5).coerceAtLeast(0.5)
                        onUpdate(item.copy(quantity = next))
                    },
                    onIncrease = {
                        val next = item.quantity + 0.5
                        onUpdate(item.copy(quantity = next))
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = purchaseText,
                    onValueChange = { raw ->
                        purchaseText = raw
                        val v = raw.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(unitPurchaseCost = v.coerceAtLeast(0.0)))
                    },
                    label = { Text("تكلفة الشراء / وحدة", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("item_purchase_cost_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.cardSurface,
                        unfocusedContainerColor = colors.cardSurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                OutlinedTextField(
                    value = sellingText,
                    onValueChange = { raw ->
                        sellingText = raw
                        val v = raw.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(unitSellingPrice = v.coerceAtLeast(0.0)))
                    },
                    label = { Text("سعر البيع / وحدة", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("item_selling_price_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.cardSurface,
                        unfocusedContainerColor = colors.cardSurface,
                        focusedBorderColor = colors.goldAccent,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = colors.profitContainer.copy(alpha = 0.85f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "شراء: ${DateUtils.formatCurrencyWithDecimal(item.totalPurchaseCost)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "بيع: ${DateUtils.formatCurrencyWithDecimal(item.totalSellingPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "ربح: ${DateUtils.formatCurrencyWithDecimal(item.itemGrossProfit)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onProfitContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color, isLarge: Boolean = false) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isLarge) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
            fontWeight = if (isLarge) FontWeight.Bold else FontWeight.Medium,
            color = colors.textPrimary
        )
        Text(
            text = value,
            style = if (isLarge) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

@Composable
private fun CustomOrderItemDialog(
    onAdd: (name: String, qty: Double, purchase: Double, selling: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var name by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("1") }
    var purchaseText by remember { mutableStateOf("0") }
    var sellingText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        modifier = Modifier.imePadding(),
        title = { Text("إضافة صنف خاص للأوردر", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("الكمية") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    value = purchaseText,
                    onValueChange = { purchaseText = it },
                    label = { Text("تكلفة الشراء للوحدة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    value = sellingText,
                    onValueChange = { sellingText = it },
                    label = { Text("سعر البيع للوحدة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            name.trim(),
                            (qtyText.toDoubleOrNull() ?: 1.0).coerceAtLeast(0.1),
                            (purchaseText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
                            (sellingText.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                        )
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp).bounceClick()
            ) {
                Text("إضافة", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = colors.textSecondary) }
        }
    )
}
