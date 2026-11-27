package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppUser
import com.example.model.CloudSyncState
import com.example.model.MonthlyStats
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.components.AnimatedCurrencyCounter
import com.example.ui.components.AnimatedIntCounter
import com.example.ui.components.LuxuryCard
import com.example.ui.components.ProductPerformanceCard
import com.example.ui.components.SalesAndProfitChartCard
import com.example.ui.components.StaggeredAppearItem
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.ui.theme.LightTextPrimary
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
    cloudSyncState: CloudSyncState = CloudSyncState.LocalOffline,
    onToggleOrderStatus: (Order) -> Unit = {},
    onDeleteAppUser: (AppUser) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    val monthTitle = DateUtils.formatYearMonthArabic(selectedYearMonth)
    val greeting = remember { getGreeting() }
    var usersExpanded by remember { mutableStateOf(false) }
    var userPendingDeletion by remember { mutableStateOf<AppUser?>(null) }

    val onlineUsersCount by remember(appUsers) {
        derivedStateOf {
            val now = System.currentTimeMillis()
            appUsers.count { it.isLiveOnline(now) }
        }
    }

    val actionableOrders by remember(recentOrders) {
        derivedStateOf {
            recentOrders
                .filter {
                    it.status != OrderStatus.DELIVERED.name &&
                        it.status != OrderStatus.CANCELLED.name
                }
                .sortedByDescending { it.orderDate }
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
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 126.dp)
                .testTag("dashboard_screen")
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Top Executive Welcome Card (Owner Name + Cloud Sync Indicator + Large Kitchen Switch + Month Selector)
            StaggeredAppearItem(index = 0) {
                LuxuryCard(
                    cornerRadius = 26.dp,
                    elevation = 5.dp,
                    modifier = Modifier.testTag("dashboard_welcome_header")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .shadow(
                                            elevation = if (colors.isDark) 0.dp else 5.dp,
                                            shape = CircleShape,
                                            spotColor = colors.goldAccent.copy(alpha = 0.38f)
                                        )
                                        .clip(CircleShape)
                                        .background(colors.cardSurface)
                                        .border(2.dp, colors.goldAccent, CircleShape)
                                        .padding(2.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.app_logo),
                                        contentDescription = "شعار أكلة حلوة",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = greeting,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.goldText
                                    )
                                    Text(
                                        text = "أ/ نهى ماهر",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    CloudSyncBadgePill(cloudSyncState = cloudSyncState)
                                }
                            }

                            Surface(
                                onClick = onCheckForUpdates,
                                color = colors.secondarySurface,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .defaultMinSize(minHeight = 42.dp)
                                    .bounceClick()
                                    .testTag("admin_check_updates_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = "التحقق من التحديثات",
                                        tint = colors.goldText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "التحديثات",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Large Prominent Kitchen Open/Closed Control Panel
                        val kitchenPanelBg by animateColorAsState(
                            targetValue = if (kitchenOpen) colors.profitContainer else colors.errorContainer,
                            animationSpec = tween(250),
                            label = "kitchen_panel_bg"
                        )
                        val kitchenPanelBorder by animateColorAsState(
                            targetValue = if (kitchenOpen) colors.profitGreen.copy(alpha = 0.38f) else colors.errorRed.copy(alpha = 0.4f),
                            animationSpec = tween(250),
                            label = "kitchen_panel_border"
                        )

                        Surface(
                            color = kitchenPanelBg,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, kitchenPanelBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dashboard_kitchen_switch_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(colors.cardSurface)
                                            .border(1.dp, kitchenPanelBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = if (kitchenOpen) colors.profitGreen else colors.errorRed,
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = if (kitchenOpen) "استقبال الطلبات مفتوح" else "استقبال الطلبات متوقف",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (kitchenOpen) colors.onProfitContainer else colors.onErrorContainer
                                        )
                                        Text(
                                            text = if (kitchenOpen) {
                                                "يظهر للعملاء الآن: المطبخ مفتوح"
                                            } else {
                                                "يظهر للعملاء: المطبخ مغلق"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = kitchenOpen,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleKitchenOpen(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.profitGreen,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = colors.errorRed.copy(alpha = 0.78f),
                                        uncheckedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .scale(1.18f)
                                        .padding(end = 6.dp)
                                        .testTag("kitchen_status_switch")
                                )
                            }
                        }

                        // Month Selector Strip inside Welcome Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إحصائيات شهر",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary
                            )

                            Surface(
                                color = colors.secondarySurface,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, colors.cardBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    IconButton(
                                        onClick = onNextMonth,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "الشهر التالي",
                                            tint = colors.primaryBrand,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = monthTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = onPreviousMonth,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "الشهر السابق",
                                            tint = colors.primaryBrand,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // New Customer Orders Urgent Pill
            AnimatedVisibility(
                visible = newCustomerOrdersCount > 0,
                enter = expandVertically(spring(dampingRatio = 0.85f)) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    LuxuryCard(
                        containerColor = colors.profitContainer,
                        borderColor = colors.profitGreen.copy(alpha = 0.4f),
                        cornerRadius = 20.dp,
                        elevation = 2.dp,
                        onClick = onNavigateToOrders,
                        modifier = Modifier.testTag("new_customer_orders_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.profitGreen.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = colors.profitGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "لديكِ $newCustomerOrdersCount طلب جديد بانتظار المراجعة والتنفيذ",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.onProfitContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = colors.onProfitContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Modern Bento Grid Stats (Large Hero Net Profit Card + Stacked Sales & Expenses Cards)
            StaggeredAppearItem(index = 1) {
                val netFinalProfit = stats.grossProfitBeforeExpenses - stats.totalExpenses
                val heroTextColor = if (colors.isDark) colors.textPrimary else Color.White
                val heroSubColor = if (colors.isDark) colors.textSecondary else Color.White.copy(alpha = 0.88f)
                val heroChipBg = if (colors.isDark) colors.secondarySurface else Color.White.copy(alpha = 0.18f)
                val heroChipBorder = if (colors.isDark) colors.cardBorder else Color.White.copy(alpha = 0.25f)

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Large Hero Bento Card: True Net Profit
                        Box(
                            modifier = Modifier
                                .weight(1.22f)
                                .fillMaxHeight()
                                .shadow(
                                    elevation = if (colors.isDark) 0.dp else 8.dp,
                                    shape = RoundedCornerShape(26.dp),
                                    ambientColor = colors.softShadow,
                                    spotColor = colors.softShadow
                                )
                                .clip(RoundedCornerShape(26.dp))
                                .background(colors.heroGradient)
                                .border(
                                    width = 1.dp,
                                    color = if (colors.isDark) colors.cardBorder else colors.primaryBrand,
                                    shape = RoundedCornerShape(26.dp)
                                )
                                .testTag("dashboard_hero_banner")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .testTag("dashboard_profit_card"),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(heroChipBg)
                                            .border(1.dp, heroChipBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = if (colors.isDark) colors.goldText else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Surface(
                                        color = if (netFinalProfit >= 0) colors.profitContainer else colors.errorContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "هامش ${String.format(Locale.US, "%.1f", stats.profitMarginPercent)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (netFinalProfit >= 0) colors.onProfitContainer else colors.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "صافي الربح الحقيقي",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = heroSubColor
                                    )
                                    AnimatedCurrencyCounter(
                                        targetValue = netFinalProfit,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 24.sp
                                        ),
                                        color = if (netFinalProfit >= 0) heroTextColor else colors.onErrorContainer,
                                        prefix = if (netFinalProfit > 0) "+" else ""
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    color = heroChipBg,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, heroChipBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                        Text(
                                            text = "قبل المصاريف",
                                            fontSize = 10.sp,
                                            color = heroSubColor
                                        )
                                        AnimatedCurrencyCounter(
                                            targetValue = stats.grossProfitBeforeExpenses,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                            color = heroTextColor
                                        )
                                    }
                                }
                            }
                        }

                        // Right/End Column: 2 Stacked Compact Bento Cards (Total Sales + Expenses)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BentoMetricTile(
                                title = "إجمالي المبيعات",
                                icon = Icons.Default.TrendingUp,
                                iconTint = colors.goldAccent,
                                badgeBg = colors.secondaryContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dashboard_revenue_card")
                            ) {
                                AnimatedCurrencyCounter(
                                    targetValue = stats.totalSales,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp
                                    ),
                                    color = colors.goldText
                                )
                            }

                            BentoMetricTile(
                                title = "المصاريف",
                                icon = Icons.Default.ReceiptLong,
                                iconTint = colors.primaryBrand,
                                badgeBg = colors.primaryContainer,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dashboard_expenses_card")
                            ) {
                                AnimatedCurrencyCounter(
                                    targetValue = stats.totalExpenses,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    ),
                                    color = colors.primaryBrand
                                )
                            }
                        }
                    }

                    // Secondary Bento Grid Row (Purchase Cost, Orders Count, Active Customers)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoMetricTile(
                            title = "تكلفة الشراء",
                            icon = Icons.Default.ShoppingCart,
                            iconTint = colors.goldText,
                            badgeBg = colors.secondaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_purchase_cost_card")
                        ) {
                            AnimatedCurrencyCounter(
                                targetValue = stats.totalPurchaseCost,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = colors.textPrimary
                            )
                        }

                        BentoMetricTile(
                            title = "الأوردرات",
                            icon = Icons.Default.ReceiptLong,
                            iconTint = colors.primaryBrand,
                            badgeBg = colors.primaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_orders_count_card")
                        ) {
                            AnimatedIntCounter(
                                targetValue = stats.totalOrdersCount,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = colors.textPrimary,
                                suffix = " أوردر"
                            )
                        }

                        BentoMetricTile(
                            title = "العملاء",
                            icon = Icons.Default.Group,
                            iconTint = colors.profitGreen,
                            badgeBg = colors.profitContainer,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_customers_card")
                        ) {
                            AnimatedIntCounter(
                                targetValue = stats.uniqueCustomersCount,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = colors.profitGreen,
                                suffix = " عميل"
                            )
                        }
                    }

                    // Live App Presence Bento Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiveOnlineUsersKpiCard(
                            onlineCount = onlineUsersCount,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_online_users_card")
                        )

                        BentoMetricTile(
                            title = "مستخدمي التطبيق",
                            icon = Icons.Default.Person,
                            iconTint = colors.primaryBrand,
                            badgeBg = colors.primaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_total_users_card")
                        ) {
                            AnimatedIntCounter(
                                targetValue = appUsers.size,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                ),
                                color = colors.textPrimary,
                                suffix = " مستخدم"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Actionable Orders Carousel ("أوردرات تحتاج تنفيذ الآن") with 1-Tap Status Advance
            StaggeredAppearItem(index = 2) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = colors.primaryBrand,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أوردرات تحتاج تنفيذ الآن (${actionableOrders.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                        }
                        TextButton(onClick = onNavigateToOrders) {
                            Text(
                                text = "كل الطلبيات",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.primaryBrand,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = colors.primaryBrand,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (actionableOrders.isEmpty()) {
                        LuxuryCard(cornerRadius = 22.dp, elevation = 2.dp) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(colors.profitContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.profitGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "كل الأوردرات الحالية مكتملة ✅",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "لا توجد طلبات معلقة أو قيد التجهيز في هذا الشهر",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        val actionListState = rememberLazyListState()
                        val actionSnap = rememberSnapFlingBehavior(lazyListState = actionListState)

                        LazyRow(
                            state = actionListState,
                            flingBehavior = actionSnap,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 2.dp),
                            modifier = Modifier.testTag("dashboard_actionable_orders_row")
                        ) {
                            items(actionableOrders, key = { "action_${it.id}" }) { order ->
                                ActionableOrderQuickCard(
                                    order = order,
                                    onCardClick = { onOrderClick(order) },
                                    onAdvanceStatus = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleOrderStatus(order)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Quick Action Navigation Row
            StaggeredAppearItem(index = 3) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionTile(
                        title = "أوردر جديد",
                        icon = Icons.Default.Add,
                        accentBrush = colors.warmAccentGradient,
                        contentColor = colors.onAccentText,
                        borderColor = colors.primaryBrand,
                        onClick = onOpenAddOrderDialog,
                        modifier = Modifier.weight(1.25f)
                    )
                    QuickActionTile(
                        title = "الأصناف",
                        icon = Icons.Default.Inventory2,
                        accentBrush = Brush.horizontalGradient(listOf(colors.cardSurface, colors.cardSurface)),
                        contentColor = colors.textPrimary,
                        iconTint = colors.goldText,
                        borderColor = colors.cardBorder,
                        onClick = onNavigateToProducts,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        title = "المنيو",
                        icon = Icons.Default.RestaurantMenu,
                        accentBrush = Brush.horizontalGradient(listOf(colors.cardSurface, colors.cardSurface)),
                        contentColor = colors.textPrimary,
                        iconTint = colors.primaryBrand,
                        borderColor = colors.cardBorder,
                        onClick = onNavigateToMenu,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Expandable Registered App Users List
            StaggeredAppearItem(index = 4) {
                LuxuryCard(
                    cornerRadius = 22.dp,
                    elevation = 3.dp,
                    modifier = Modifier.testTag("dashboard_users_list_card")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .animateContentSize(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick { usersExpanded = !usersExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colors.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
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

                        AnimatedVisibility(
                            visible = usersExpanded,
                            enter = expandVertically(spring(dampingRatio = 0.85f)) + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
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
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, if (isLive) colors.profitGreen.copy(alpha = 0.4f) else colors.cardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Surface(
                                                        color = if (isLive) colors.profitContainer else colors.cardSurface,
                                                        shape = RoundedCornerShape(50),
                                                        border = BorderStroke(1.dp, if (isLive) colors.profitGreen.copy(alpha = 0.3f) else colors.cardBorder)
                                                    ) {
                                                        Text(
                                                            text = if (isLive) "متصل الآن" else "غير متصل",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isLive) colors.onProfitContainer else colors.textSecondary,
                                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                    Surface(
                                                        onClick = { userPendingDeletion = u },
                                                        color = colors.errorContainer,
                                                        shape = RoundedCornerShape(50),
                                                        border = BorderStroke(1.dp, colors.errorRed.copy(alpha = 0.3f)),
                                                        modifier = Modifier
                                                            .minimumInteractiveComponentSize()
                                                            .defaultMinSize(minHeight = 36.dp)
                                                            .bounceClick()
                                                            .testTag("delete_user_btn_${u.userId}")
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.DeleteOutline,
                                                                contentDescription = "حذف الحساب",
                                                                tint = colors.errorRed,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = "حذف الحساب",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = colors.onErrorContainer
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Charts & Top Products
            StaggeredAppearItem(index = 5) {
                SalesAndProfitChartCard(points = stats.dailyPoints)
            }
            Spacer(modifier = Modifier.height(12.dp))
            StaggeredAppearItem(index = 6) {
                ProductPerformanceCard(topProducts = stats.topProducts)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 7. Recent Orders Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أحدث الأوردرات المسجلة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
                TextButton(onClick = onNavigateToOrders) {
                    Text("عرض الكل (${recentOrders.size})", style = MaterialTheme.typography.labelLarge, color = colors.primaryBrand, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (recentOrders.isEmpty()) {
                LuxuryCard(cornerRadius = 24.dp, elevation = 3.dp) {
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
                                .size(92.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد أوردرات مسجلة بعد", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
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
                Text("فتح التقارير التفصيلية والمصاريف", color = colors.primaryBrand, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = colors.cardSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_footer_branding")
            ) {
                Text(
                    text = "أكلة حلوة — بإدارة أ/ نهى ماهر",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.goldText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }
        }
    }

    userPendingDeletion?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { userPendingDeletion = null },
            containerColor = colors.cardSurface,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = colors.errorRed
                )
            },
            title = {
                Text(
                    text = "تأكيد حذف الحساب",
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "هل أنتِ متأكدة من حذف حساب (${targetUser.username}) نهائياً؟ سيتم حذفه من السحابة ومن قائمة المستخدمين فوراً.",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAppUser(targetUser)
                        userPendingDeletion = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.errorRed),
                    modifier = Modifier.testTag("confirm_delete_user_btn")
                ) {
                    Text("حذف نهائياً", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userPendingDeletion = null }) {
                    Text("إلغاء", color = colors.textSecondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun CloudSyncBadgePill(cloudSyncState: CloudSyncState) {
    val colors = AppTheme.colors
    val isConnected = cloudSyncState is CloudSyncState.Connected
    val isConnecting = cloudSyncState is CloudSyncState.Connecting
    val isError = cloudSyncState is CloudSyncState.Error
    val pillBg = when {
        isConnected -> colors.profitContainer
        isError -> colors.errorContainer
        else -> colors.secondaryContainer
    }
    val pillFg = when {
        isConnected -> colors.onProfitContainer
        isError -> colors.onErrorContainer
        else -> colors.goldText
    }
    val icon = when {
        isConnected -> Icons.Default.CloudDone
        isError -> Icons.Default.CloudOff
        else -> Icons.Default.CloudSync
    }
    val label = when (cloudSyncState) {
        is CloudSyncState.Connected -> "متصل بالسحابة لحظياً"
        is CloudSyncState.Connecting -> "جاري الاتصال بالسحابة..."
        is CloudSyncState.Error -> "تعذر الاتصال السحابي"
        is CloudSyncState.LocalOffline -> "الوضع المحلي نشط"
    }

    val pulseTransition = rememberInfiniteTransition(label = "cloud_sync_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "cloud_dot_scale"
    )

    Surface(
        color = pillBg,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, pillFg.copy(alpha = 0.25f)),
        modifier = Modifier.testTag("dashboard_cloud_sync_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isConnected || isConnecting) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(if (isConnected) colors.profitGreen else colors.goldAccent)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = pillFg,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = pillFg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BentoMetricTile(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    badgeBg: Color,
    modifier: Modifier = Modifier,
    valueContent: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    LuxuryCard(
        cornerRadius = 22.dp,
        elevation = 3.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(badgeBg)
                        .border(1.dp, iconTint.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            valueContent()
        }
    }
}

@Composable
private fun ActionableOrderQuickCard(
    order: Order,
    onCardClick: () -> Unit,
    onAdvanceStatus: () -> Unit
) {
    val colors = AppTheme.colors
    val isNew = order.status == OrderStatus.NEW_CUSTOMER.name || order.status == OrderStatus.PENDING_REVIEW.name
    val isOut = order.status == OrderStatus.OUT_FOR_DELIVERY.name
    val accentColor by animateColorAsState(
        targetValue = when {
            isNew -> colors.primaryBrand
            isOut -> colors.profitGreen
            else -> colors.goldAccent
        },
        animationSpec = tween(220),
        label = "action_order_accent"
    )
    val nextStatus = OrderStatus.nextAdminStatus(order.status)

    LuxuryCard(
        cornerRadius = 22.dp,
        borderColor = accentColor.copy(alpha = 0.4f),
        elevation = 4.dp,
        onClick = onCardClick,
        modifier = Modifier
            .width(284.dp)
            .testTag("actionable_order_card_${order.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = accentColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = OrderStatus.trackingTitle(order.status),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isNew) colors.primaryBrand else if (isOut) colors.profitGreen else colors.goldText,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = order.itemsDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = DateUtils.formatCurrency(order.salePrice),
                            style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.goldText
                        )
                        Text(
                            text = "ربح +${DateUtils.formatCurrency(order.netProfit)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.profitGreen
                        )
                    }

                    val actionButtonTextColor = when {
                        isNew -> colors.onAccentText
                        isOut -> Color.White
                        else -> LightTextPrimary
                    }
                    Surface(
                        onClick = onAdvanceStatus,
                        color = accentColor,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .defaultMinSize(minHeight = 40.dp)
                            .bounceClick()
                            .testTag("quick_advance_order_${order.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (nextStatus == OrderStatus.OUT_FOR_DELIVERY) Icons.Default.LocalShipping else Icons.Default.Check,
                                contentDescription = null,
                                tint = actionButtonTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = OrderStatus.trackingTitle(nextStatus.name),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = actionButtonTextColor,
                                maxLines = 1
                            )
                        }
                    }
                }
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
        animationSpec = infiniteRepeatable(animation = tween(850), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )

    LuxuryCard(
        cornerRadius = 22.dp,
        borderColor = colors.profitGreen.copy(alpha = 0.35f),
        elevation = 3.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المتصلون الآن",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(colors.profitContainer, CircleShape)
                        .border(1.dp, colors.profitGreen.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(colors.profitGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedIntCounter(
                    targetValue = onlineCount,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                    color = colors.profitGreen,
                    suffix = " متصل"
                )
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    icon: ImageVector,
    accentBrush: Brush,
    contentColor: Color,
    borderColor: Color,
    iconTint: Color = contentColor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .wrapContentHeight()
            .shadow(if (colors.isDark) 0.dp else 4.dp, RoundedCornerShape(20.dp), ambientColor = colors.softShadow, spotColor = colors.softShadow)
            .clip(RoundedCornerShape(20.dp))
            .background(accentBrush)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentOrderItem(order: Order, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val isDelivered = order.status == OrderStatus.DELIVERED.name
    LuxuryCard(
        cornerRadius = 22.dp,
        elevation = 3.dp,
        onClick = onClick,
        modifier = Modifier.testTag("recent_order_${order.id}")
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
                        .size(44.dp)
                        .background(colors.warmAccentGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(order.customerName.take(1).ifBlank { "ع" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.onAccentText)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(order.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        Surface(
                            color = if (isDelivered) colors.profitContainer else colors.secondaryContainer,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, if (isDelivered) colors.profitGreen.copy(alpha = 0.3f) else colors.goldAccent.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = OrderStatus.trackingTitle(order.status),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDelivered) colors.onProfitContainer else colors.goldText,
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
                    Spacer(modifier = Modifier.height(3.dp))
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
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = DateUtils.formatCurrency(order.salePrice),
                        style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.goldText
                    )
                    Text(
                        text = "+${DateUtils.formatCurrency(order.netProfit)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        color = colors.profitGreen
                    )
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
