package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppUser
import com.example.model.MonthlyStats
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.components.AnimatedCurrencyCounter
import com.example.ui.components.AnimatedIntCounter
import com.example.ui.components.ProductPerformanceCard
import com.example.ui.components.SalesAndProfitChartCard
import com.example.ui.components.StaggeredAppearItem
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    stats: MonthlyStats,
    selectedYearMonth: String,
    recentOrders: List<Order>,
    appUsers: List<AppUser>,
    kitchenOpen: Boolean,
    onToggleKitchenOpen: (Boolean) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onOpenAddOrderDialog: () -> Unit,
    onOrderClick: (Order) -> Unit,
    onCheckForUpdates: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val monthTitle = DateUtils.formatYearMonthArabic(selectedYearMonth)
    val greeting = remember { getGreeting() }
    var usersExpanded by remember { mutableStateOf(false) }

    val onlineUsersCount by remember(appUsers) {
        derivedStateOf {
            val now = System.currentTimeMillis()
            appUsers.count { it.isLiveOnline(now) }
        }
    }

    val newCustomerOrdersCount by remember(recentOrders) {
        derivedStateOf {
            recentOrders.count {
                it.status == OrderStatus.PENDING.name ||
                    it.status == OrderStatus.NEW_CUSTOMER.name ||
                    it.status == OrderStatus.PENDING_REVIEW.name
            }
        }
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
                .testTag("dashboard_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Personalized Owner Welcome & Month Selector Pill Header
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_welcome_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryBrand
                    )
                    Text(
                        text = "أهلاً بكِ يا أ/ نهى ماهر",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onCheckForUpdates,
                        color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .bounceClick()
                            .testTag("admin_check_updates_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "التحقق من التحديثات",
                                tint = colors.goldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "التحقق من التحديثات",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder),
                        shadowElevation = if (colors.isDark) 0.dp else 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "التالي", tint = colors.primaryBrand, modifier = Modifier.size(16.dp))
                            }
                            Text(monthTitle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            IconButton(onClick = onPreviousMonth, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "السابق", tint = colors.primaryBrand, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kitchen Live Status Control Switch Card
            Surface(
                color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    if (kitchenOpen) colors.profitGreen.copy(alpha = 0.55f) else colors.goldAccent.copy(alpha = 0.55f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_kitchen_switch_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (kitchenOpen) colors.profitContainer else colors.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = if (kitchenOpen) colors.profitGreen else colors.goldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (kitchenOpen) "حالة المطبخ: مفتوح لاستقبال الطلبات" else "حالة المطبخ: مشغول حالياً",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "تظهر مباشرة في واجهة العملاء لحظياً",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                    Switch(
                        checked = kitchenOpen,
                        onCheckedChange = onToggleKitchenOpen,
                        modifier = Modifier.testTag("kitchen_status_switch")
                    )
                }
            }

            if (newCustomerOrdersCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = colors.profitContainer,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(onClick = onNavigateToOrders)
                        .testTag("new_customer_orders_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "لديكِ $newCustomerOrdersCount طلب بانتظار المراجعة أو التجهيز",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onProfitContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onProfitContainer, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Financial Card (Upgraded with Net Profit Pill & Gold Border)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = colors.primaryBrand.copy(alpha = 0.45f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(colors.heroGradient)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                colors.goldAccent.copy(alpha = 0.85f),
                                Color.White.copy(alpha = 0.25f),
                                colors.goldAccent.copy(alpha = 0.55f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .testTag("dashboard_hero_banner")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "ملخص $monthTitle المالي ✨",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.94f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "هامش ${String.format(Locale.US, "%.1f", stats.profitMarginPercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.profitGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f).testTag("dashboard_revenue_card")) {
                            Text(
                                text = "إجمالي المبيعات",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AnimatedCurrencyCounter(
                                targetValue = stats.totalSales,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(1.dp)
                                .background(Color.White.copy(alpha = 0.28f))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f).testTag("dashboard_profit_card")
                        ) {
                            Text(
                                text = "ربح الأوردرات",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFE599)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AnimatedCurrencyCounter(
                                targetValue = stats.grossProfitBeforeExpenses,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFFFFE599),
                                prefix = "+"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val netFinalProfit = stats.grossProfitBeforeExpenses - stats.totalExpenses
                    Surface(
                        color = Color.Black.copy(alpha = 0.24f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFE599).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "صافي الربح النهائي (بعد المصاريف):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = DateUtils.formatCurrency(netFinalProfit),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netFinalProfit >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // KPI Cards Row 1 (Financial)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnimatedMiniKpiCard(
                    title = "تكلفة الشراء",
                    icon = Icons.Default.ShoppingCart,
                    tint = colors.secondaryAccent,
                    modifier = Modifier.weight(1f).testTag("dashboard_purchase_cost_card")
                ) {
                    AnimatedCurrencyCounter(
                        targetValue = stats.totalPurchaseCost,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.textPrimary
                    )
                }

                AnimatedMiniKpiCard(
                    title = "المصاريف المنفصلة",
                    icon = Icons.Default.ReceiptLong,
                    tint = colors.primaryBrand,
                    modifier = Modifier.weight(1f).testTag("dashboard_expenses_card")
                ) {
                    AnimatedCurrencyCounter(
                        targetValue = stats.totalExpenses,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // KPI Cards Row 2 (Orders & Customers)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnimatedMiniKpiCard(
                    title = "عدد الأوردرات",
                    icon = Icons.Default.ReceiptLong,
                    tint = colors.goldAccent,
                    modifier = Modifier.weight(1f).testTag("dashboard_orders_count_card")
                ) {
                    AnimatedIntCounter(
                        targetValue = stats.totalOrdersCount,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.goldAccent,
                        suffix = " أوردر"
                    )
                }

                AnimatedMiniKpiCard(
                    title = "العملاء النشطين",
                    icon = Icons.Default.Group,
                    tint = colors.profitGreen,
                    modifier = Modifier.weight(1f).testTag("dashboard_customers_card")
                ) {
                    AnimatedIntCounter(
                        targetValue = stats.uniqueCustomersCount,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.profitGreen,
                        suffix = " عميل"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // KPI Cards Row 3 (Live Online Now & Total Registered
