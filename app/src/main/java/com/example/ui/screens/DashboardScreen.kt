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

            // Hero Financial Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = colors.primaryBrand.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.heroGradient)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(colors.goldAccent.copy(alpha = 0.55f), Color.White.copy(alpha = 0.15f))
                        ),
                        shape = RoundedCornerShape(26.dp)
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
                            color = Color.White.copy(alpha = 0.92f),
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
                                text = "إجمالي الربح (قبل المصاريف)",
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

            // KPI Cards Row 3 (Live Online Now & Total Registered App Users)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiveOnlineUsersKpiCard(
                    onlineCount = onlineUsersCount,
                    modifier = Modifier.weight(1f).testTag("dashboard_online_users_card")
                )

                AnimatedMiniKpiCard(
                    title = "إجمالي مستخدمي التطبيق",
                    icon = Icons.Default.Person,
                    tint = colors.goldAccent,
                    modifier = Modifier.weight(1f).testTag("dashboard_total_users_card")
                ) {
                    AnimatedIntCounter(
                        targetValue = appUsers.size,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.textPrimary,
                        suffix = " مستخدم"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Registered App Users List
            Surface(
                color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_users_list_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { usersExpanded = !usersExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "قائمة العملاء والمستخدمين (${appUsers.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                        }
                        Icon(
                            imageVector = if (usersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "عرض أو إخفاء القائمة",
                            tint = colors.textSecondary
                        )
                    }

                    AnimatedVisibility(visible = usersExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = colors.cardBorder)
                            if (appUsers.isEmpty()) {
                                Text(
                                    text = "لم يسجل أي مستخدم على التطبيق السحابي بعد",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                val now = System.currentTimeMillis()
                                appUsers.forEach { u ->
                                    val isLive = u.isLiveOnline(now)
                                    Surface(
                                        color = colors.secondarySurface,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, if (isLive) colors.profitGreen.copy(alpha = 0.45f) else colors.cardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isLive) colors.profitGreen else colors.textSecondary.copy(alpha = 0.4f))
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = u.username,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.textPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    val joined = if (u.joinedAt > 0L) DateUtils.formatDate(u.joinedAt) else "—"
                                                    Text(
                                                        text = "انضم: $joined • طلبات: ${u.ordersCount}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                            }
                                            Surface(
                                                color = if (isLive) colors.profitContainer else colors.cardSurface,
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text(
                                                    text = if (isLive) "متصل الآن" else "غير متصل",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isLive) colors.onProfitContainer else colors.textSecondary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Action Tiles
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionTile(
                    title = "أوردر جديد",
                    icon = Icons.Default.Add,
                    accentBrush = colors.warmAccentGradient,
                    contentColor = Color.White,
                    onClick = onOpenAddOrderDialog,
                    modifier = Modifier.weight(1.25f)
                )
                QuickActionTile(
                    title = "الأصناف",
                    icon = Icons.Default.Inventory2,
                    accentBrush = Brush.horizontalGradient(listOf(colors.cardSurface, colors.secondarySurface)),
                    contentColor = colors.textPrimary,
                    iconTint = colors.secondaryAccent,
                    onClick = onNavigateToProducts,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    title = "المنيو",
                    icon = Icons.Default.RestaurantMenu,
                    accentBrush = Brush.horizontalGradient(listOf(colors.cardSurface, colors.secondarySurface)),
                    contentColor = colors.textPrimary,
                    iconTint = colors.primaryBrand,
                    onClick = onNavigateToMenu,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Charts & Performance
            SalesAndProfitChartCard(points = stats.dailyPoints)
            Spacer(modifier = Modifier.height(12.dp))
            ProductPerformanceCard(topProducts = stats.topProducts)

            Spacer(modifier = Modifier.height(18.dp))

            // Recent Orders Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("أحدث الأوردرات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                TextButton(onClick = onNavigateToOrders) {
                    Text("عرض الكل (${recentOrders.size})", style = MaterialTheme.typography.labelLarge, color = colors.primaryBrand, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (recentOrders.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_empty_kitchen),
                            contentDescription = null,
                            modifier = Modifier
                                .size(86.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد أوردرات مسجلة بعد", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("اضغط على (أوردر جديد) لتسجيل أول طلبية فعلية", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, textAlign = TextAlign.Center)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recentOrders.take(4).forEachIndexed { index, order ->
                        StaggeredAppearItem(index = index) {
                            RecentOrderItem(order = order, onClick = { onOrderClick(order) })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(
                onClick = onNavigateToAnalytics,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("فتح التقارير التفصيلية والمصاريف", color = colors.primaryBrand, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = colors.secondarySurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_footer_branding")
            ) {
                Text(
                    text = "أكلة حلوة — بإدارة نهى ماهر",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun LiveOnlineUsersKpiCard(
    onlineCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "online_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )

    Surface(
        color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.55f)),
        shadowElevation = if (colors.isDark) 0.dp else 2.dp,
        modifier = modifier.wrapContentHeight()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المتصلون الآن (أونلاين)",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(colors.profitContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(colors.profitGreen)
                )
                Spacer(modifier = Modifier.width(7.dp))
                AnimatedIntCounter(
                    targetValue = onlineCount,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.profitGreen,
                    suffix = " متصل"
                )
            }
        }
    }
}

@Composable
private fun AnimatedMiniKpiCard(
    title: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    valueContent: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    Surface(
        color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder),
        shadowElevation = if (colors.isDark) 0.dp else 2.dp,
        modifier = modifier.wrapContentHeight()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(tint.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            valueContent()
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    icon: ImageVector,
    accentBrush: Brush,
    contentColor: Color,
    iconTint: Color = contentColor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 50.dp)
            .wrapContentHeight()
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(accentBrush)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(18.dp))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentOrderItem(order: Order, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val isDelivered = order.status == OrderStatus.DELIVERED.name
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(20.dp))
            .bounceClick(onClick = onClick)
            .testTag("recent_order_${order.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
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
                        .size(42.dp)
                        .background(colors.warmAccentGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(order.customerName.take(1).ifBlank { "ع" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(order.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Surface(
                            color = if (isDelivered) colors.profitContainer else colors.secondaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = OrderStatus.trackingTitle(order.status),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDelivered) colors.onProfitContainer else colors.goldAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        if (order.isFromCustomer) {
                            Surface(color = colors.primaryContainer, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "طلب عميل",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = order.itemsDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = colors.secondarySurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(DateUtils.formatCurrency(order.salePrice), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = colors.primaryBrand)
                    Text("+${DateUtils.formatCurrency(order.netProfit)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = colors.profitGreen)
                }
            }
        }
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..11 -> "صباح الخير والبركة ☀️"
        in 12..16 -> "مساء النور والبهجة 🍲"
        else -> "مساء الخير والسعادة 🌙"
    }
}
