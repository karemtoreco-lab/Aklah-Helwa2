package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.ui.components.CheckeredRibbon
import com.example.ui.components.OrderDetailDialog
import com.example.ui.components.OrderFormDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.MonthlyStatsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.theme.BrandRedPrimary
import com.example.ui.theme.CharcoalBlackBg
import com.example.ui.theme.CharcoalCardBorder
import com.example.ui.theme.CharcoalCardSurface
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextWhiteSecondary
import com.example.util.DateUtils
import com.example.viewmodel.AppTab
import com.example.viewmodel.OrderViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AklehHelwaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AklehHelwaApp(viewModel: OrderViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val filteredOrders by viewModel.filteredOrders.collectAsStateWithLifecycle()
    val ordersForSelectedMonth by viewModel.ordersForSelectedMonth.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val monthlyStats by viewModel.monthlyStats.collectAsStateWithLifecycle()

    val isOrderDialogOpen by viewModel.isOrderDialogOpen.collectAsStateWithLifecycle()
    val editingOrder by viewModel.editingOrder.collectAsStateWithLifecycle()
    val selectedOrderDetails by viewModel.selectedOrderDetails.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBlackBg),
        containerColor = CharcoalBlackBg,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("app_header")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFF3D00), BrandRedPrimary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fastfood,
                                    contentDescription = null,
                                    tint = CleanWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "أكلة حلوة",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CleanWhite
                                )
                                Text(
                                    text = "أشهى الأكلات المنزلية وحسابات الأرباح",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextWhiteSecondary
                                )
                            }
                        }
                    },
                    actions = {
                        // Brand month pill
                        Surface(
                            color = Color(0xFF381212),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandRedPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .testTag("header_month_chip")
                        ) {
                            Text(
                                text = DateUtils.formatYearMonthArabic(selectedYearMonth),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandRedPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CharcoalCardSurface
                    )
                )

                // Subtle Red and White Checkered ribbon under top bar
                CheckeredRibbon(height = 4.dp)
            }
        },
        bottomBar = {
            Column {
                CheckeredRibbon(height = 3.dp)

                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .shadow(elevation = 10.dp)
                        .testTag("bottom_navigation"),
                    containerColor = CharcoalCardSurface,
                    tonalElevation = 6.dp
                ) {
                    // Tab 1: Dashboard
                    NavigationBarItem(
                        selected = currentTab == AppTab.DASHBOARD,
                        onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "الرئيسية"
                            )
                        },
                        label = { Text("الرئيسية", fontWeight = if (currentTab == AppTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CleanWhite,
                            selectedTextColor = BrandRedPrimary,
                            unselectedIconColor = TextWhiteSecondary,
                            unselectedTextColor = TextWhiteSecondary,
                            indicatorColor = BrandRedPrimary
                        ),
                        modifier = Modifier.testTag("tab_dashboard")
                    )

                    // Tab 2: Orders
                    NavigationBarItem(
                        selected = currentTab == AppTab.ORDERS,
                        onClick = { viewModel.setTab(AppTab.ORDERS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.ORDERS) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "الطلبيات"
                            )
                        },
                        label = { Text("الطلبيات", fontWeight = if (currentTab == AppTab.ORDERS) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CleanWhite,
                            selectedTextColor = BrandRedPrimary,
                            unselectedIconColor = TextWhiteSecondary,
                            unselectedTextColor = TextWhiteSecondary,
                            indicatorColor = BrandRedPrimary
                        ),
                        modifier = Modifier.testTag("tab_orders")
                    )

                    // Tab 3: Menu
                    NavigationBarItem(
                        selected = currentTab == AppTab.MENU,
                        onClick = { viewModel.setTab(AppTab.MENU) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.MENU) Icons.Default.RestaurantMenu else Icons.Outlined.RestaurantMenu,
                                contentDescription = "المنيو"
                            )
                        },
                        label = { Text("المنيو", fontWeight = if (currentTab == AppTab.MENU) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CleanWhite,
                            selectedTextColor = BrandRedPrimary,
                            unselectedIconColor = TextWhiteSecondary,
                            unselectedTextColor = TextWhiteSecondary,
                            indicatorColor = BrandRedPrimary
                        ),
                        modifier = Modifier.testTag("tab_menu")
                    )

                    // Tab 4: Monthly Stats
                    NavigationBarItem(
                        selected = currentTab == AppTab.MONTHLY_STATS,
                        onClick = { viewModel.setTab(AppTab.MONTHLY_STATS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppTab.MONTHLY_STATS) Icons.Default.TrendingUp else Icons.Outlined.TrendingUp,
                                contentDescription = "التقارير"
                            )
                        },
                        label = { Text("التقارير", fontWeight = if (currentTab == AppTab.MONTHLY_STATS) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CleanWhite,
                            selectedTextColor = BrandRedPrimary,
                            unselectedIconColor = TextWhiteSecondary,
                            unselectedTextColor = TextWhiteSecondary,
                            indicatorColor = BrandRedPrimary
                        ),
                        modifier = Modifier.testTag("tab_monthly_stats")
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddOrderDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = CleanWhite) },
                text = { Text("أوردر جديد", fontWeight = FontWeight.Bold, color = CleanWhite) },
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .testTag("fab_add_order"),
                containerColor = BrandRedPrimary,
                contentColor = CleanWhite,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CharcoalBlackBg)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.DASHBOARD -> {
                        DashboardScreen(
                            stats = monthlyStats,
                            selectedYearMonth = selectedYearMonth,
                            recentOrders = ordersForSelectedMonth,
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onNavigateToOrders = { viewModel.setTab(AppTab.ORDERS) },
                            onNavigateToMenu = { viewModel.setTab(AppTab.MENU) },
                            onNavigateToAnalytics = { viewModel.setTab(AppTab.MONTHLY_STATS) },
                            onOpenAddOrderDialog = { viewModel.openAddOrderDialog() },
                            onOrderClick = { viewModel.showOrderDetails(it) },
                            onSaveQuickOrder = { name, value, profit, items, phone ->
                                val cost = (value - profit).coerceAtLeast(0.0)
                                val finalItems = if (items.isBlank()) "طلب طعام متنوع" else items
                                viewModel.saveOrder(
                                    customerName = name,
                                    customerPhone = phone,
                                    itemsDescription = finalItems,
                                    salePrice = value,
                                    costPrice = cost,
                                    orderDate = System.currentTimeMillis(),
                                    status = OrderStatus.DELIVERED.name,
                                    paymentStatus = PaymentStatus.PAID.name,
                                    notes = ""
                                )
                            }
                        )
                    }

                    AppTab.ORDERS -> {
                        OrdersScreen(
                            orders = filteredOrders,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            statusFilter = statusFilter,
                            onStatusFilterChange = { viewModel.setStatusFilter(it) },
                            onOrderClick = { viewModel.showOrderDetails(it) },
                            onToggleStatus = { viewModel.toggleOrderStatus(it) },
                            onAddOrderClick = { viewModel.openAddOrderDialog() },
                            generateReceiptText = { viewModel.generateOrderReceiptText(it) }
                        )
                    }

                    AppTab.MENU -> {
                        MenuScreen(
                            menuItems = viewModel.menuItems,
                            onSelectMenuItem = { viewModel.openAddOrderForMenuItem(it) }
                        )
                    }

                    AppTab.MONTHLY_STATS -> {
                        MonthlyStatsScreen(
                            stats = monthlyStats,
                            selectedYearMonth = selectedYearMonth,
                            availableMonths = availableMonths,
                            onSelectMonth = { viewModel.selectMonth(it) },
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onShareReport = {
                                val text = viewModel.generateMonthlyReportText()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة تقرير الشهر"))
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog for adding or editing an order
    if (isOrderDialogOpen) {
        OrderFormDialog(
            initialOrder = editingOrder,
            onDismiss = { viewModel.closeOrderDialog() },
            onSaveOrder = { id, name, phone, items, sale, cost, date, status, payment, notes ->
                viewModel.saveOrder(
                    id = id,
                    customerName = name,
                    customerPhone = phone,
                    itemsDescription = items,
                    salePrice = sale,
                    costPrice = cost,
                    orderDate = date,
                    status = status,
                    paymentStatus = payment,
                    notes = notes
                )
            }
        )
    }

    // Dialog for viewing order details
    selectedOrderDetails?.let { order ->
        OrderDetailDialog(
            order = order,
            onDismiss = { viewModel.showOrderDetails(null) },
            onEdit = {
                viewModel.showOrderDetails(null)
                viewModel.openAddOrderDialog(order)
            },
            onDelete = { viewModel.deleteOrder(order) },
            onToggleStatus = { viewModel.toggleOrderStatus(order) },
            onTogglePayment = { viewModel.togglePaymentStatus(order) },
            receiptText = viewModel.generateOrderReceiptText(order)
        )
    }
}
