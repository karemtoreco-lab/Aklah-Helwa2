package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.ui.theme.CoralOrangePrimary
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitContainer
import com.example.ui.theme.EmeraldProfitOnContainer
import com.example.ui.theme.GoldenHoneySecondary
import com.example.ui.theme.WarmCardBorder
import com.example.ui.theme.WarmCardSurface
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderFormDialog(
    initialOrder: Order? = null,
    onDismiss: () -> Unit,
    onSaveOrder: (
        id: Long,
        customerName: String,
        customerPhone: String,
        itemsDescription: String,
        salePrice: Double,
        costPrice: Double,
        orderDate: Long,
        status: String,
        paymentStatus: String,
        notes: String
    ) -> Unit
) {
    var customerName by remember { mutableStateOf(initialOrder?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(initialOrder?.customerPhone ?: "") }
    var itemsDescription by remember { mutableStateOf(initialOrder?.itemsDescription ?: "") }

    var orderValueText by remember {
        mutableStateOf(if (initialOrder != null) initialOrder.salePrice.toString().removeSuffix(".0") else "")
    }
    var profitEarnedText by remember {
        mutableStateOf(if (initialOrder != null) initialOrder.netProfit.toString().removeSuffix(".0") else "")
    }
    var costPriceText by remember {
        mutableStateOf(if (initialOrder != null) initialOrder.costPrice.toString().removeSuffix(".0") else "")
    }

    var orderDate by remember { mutableLongStateOf(initialOrder?.orderDate ?: System.currentTimeMillis()) }
    var status by remember { mutableStateOf(initialOrder?.status ?: OrderStatus.DELIVERED.name) }
    var paymentStatus by remember { mutableStateOf(initialOrder?.paymentStatus ?: PaymentStatus.PAID.name) }
    var notes by remember { mutableStateOf(initialOrder?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var valueError by remember { mutableStateOf(false) }
    var profitError by remember { mutableStateOf(false) }

    val orderValueVal = orderValueText.toDoubleOrNull() ?: 0.0
    val profitEarnedVal = profitEarnedText.toDoubleOrNull() ?: 0.0
    val costPriceVal = if (costPriceText.isNotBlank()) {
        costPriceText.toDoubleOrNull() ?: (orderValueVal - profitEarnedVal).coerceAtLeast(0.0)
    } else {
        (orderValueVal - profitEarnedVal).coerceAtLeast(0.0)
    }

    val profitMargin = if (orderValueVal > 0) (profitEarnedVal / orderValueVal) * 100.0 else 0.0

    val quickDishes = listOf(
        "مكرونة بشاميل", "محشي مشكل", "طاجن عكاوي", "فراخ مشوية",
        "كفتة حاتي", "كبسة لحمة", "حواوشي بلدي", "ورق عنب باللحمة",
        "صينية بطاطس بالفراخ", "رقاق بلدي", "بسبوسة مكرملة", "كنافة بالقشطة"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, WarmCardBorder, RoundedCornerShape(26.dp))
                .testTag("order_form_dialog"),
            color = WarmCardSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with soft warm banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFF7043), CoralOrangePrimary)
                                    ),
                                    CircleShape
                                ),
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
                                text = if (initialOrder == null) "تسجيل طلبية جديدة 🍲" else "تعديل بيانات الطلبية",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "مشروع أكله حلوة للأكلات المنزلية",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .testTag("close_order_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Customer Information
                Text(
                    text = "👤 بيانات العميل",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = CoralOrangePrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = {
                        customerName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("اسم العميل *") },
                    placeholder = { Text("مثال: أم محمد / أحمد طارق") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = CoralOrangePrimary)
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("يرجى إدخال اسم العميل") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = WarmCardSurface
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Customer Phone
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("رقم التليفون (اختياري)") },
                    placeholder = { Text("010XXXXXXXX") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = CoralOrangePrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_phone_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = WarmCardSurface
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Financial Details (Order Value & Profit)
                Text(
                    text = "💰 الحسابات والمكسب",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = CoralOrangePrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Order Value
                    OutlinedTextField(
                        value = orderValueText,
                        onValueChange = {
                            orderValueText = it
                            val value = it.toDoubleOrNull()
                            if (value != null && value > 0) {
                                valueError = false
                                if (costPriceText.isNotBlank()) {
                                    val cost = costPriceText.toDoubleOrNull() ?: 0.0
                                    profitEarnedText = (value - cost).coerceAtLeast(0.0).toString().removeSuffix(".0")
                                }
                            }
                        },
                        label = { Text("قيمة الطلب (ج.م) *") },
                        placeholder = { Text("500") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = CoralOrangePrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = valueError,
                        supportingText = if (valueError) {
                            { Text("أدخل قيمة الطلب") }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_value_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedContainerColor = WarmCardSurface
                        )
                    )

                    // Profit Earned
                    OutlinedTextField(
                        value = profitEarnedText,
                        onValueChange = {
                            profitEarnedText = it
                            val profit = it.toDoubleOrNull()
                            if (profit != null) {
                                profitError = false
                                val value = orderValueText.toDoubleOrNull() ?: 0.0
                                if (value > 0) {
                                    costPriceText = (value - profit).coerceAtLeast(0.0).toString().removeSuffix(".0")
                                }
                            }
                        },
                        label = { Text("المكسب المحقق (ج.م) *") },
                        placeholder = { Text("180") },
                        leadingIcon = {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldProfit)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = profitError,
                        supportingText = if (profitError) {
                            { Text("أدخل المكسب") }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profit_earned_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedContainerColor = WarmCardSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live Net Profit Highlight Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                        .testTag("live_profit_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F2)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFE8F5E9), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = EmeraldProfit,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "صافي ربحك في الطلب:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldProfit
                                )
                                Text(
                                    text = DateUtils.formatCurrencyWithDecimal(profitEarnedVal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldProfit
                                )
                            }
                        }

                        if (orderValueVal > 0) {
                            Surface(
                                color = EmeraldProfitContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "هامش ${String.format(java.util.Locale.US, "%.0f", profitMargin)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldProfitOnContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Meal details
                Text(
                    text = "🍽️ تفاصيل الأكلات والوجبات",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = CoralOrangePrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = itemsDescription,
                    onValueChange = { itemsDescription = it },
                    label = { Text("أصناف الطلب والوجبات (اختياري)") },
                    placeholder = { Text("مثال: طاجن مكرونة بشاميل + ٢ كيلو محشي مشكل...") },
                    leadingIcon = {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = CoralOrangePrimary)
                    },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("items_description_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = WarmCardSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Meal Chips
                Text(
                    text = "أكلات سريعة لإضافتها بلمسة واحدة:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickDishes.forEach { dish ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    itemsDescription = if (itemsDescription.isBlank()) {
                                        dish
                                    } else {
                                        "$itemsDescription + $dish"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ $dish",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Statuses
                Text(
                    text = "📦 حالة الطلب والتسليم:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrderStatus.entries.forEach { orderStatus ->
                        FilterChip(
                            selected = status == orderStatus.name,
                            onClick = { status = orderStatus.name },
                            label = { Text(orderStatus.titleAr) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralOrangePrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("status_chip_${orderStatus.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "💳 حالة سداد المبلغ:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentStatus.entries.forEach { pStatus ->
                        FilterChip(
                            selected = paymentStatus == pStatus.name,
                            onClick = { paymentStatus = pStatus.name },
                            label = { Text(pStatus.titleAr) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldProfit,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("payment_chip_${pStatus.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    placeholder = { Text("عنوان التوصيل أو موعد الاستلام...") },
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_notes_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = WarmCardSurface
                    )
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Action Buttons: Cancel and Gradient Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("cancel_order_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold)
                    }

                    // Save Order Button with Gradient
                    Button(
                        onClick = {
                            var valid = true
                            if (customerName.isBlank()) {
                                nameError = true
                                valid = false
                            }
                            if (orderValueText.toDoubleOrNull() == null || orderValueText.toDouble() <= 0) {
                                valueError = true
                                valid = false
                            }
                            if (profitEarnedText.toDoubleOrNull() == null) {
                                profitError = true
                                valid = false
                            }

                            if (valid) {
                                val finalItems = if (itemsDescription.isBlank()) "طلب طعام متنوع" else itemsDescription
                                onSaveOrder(
                                    initialOrder?.id ?: 0L,
                                    customerName,
                                    customerPhone,
                                    finalItems,
                                    orderValueVal,
                                    costPriceVal,
                                    orderDate,
                                    status,
                                    paymentStatus,
                                    notes
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp))
                            .testTag("save_order_to_db_dialog_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralOrangePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "حفظ الطلب في قاعدة البيانات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
