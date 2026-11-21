package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@Composable
fun QuickOrderEntryCard(
    onSaveQuickOrder: (
        customerName: String,
        itemsDescription: String,
        salePrice: Double,
        costPrice: Double
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var customerName by remember { mutableStateOf("") }
    var itemsDescription by remember { mutableStateOf("") }
    var salePriceText by remember { mutableStateOf("") }
    var costPriceText by remember { mutableStateOf("") }

    val salePrice = salePriceText.toDoubleOrNull() ?: 0.0
    val costPrice = costPriceText.toDoubleOrNull() ?: 0.0
    val grossProfit = salePrice - costPrice
    val canSubmit = customerName.isNotBlank() && itemsDescription.isNotBlank() && salePrice > 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 0.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = colors.primaryBrand.copy(alpha = 0.2f)
            )
            .border(
                width = 1.dp,
                color = if (colors.isDark) colors.glassBorder else colors.cardBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("quick_order_entry_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape, spotColor = colors.primaryBrand.copy(alpha = 0.35f))
                            .clip(CircleShape)
                            .background(colors.warmAccentGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تسجيل أوردر سريع",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "حساب فوري لصافي الربح",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                val isPositive = grossProfit >= 0.0
                Surface(
                    color = if (isPositive) colors.profitContainer else colors.primaryContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, (if (isPositive) colors.profitGreen else colors.primaryBrand).copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (isPositive) colors.profitGreen else colors.primaryBrand,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = DateUtils.formatCurrency(grossProfit),
                            color = if (isPositive) colors.onProfitContainer else colors.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("اسم العميل *") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = colors.primaryBrand)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_order_customer_input"),
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
                value = itemsDescription,
                onValueChange = { itemsDescription = it },
                label = { Text("تفاصيل الأصناف *") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_order_items_input"),
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = salePriceText,
                    onValueChange = { salePriceText = it },
                    label = { Text("سعر البيع *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_order_sale_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.goldAccent,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { costPriceText = it },
                    label = { Text("التكلفة") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_order_cost_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.profitGreen,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = {
                    if (canSubmit) {
                        onSaveQuickOrder(
                            customerName.trim(),
                            itemsDescription.trim(),
                            salePrice.coerceAtLeast(0.0),
                            costPrice.coerceAtLeast(0.0)
                        )
                        customerName = ""
                        itemsDescription = ""
                        salePriceText = ""
                        costPriceText = ""
                    }
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryBrand,
                    disabledContainerColor = colors.secondarySurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .bounceClick()
                    .testTag("quick_order_save_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    tint = if (canSubmit) Color.White else colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "حفظ الأوردر السريع",
                    color = if (canSubmit) Color.White else colors.textSecondary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
