package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpenseEntity
import com.example.model.MonthlyStats
import com.example.ui.components.AnimatedCurrencyCounter
import com.example.ui.components.AnimatedIntCounter
import com.example.ui.components.OrdersVolumeChartCard
import com.example.ui.components.ProductPerformanceCard
import com.example.ui.components.SalesAndProfitChartCard
import com.example.ui.components.StaggeredAppearItem
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun MonthlyStatsScreen(
    stats: MonthlyStats,
    expenses: List<ExpenseEntity>,
    selectedYearMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddExpense: (title: String, category: String, amount: Double, notes: String) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onShareReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val monthTitleArabic = DateUtils.formatYearMonthArabic(selectedYearMonth)
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onSave = { title, cat, amount, notes ->
                onAddExpense(title, cat, amount, notes)
                showAddExpenseDialog = false
            },
            onDismiss = { showAddExpenseDialog = false }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 124.dp)
                .testTag("monthly_stats_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (colors.isDark) 0.dp else 4.dp, RoundedCornerShape(22.dp))
                    .testTag("month_selector_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.secondarySurface)
                            .border(1.dp, colors.cardBorder, CircleShape)
                            .bounceClick(onClick = onNextMonth)
                            .testTag("next_month_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "الشهر القادم", tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(21.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("تقرير أرباح $monthTitleArabic", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary, textAlign = TextAlign.Center)
                            Text("تحليل الأداء والمبيعات والمصاريف", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary, textAlign = TextAlign.Center)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.secondarySurface)
                            .border(1.dp, colors.cardBorder, CircleShape)
                            .bounceClick(onClick = onPreviousMonth)
                            .testTag("prev_month_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الشهر السابق", tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Financial Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(12.dp, RoundedCornerShape(26.dp), spotColor = colors.primaryBrand.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.heroGradient)
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("total_sales_card")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFE599), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إجمالي المبيعات", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedCurrencyCounter(
                                targetValue = stats.totalSales,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = Color.White.copy(alpha = 0.18f), shape = RoundedCornerShape(50)) {
                                Text(
                                    text = monthTitleArabic,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.Black.copy(alpha = 0.26f))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                                .padding(12.dp)
                                .testTag("net_profit_card")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFFA8F5EC), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إجمالي الربح", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFA8F5EC))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            AnimatedCurrencyCounter(
                                targetValue = stats.grossProfitBeforeExpenses,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFFA8F5EC)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "هامش ${String.format(Locale.US, "%.1f", stats.profitMarginPercent)}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(22.dp))
                    .testTag("costs_summary_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("إجمالي تكلفة الشراء:", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            AnimatedCurrencyCounter(
                                targetValue = stats.totalPurchaseCost,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                            Text("متوسط ربح الأوردر:", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            AnimatedCurrencyCounter(
                                targetValue = stats.averageOrderProfit,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = colors.profitGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val targetRatio = if (stats.totalSales > 0) (stats.grossProfitBeforeExpenses / stats.totalSales).toFloat().coerceIn(0f, 1f) else 0f
                    val animatedRatio by animateFloatAsState(
                        targetValue = targetRatio,
                        animationSpec = tween(750, easing = FastOutSlowInEasing),
                        label = "margin_progress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedRatio },
                        modifier = Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(50)),
                        color = colors.profitGreen,
                        trackColor = colors.primaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCountChip("الأوردرات", stats.totalOrdersCount, if (colors.isDark) colors.glassSurface else colors.cardSurface, colors.textPrimary, Icons.Default.ShoppingBag, Modifier.weight(1f))
                StatCountChip("تم التسليم", stats.deliveredCount, colors.profitContainer, colors.onProfitContainer, Icons.Default.CheckCircle, Modifier.weight(1f))
                StatCountChip("قيد التجهيز", stats.pendingCount, colors.secondaryContainer, colors.goldAccent, Icons.Default.HourglassTop, Modifier.weight(1f))
                StatCountChip("العملاء", stats.uniqueCustomersCount, if (colors.isDark) colors.glassSurface else colors.cardSurface, colors.secondaryAccent, Icons.Default.Group, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            SalesAndProfitChartCard(points = stats.dailyPoints)
            Spacer(modifier = Modifier.height(12.dp))
            OrdersVolumeChartCard(points = stats.dailyPoints)
            Spacer(modifier = Modifier.height(12.dp))
            ProductPerformanceCard(topProducts = stats.topProducts)

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(22.dp))
                    .testTag("separate_expenses_section"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("المصاريف والتشغيل (منفصلة)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                            Text(
                                text = "إجمالي المصاريف: ${DateUtils.formatCurrency(stats.totalExpenses)} (لا تُخصم من إجمالي الربح)",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showAddExpenseDialog = true },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick()
                                .testTag("add_expense_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مصروف", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (expenses.isEmpty()) {
                        Surface(
                            color = colors.secondarySurface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colors.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "لا توجد مصاريف مسجلة في هذا الشهر حتى الآن",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            expenses.forEachIndexed { idx, expense ->
                                StaggeredAppearItem(index = idx) {
                                    Surface(
                                        color = colors.secondarySurface,
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, colors.cardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(expense.title, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${expense.category} • ${DateUtils.formatDayMonth(expense.expenseDate)}", color = colors.textSecondary, fontSize = 11.sp)
                                            }
                                            Surface(color = colors.primaryContainer, shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = DateUtils.formatCurrency(expense.amount),
                                                    color = colors.onPrimaryContainer,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            IconButton(onClick = { onDeleteExpense(expense) }, modifier = Modifier.size(34.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف المصروف", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onShareReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 54.dp)
                    .wrapContentHeight()
                    .shadow(8.dp, RoundedCornerShape(50), spotColor = colors.profitGreen.copy(alpha = 0.4f))
                    .bounceClick()
                    .testTag("share_monthly_report_btn"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.profitGreen)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مشاركة تقرير شهر $monthTitleArabic عبر واتساب", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StatCountChip(
    title: String,
    count: Int,
    color: Color,
    textColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Surface(
        color = color,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = modifier.wrapContentHeight()
    ) {
        Column(modifier = Modifier.padding(vertical = 11.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedIntCounter(
                targetValue = count,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = textColor
            )
            Text(title, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.85f), textAlign = TextAlign.Center, fontSize = 10.sp)
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onSave: (title: String, category: String, amount: Double, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("توصيل ومواصلات") }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardSurface,
        modifier = Modifier.imePadding(),
        title = { Text("تسجيل مصروف منفصل", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("بند المصروف *") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input"),
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
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف") },
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
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ (ج.م) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
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
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات (اختياري)") },
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
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0.0) {
                        onSave(title, category, amt, notes)
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .bounceClick()
                    .testTag("save_expense_btn")
            ) {
                Text("حفظ المصروف", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = colors.textSecondary) }
        }
    )
}
