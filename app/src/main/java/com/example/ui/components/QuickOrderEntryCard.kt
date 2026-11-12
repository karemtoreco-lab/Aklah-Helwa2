package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandRedPrimary
import com.example.ui.theme.CharcoalBlackBg
import com.example.ui.theme.CharcoalCardBorder
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CharcoalCardSurface
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitContainer
import com.example.ui.theme.EmeraldProfitOnContainer
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary

@Composable
fun QuickOrderEntryCard(
    onSaveOrder: (
        customerName: String,
        orderValue: Double,
        profitEarned: Double,
        itemsDescription: String,
        customerPhone: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var customerName by remember { mutableStateOf("") }
    var orderValueText by remember { mutableStateOf("") }
    var profitEarnedText by remember { mutableStateOf("") }
    var itemsDescription by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }

    var isExpanded by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var valueError by remember { mutableStateOf(false) }
    var profitError by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(20.dp))
            .testTag("quick_order_entry_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CharcoalCardSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFF3D00), BrandRedPrimary)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = CleanWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تسجيل طلبية سريعة ⚡",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleanWhite
                        )
                        Text(
                            text = "أدخل بيانات العميل والمكسب للحفظ الفوري",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhiteSecondary
                        )
                    }
                }

                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("toggle_expand_extra_fields")
                ) {
                    Text(
                        text = if (isExpanded) "أقل" else "تفاصيل أكثر",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandRedPrimary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = BrandRedPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Customer Name TextField
            OutlinedTextField(
                value = customerName,
                onValueChange = {
                    customerName = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("اسم العميل *", color = TextWhiteSecondary) },
                placeholder = { Text("مثال: أم محمد / أحمد علي", color = Color(0xFF666666)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = BrandRedPrimary
                    )
                },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("يرجى كتابة اسم العميل") }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_customer_name_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CharcoalCardElevated,
                    focusedContainerColor = CharcoalCardElevated,
                    focusedBorderColor = BrandRedPrimary,
                    unfocusedBorderColor = CharcoalCardBorder,
                    focusedTextColor = CleanWhite,
                    unfocusedTextColor = CleanWhite
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2 & 3. Order Value & Profit Earned TextFields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Order Value TextField
                OutlinedTextField(
                    value = orderValueText,
                    onValueChange = {
                        orderValueText = it
                        if (it.toDoubleOrNull() != null && it.toDouble() > 0) valueError = false
                    },
                    label = { Text("قيمة الطلب (ج.م) *", color = TextWhiteSecondary) },
                    placeholder = { Text("500", color = Color(0xFF666666)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = BrandRedPrimary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = valueError,
                    supportingText = if (valueError) {
                        { Text("أدخل قيمة الطلب") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("entry_order_value_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CharcoalCardElevated,
                        focusedContainerColor = CharcoalCardElevated,
                        focusedBorderColor = BrandRedPrimary,
                        unfocusedBorderColor = CharcoalCardBorder,
                        focusedTextColor = CleanWhite,
                        unfocusedTextColor = CleanWhite
                    )
                )

                // Profit Earned TextField
                OutlinedTextField(
                    value = profitEarnedText,
                    onValueChange = {
                        profitEarnedText = it
                        if (it.toDoubleOrNull() != null) profitError = false
                    },
                    label = { Text("المكسب (ج.م) *", color = TextWhiteSecondary) },
                    placeholder = { Text("180", color = Color(0xFF666666)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = EmeraldProfit
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = profitError,
                    supportingText = if (profitError) {
                        { Text("أدخل المكسب") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("entry_profit_earned_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CharcoalCardElevated,
                        focusedContainerColor = CharcoalCardElevated,
                        focusedBorderColor = EmeraldProfit,
                        unfocusedBorderColor = CharcoalCardBorder,
                        focusedTextColor = CleanWhite,
                        unfocusedTextColor = CleanWhite
                    )
                )
            }

            // Optional Expanded Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = itemsDescription,
                        onValueChange = { itemsDescription = it },
                        label = { Text("أصناف الطلب / الوجبات (اختياري)", color = TextWhiteSecondary) },
                        placeholder = { Text("مثال: طاجن مكرونة بشاميل + فراخ مشوية", color = Color(0xFF666666)) },
                        leadingIcon = {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = BrandRedPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_items_description_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = CharcoalCardElevated,
                            focusedContainerColor = CharcoalCardElevated,
                            focusedBorderColor = BrandRedPrimary,
                            unfocusedBorderColor = CharcoalCardBorder,
                            focusedTextColor = CleanWhite,
                            unfocusedTextColor = CleanWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("رقم هاتف العميل (اختياري)", color = TextWhiteSecondary) },
                        placeholder = { Text("010XXXXXXXX", color = Color(0xFF666666)) },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = BrandRedPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_customer_phone_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = CharcoalCardElevated,
                            focusedContainerColor = CharcoalCardElevated,
                            focusedBorderColor = BrandRedPrimary,
                            unfocusedBorderColor = CharcoalCardBorder,
                            focusedTextColor = CleanWhite,
                            unfocusedTextColor = CleanWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Save to Database Button with Brand Red Gradient
            Button(
                onClick = {
                    var hasError = false
                    if (customerName.isBlank()) {
                        nameError = true
                        hasError = true
                    }
                    val value = orderValueText.toDoubleOrNull()
                    if (value == null || value <= 0) {
                        valueError = true
                        hasError = true
                    }
                    val profit = profitEarnedText.toDoubleOrNull()
                    if (profit == null) {
                        profitError = true
                        hasError = true
                    }

                    if (!hasError && value != null && profit != null) {
                        onSaveOrder(
                            customerName.trim(),
                            value,
                            profit,
                            itemsDescription.trim(),
                            customerPhone.trim()
                        )
                        customerName = ""
                        orderValueText = ""
                        profitEarnedText = ""
                        itemsDescription = ""
                        customerPhone = ""
                        showSuccessMessage = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_order_to_db_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandRedPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = CleanWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حفظ الطلب في قاعدة البيانات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CleanWhite
                )
            }

            // Success feedback
            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = EmeraldProfitContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldProfit,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تم حفظ الطلب في قاعدة البيانات وتحديث الأرباح بنجاح!",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldProfitOnContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
