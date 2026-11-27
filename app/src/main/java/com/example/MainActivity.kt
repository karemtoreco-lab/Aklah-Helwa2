package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppStage
import com.example.service.OrderNotificationService
import com.example.ui.components.AdminSettingsDialog
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.CheckeredRibbon
import com.example.ui.components.DockNavItem
import com.example.ui.components.FloatingNavDock
import com.example.ui.components.OrderCelebrationDialog
import com.example.ui.components.OrderDetailDialog
import com.example.ui.components.OrderFormDialog
import com.example.ui.components.ThemeToggleIconButton
import com.example.ui.components.bounceClick
import com.example.ui.screens.CustomerStorefrontScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MenuManagementScreen
import com.example.ui.screens.MonthlyStatsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductCatalogScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.util.DateUtils
import com.example.util.WhatsAppUtils
import com.example.viewmodel.AppTab
import com.example.viewmodel.OrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {

    private val viewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AklehHelwaApp(viewModel = viewModel, isDarkMode = isDarkMode)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(OrderNotificationService.EXTRA_OPEN_ADMIN_ORDERS, false) == true) {
            viewModel.openAdminOrdersFromNotification()
        }
    }
}

@Composable
fun AklehHelwaApp(
    viewModel: OrderViewModel,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val appStage by viewModel.appStage.collectAsStateWithLifecycle()
    val adminPin by viewModel.adminPin.collectAsStateWithLifecycle()
    val isAdminDevice by viewModel.isAdminDevice.collectAsStateWithLifecycle()
    val adminWhatsAppNumber by viewModel.adminWhatsAppNumber.collectAsStateWithLifecycle()
    val cloudConfig by viewModel.cloudConfig.collectAsStateWithLifecycle()
    val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()

    val customerProfile by viewModel.customerProfile.collectAsStateWithLifecycle()
    val storefrontCategories by viewModel.storefrontCategories.collectAsStateWithLifecycle()
    val storefrontItems by viewModel.storefrontItems.collectAsStateWithLifecycle()
    val primaryImagesMap by viewModel.primaryImagesMap.collectAsStateWithLifecycle()
    val customerCart by viewModel.customerCart.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteMenuItemIds.collectAsStateWithLifecycle()
    val lastReorderItems by viewModel.lastReorderItems.collectAsStateWithLifecycle()
    val kitchenOpen by viewModel.kitchenOpen.collectAsStateWithLifecycle()
    val dailyOfferText by viewModel.dailyOfferText.collectAsStateWithLifecycle()
    val deliveryZones by viewModel.deliveryZones.collectAsStateWithLifecycle()
    val trackedOrders by viewModel.customerTrackedOrders.collectAsStateWithLifecycle()
    val appVersionConfig by viewModel.appVersionConfig.collectAsStateWithLifecycle()
    val isUpdateDismissed by viewModel.isUpdateDismissed.collectAsStateWithLifecycle()
    val celebrationMessage by viewModel.celebrationMessage.collectAsStateWithLifecycle()
    val customerUiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    var requestedAdminNotifPermission by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        if (isAdminDevice) {
            OrderNotificationService.start(context)
        }
    }

    // Start Admin-only real-time ForegroundService strictly on Admin devices
    LaunchedEffect(isAdminDevice) {
        if (isAdminDevice) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted && !requestedAdminNotifPermission) {
                    requestedAdminNotifPermission = true
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            OrderNotificationService.start(context)
        }
    }

    // Live User Presence lifecycle observer (ON_START -> online, ON_STOP -> offline)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.setUserPresence(true)
                Lifecycle.Event.ON_STOP -> viewModel.setUserPresence(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Periodic 30s presence heartbeat while foregrounded
    LaunchedEffect(lifecycleOwner) {
        while (isActive) {
            delay(30_000L)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                viewModel.setUserPresence(true)
            }
        }
    }

    AnimatedContent(
        targetState = appStage,
        transitionSpec = {
            (fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.985f, animationSpec = spring(dampingRatio = 0.85f))) togetherWith
                fadeOut(animationSpec = tween(200))
        },
        label = "app_stage_transition"
    ) { stage ->
        when (stage) {
            AppStage.SPLASH -> {
                SplashScreen(
                    onFinished = { viewModel.completeSplash() }
                )
            }

            AppStage.ONBOARDING -> {
                OnboardingScreen(
                    isDarkMode = isDarkMode,
                    adminPin = adminPin,
                    onToggleTheme = { viewModel.toggleDarkMode() },
                    onCompleteOnboarding = { username, password ->
                        viewModel.completeOnboarding(username, password)
                    },
                    onEnterAdmin = { viewModel.enterAdminPanel() }
                )
            }

            AppStage.CUSTOMER_STOREFRONT -> {
                CustomerStorefrontScreen(
                    categories = storefrontCategories,
                    items = storefrontItems,
                    primaryImages = primaryImagesMap,
                    cartItems = customerCart,
                    customerProfile = customerProfile,
                    favoriteIds = favoriteIds,
                    hasPreviousOrder = lastReorderItems.isNotEmpty(),
                    kitchenOpen = kitchenOpen,
                    dailyOfferText = dailyOfferText,
                    deliveryZones = deliveryZones,
                    trackedOrders = trackedOrders,
                    isDarkMode = isDarkMode,
                    adminPin = adminPin,
                    cloudSyncState = cloudSyncState,
                    uiMessage = customerUiMessage,
                    onClearUiMessage = { viewModel.clearUiMessage() },
                    onCheckForUpdates = { viewModel.checkForUpdatesManually() },
                    onToggleTheme = { viewModel.toggleDarkMode() },
                    onEnterAdmin = { viewModel.enterAdminPanel() },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onReorderLastOrder = { viewModel.reorderLastOrder() },
                    onReorderFromTrackedOrder = { viewModel.reorderFromTrackedOrder(it) },
                    onSubmitOrderRating = { order, stars -> viewModel.submitCustomerOrderRating(order, stars) },
                    onAddToCart = { item, img, qty -> viewModel.addToCustomerCart(item, img, qty) },
                    onUpdateCartQuantity = { id, delta -> viewModel.updateCustomerCartQuantity(id, delta) },
                    onClearCart = { viewModel.clearCustomerCart() },
                    onSubmitOrder = { name, phone, zone, address, notes ->
                        viewModel.submitCustomerOrder(
                            customerName = name,
                            customerPhone = phone,
                            deliveryZone = zone,
                            deliveryAddress = address,
                            notes = notes,
                            onOrderReadyForWhatsApp = { waMessage, adminNumber ->
                                WhatsAppUtils.launchWhatsAppOrderIntent(
                                    context = context,
                                    rawAdminPhone = adminNumber,
                                    message = waMessage
                                )
                            }
                        )
                    }
                )
            }

            AppStage.ADMIN_PANEL -> {
                BackHandler {
                    viewModel.returnToCustomerStorefront()
                }
                AdminPanelScaffold(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    adminPin = adminPin,
                    adminWhatsAppNumber = adminWhatsAppNumber,
                    kitchenOpen = kitchenOpen,
                    dailyOfferText = dailyOfferText,
                    deliveryZones = deliveryZones,
                    cloudConfig = cloudConfig,
                    cloudSyncState = cloudSyncState
                )
            }
        }
    }

    celebrationMessage?.let { msg ->
        OrderCelebrationDialog(
            message = msg,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }

    val remoteUpdate = appVersionConfig
    if ((appStage == AppStage.CUSTOMER_STOREFRONT || appStage == AppStage.ADMIN_PANEL) &&
        remoteUpdate != null &&
        remoteUpdate.isUpdateAvailable(BuildConfig.VERSION_CODE) &&
        (!isUpdateDismissed || remoteUpdate.isMandatory)
    ) {
        AppUpdateDialog(
            versionConfig = remoteUpdate,
            onDismiss = { viewModel.dismissUpdateDialog() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPanelScaffold(
    viewModel: OrderViewModel,
    isDarkMode: Boolean,
    adminPin: String,
    adminWhatsAppNumber: String,
    kitchenOpen: Boolean,
    dailyOfferText: String,
    deliveryZones: List<com.example.model.DeliveryZone>,
    cloudConfig: com.example.model.CloudConfig,
    cloudSyncState: com.example.model.CloudSyncState
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val currentTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val filteredOrders by viewModel.filteredOrders.collectAsStateWithLifecycle()
    val ordersForSelectedMonth by viewModel.ordersForSelectedMonth.collectAsStateWithLifecycle()
    val orderItemsMap by viewModel.orderItemsMap.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val monthlyStats by viewModel.monthlyStats.collectAsStateWithLifecycle()
    val expensesForSelectedMonth by viewModel.expensesForSelectedMonth.collectAsStateWithLifecycle()
    val cloudAppUsers by viewModel.cloudAppUsers.collectAsStateWithLifecycle()

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val primaryImagesMap by viewModel.primaryImagesMap.collectAsStateWithLifecycle()
    val imagesByProductMap by viewModel.imagesByProductMap.collectAsStateWithLifecycle()

    val menuCategories by viewModel.menuCategories.collectAsStateWithLifecycle()
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    val generatedMenus by viewModel.generatedMenus.collectAsStateWithLifecycle()
    val previewBitmaps by viewModel.previewBitmaps.collectAsStateWithLifecycle()
    val selectedPreviewCategoryId by viewModel.selectedPreviewCategoryId.collectAsStateWithLifecycle()
    val isGeneratingPreview by viewModel.isGeneratingPreview.collectAsStateWithLifecycle()

    val isOrderDialogOpen by viewModel.isOrderDialogOpen.collectAsStateWithLifecycle()
    val editingOrder by viewModel.editingOrder.collectAsStateWithLifecycle()
    val editingDraftItems by viewModel.editingDraftItems.collectAsStateWithLifecycle()
    val selectedOrderDetails by viewModel.selectedOrderDetails.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    LaunchedEffect(uiMessage) {
        val msg = uiMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    val dockItems = remember(currentTab) {
        listOf(
            DockNavItem(
                title = "الرئيسية",
                selectedIcon = Icons.Default.Dashboard,
                unselectedIcon = Icons.Outlined.Dashboard,
                testTag = "tab_dashboard",
                selected = currentTab == AppTab.DASHBOARD,
                onClick = { viewModel.setTab(AppTab.DASHBOARD) }
            ),
            DockNavItem(
                title = "الطلبيات",
                selectedIcon = Icons.Default.ReceiptLong,
                unselectedIcon = Icons.Outlined.ReceiptLong,
                testTag = "tab_orders",
                selected = currentTab == AppTab.ORDERS,
                onClick = { viewModel.setTab(AppTab.ORDERS) }
            ),
            DockNavItem(
                title = "الأصناف",
                selectedIcon = Icons.Default.Inventory2,
                unselectedIcon = Icons.Outlined.Inventory2,
                testTag = "tab_products",
                selected = currentTab == AppTab.PRODUCTS,
                onClick = { viewModel.setTab(AppTab.PRODUCTS) }
            ),
            DockNavItem(
                title = "المنيو",
                selectedIcon = Icons.Default.RestaurantMenu,
                unselectedIcon = Icons.Outlined.RestaurantMenu,
                testTag = "tab_menu",
                selected = currentTab == AppTab.MENU,
                onClick = { viewModel.setTab(AppTab.MENU) }
            ),
            DockNavItem(
                title = "التقارير",
                selectedIcon = Icons.Default.TrendingUp,
                unselectedIcon = Icons.Outlined.TrendingUp,
                testTag = "tab_monthly_stats",
                selected = currentTab == AppTab.MONTHLY_STATS,
                onClick = { viewModel.setTab(AppTab.MONTHLY_STATS) }
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundGradient),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(colors.cardSurface).statusBarsPadding()) {
                CheckeredRibbon(height = 3.dp, squareSize = 4.dp)
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.testTag("app_header")) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "شعار أكلة حلوة",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .shadow(4.dp, CircleShape, spotColor = colors.goldAccent.copy(alpha = 0.45f))
                                    .clip(CircleShape)
                                    .border(1.5.dp, colors.goldAccent, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "أكلة حلوة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "إدارة أ/ نهى ماهر",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.goldText,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        Surface(
                            color = colors.secondaryContainer,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.45f)),
                            modifier = Modifier.testTag("header_month_chip")
                        ) {
                            Text(
                                text = DateUtils.formatYearMonthArabic(selectedYearMonth),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.goldText,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { viewModel.returnToCustomerStorefront() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.secondarySurface)
                                .border(1.dp, colors.cardBorder, CircleShape)
                                .testTag("admin_return_customer_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "العودة لواجهة العملاء",
                                tint = colors.goldText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        ThemeToggleIconButton(
                            isDarkMode = isDarkMode,
                            onToggle = { viewModel.toggleDarkMode() }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                viewModel.clearForceResyncFeedback()
                                showSettingsDialog = true
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.secondarySurface)
                                .border(1.dp, colors.cardBorder, CircleShape)
                                .testTag("admin_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "إعدادات الإدارة والمزامنة",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.cardSurface)
                )
                HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
            }
        },
        bottomBar = {
            FloatingNavDock(items = dockItems)
        },
        floatingActionButton = {
            if (currentTab == AppTab.DASHBOARD || currentTab == AppTab.ORDERS) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddOrderDialog() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = colors.onAccentText) },
                    text = { Text("أوردر جديد", fontWeight = FontWeight.ExtraBold, color = colors.onAccentText) },
                    modifier = Modifier
                        .shadow(10.dp, RoundedCornerShape(50), spotColor = colors.primaryBrand.copy(alpha = 0.35f))
                        .bounceClick()
                        .testTag("fab_add_order"),
                    containerColor = colors.primaryBrand,
                    contentColor = colors.onAccentText,
                    shape = RoundedCornerShape(50)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(colors.backgroundGradient)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val enterOffset: (Int) -> Int = { fullWidth ->
                        if (forward) -fullWidth / 5 else fullWidth / 5
                    }
                    val exitOffset: (Int) -> Int = { fullWidth ->
                        if (forward) fullWidth / 7 else -fullWidth / 7
                    }
                    (slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.86f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetX = enterOffset
                    ) + fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(190, easing = FastOutSlowInEasing),
                            targetOffsetX = exitOffset
                        ) + fadeOut(animationSpec = tween(160)))
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.DASHBOARD -> {
                        DashboardScreen(
                            stats = monthlyStats,
                            selectedYearMonth = selectedYearMonth,
                            recentOrders = ordersForSelectedMonth,
                            appUsers = cloudAppUsers,
                            kitchenOpen = kitchenOpen,
                            onToggleKitchenOpen = { viewModel.toggleKitchenOpen(it) },
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onNavigateToOrders = { viewModel.setTab(AppTab.ORDERS) },
                            onNavigateToProducts = { viewModel.setTab(AppTab.PRODUCTS) },
                            onNavigateToMenu = { viewModel.setTab(AppTab.MENU) },
                            onNavigateToAnalytics = { viewModel.setTab(AppTab.MONTHLY_STATS) },
                            onOpenAddOrderDialog = { viewModel.openAddOrderDialog() },
                            onOrderClick = { viewModel.showOrderDetails(it) },
                            onCheckForUpdates = { viewModel.checkForUpdatesManually() },
                            cloudSyncState = cloudSyncState,
                            onToggleOrderStatus = { viewModel.toggleOrderStatus(it) },
                            onDeleteAppUser = { viewModel.deleteAppUser(it) }
                        )
                    }

                    AppTab.ORDERS -> {
                        OrdersScreen(
                            orders = filteredOrders,
                            orderItemsMap = orderItemsMap,
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

                    AppTab.PRODUCTS -> {
                        ProductCatalogScreen(
                            categories = categories,
                            products = products,
                            primaryImages = primaryImagesMap,
                            imagesByProduct = imagesByProductMap,
                            onSaveCategory = { id, name -> viewModel.saveCategory(id, name) },
                            onDeleteCategory = { viewModel.deleteCategory(it) },
                            onSaveProduct = { id, name, catId, unit, desc, purchase, selling, active ->
                                viewModel.saveProduct(id, name, catId, unit, desc, purchase, selling, active)
                            },
                            onToggleProductActive = { viewModel.toggleProductActive(it) },
                            onDeleteProduct = { viewModel.deleteProduct(it) },
                            onAddImagesForProduct = { prodId, uris -> viewModel.addProductImagesFromUris(prodId, uris) },
                            onSetPrimaryImage = { prodId, imgId -> viewModel.setPrimaryProductImage(prodId, imgId) },
                            onDeleteImage = { viewModel.deleteProductImage(it) }
                        )
                    }

                    AppTab.MENU -> {
                        MenuManagementScreen(
                            menuCategories = menuCategories,
                            menuItems = menuItems,
                            catalogProducts = products,
                            primaryImages = primaryImagesMap,
                            previewBitmaps = previewBitmaps,
                            selectedPreviewCategoryId = selectedPreviewCategoryId,
                            isGeneratingPreview = isGeneratingPreview,
                            generatedMenus = generatedMenus,
                            onAddMenuCategory = { viewModel.addMenuCategory(it) },
                            onUpdateMenuCategory = { viewModel.updateMenuCategory(it) },
                            onDeleteMenuCategory = { viewModel.deleteMenuCategory(it) },
                            onMoveMenuCategory = { cat, up -> viewModel.moveMenuCategory(cat, up) },
                            onAddCatalogProductToMenu = { prod, menuCatId -> viewModel.addProductToMenu(prod, menuCatId) },
                            onAddManualMenuItem = { menuCatId, name, unit, desc, price, isVar, isPopular, isPreOrder24h ->
                                viewModel.addManualMenuItem(
                                    menuCategoryId = menuCatId,
                                    displayName = name,
                                    unitLabel = unit,
                                    description = desc,
                                    publishedPrice = price,
                                    isVariablePrice = isVar,
                                    isPopular = isPopular,
                                    isPreOrder24h = isPreOrder24h
                                )
                            },
                            onUpdateMenuItem = { viewModel.updateMenuItem(it) },
                            onRestoreDefaultPrice = { viewModel.restoreMenuItemDefaultPrice(it) },
                            onDeleteMenuItem = { viewModel.deleteMenuItem(it) },
                            onMoveMenuItem = { item, up -> viewModel.moveMenuItem(item, up) },
                            onAddMenuItemPhotos = { item, uris -> viewModel.addMenuItemPhotosFromUris(item, uris) },
                            onRemoveMenuItemPhoto = { item, idx -> viewModel.removeMenuItemPhotoAt(item, idx) },
                            onSelectPreviewCategory = { viewModel.setPreviewCategoryFilter(it) },
                            onRefreshPreview = { viewModel.refreshMenuPreview() },
                            onExportAllPages = { onDone -> viewModel.exportMenuToPng(onExportComplete = onDone) },
                            onExportSinglePage = { pageIdx, onDone ->
                                viewModel.exportMenuToPng(singlePageZeroIndex = pageIdx, onExportComplete = onDone)
                            },
                            onDeleteGeneratedMenu = { viewModel.deleteGeneratedMenu(it) }
                        )
                    }

                    AppTab.MONTHLY_STATS -> {
                        MonthlyStatsScreen(
                            stats = monthlyStats,
                            expenses = expensesForSelectedMonth,
                            selectedYearMonth = selectedYearMonth,
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onAddExpense = { title, cat, amt, notes -> viewModel.saveExpense(title, cat, amt, notes) },
                            onDeleteExpense = { viewModel.deleteExpense(it) },
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

    if (showSettingsDialog) {
        val currentVersionConfig by viewModel.appVersionConfig.collectAsStateWithLifecycle()
        val isForceResyncing by viewModel.isForceResyncing.collectAsStateWithLifecycle()
        val forceResyncFeedback by viewModel.forceResyncFeedback.collectAsStateWithLifecycle()
        AdminSettingsDialog(
            isDarkMode = isDarkMode,
            currentPin = adminPin,
            adminWhatsAppNumber = adminWhatsAppNumber,
            dailyOfferText = dailyOfferText,
            deliveryZones = deliveryZones,
            appVersionConfig = currentVersionConfig,
            cloudConfig = cloudConfig,
            cloudSyncState = cloudSyncState,
            appUsers = cloudAppUsers,
            isForceResyncing = isForceResyncing,
            forceResyncFeedback = forceResyncFeedback,
            onToggleTheme = { viewModel.toggleDarkMode() },
            onUpdatePin = { viewModel.updateAdminPin(it) },
            onUpdateWhatsAppNumber = { viewModel.updateAdminWhatsAppNumber(it) },
            onUpdateDailyOffer = { viewModel.updateDailyOffer(it) },
            onSaveDeliveryZones = { viewModel.saveDeliveryZones(it) },
            onPublishAppVersion = { code, name, url, notes, mandatory ->
                viewModel.publishAppVersionConfig(code, name, url, notes, mandatory)
            },
            onSyncMenuNow = { viewModel.syncPublishedMenuToCloudNow() },
            onForceResyncNow = { viewModel.forceFullResyncFromCloud() },
            onDeleteAppUser = { viewModel.deleteAppUser(it) },
            onReturnToCustomerStorefront = { viewModel.returnToCustomerStorefront() },
            onDismiss = {
                viewModel.clearForceResyncFeedback()
                showSettingsDialog = false
            }
        )
    }

    if (isOrderDialogOpen) {
        OrderFormDialog(
            initialOrder = editingOrder,
            initialDraftItems = editingDraftItems,
            categories = categories,
            products = products,
            primaryImages = primaryImagesMap,
            onDismiss = { viewModel.closeOrderDialog() },
            onSaveOrder = { id, name, phone, lineItems, date, status, payment, notes, extraPrepCost, extraPrepNotes ->
                viewModel.saveMultiProductOrder(
                    id = id,
                    customerName = name,
                    customerPhone = phone,
                    lineItems = lineItems,
                    orderDate = date,
                    status = status,
                    paymentStatus = payment,
                    notes = notes,
                    extraPrepCost = extraPrepCost,
                    extraPrepNotes = extraPrepNotes
                )
            }
        )
    }

    selectedOrderDetails?.let { order ->
        OrderDetailDialog(
            order = order,
            lineItems = orderItemsMap[order.id].orEmpty(),
            onDismiss = { viewModel.showOrderDetails(null) },
            onEdit = {
                viewModel.showOrderDetails(null)
                viewModel.openAddOrderDialog(order)
            },
            onDelete = { viewModel.deleteOrder(order) },
            onSelectStatus = { nextStatus -> viewModel.updateOrderStatus(order, nextStatus) },
            onTogglePayment = { viewModel.togglePaymentStatus(order) },
            receiptText = viewModel.generateOrderReceiptText(order)
        )
    }
}
