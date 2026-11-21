package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.AppPreferences
import com.example.data.BusinessRepository
import com.example.data.CloudSyncManager
import com.example.data.OrderRepository
import com.example.model.AppStage
import com.example.model.AppUser
import com.example.model.AppVersionConfig
import com.example.model.CategoryEntity
import com.example.model.CloudConfig
import com.example.model.CloudSyncState
import com.example.model.CustomerCartItem
import com.example.model.CustomerProfile
import com.example.model.DailyStatPoint
import com.example.model.DeliveryZone
import com.example.model.DraftOrderLineItem
import com.example.model.ExpenseEntity
import com.example.model.GeneratedMenuEntity
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import com.example.model.MonthlyStats
import com.example.model.Order
import com.example.model.OrderLineItemEntity
import com.example.model.OrderSource
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.model.ProductEntity
import com.example.model.ProductImageEntity
import com.example.model.ShoppingListItem
import com.example.model.TopProductStat
import com.example.model.TrackedCloudOrder
import com.example.model.TreasurySummary
import com.example.model.TreasuryTransactionEntity
import com.example.model.TreasuryTxType
import com.example.util.Base64ImageUtils
import com.example.util.DateUtils
import com.example.util.ImageStorageUtils
import com.example.util.ImageUtils
import com.example.util.MenuPosterRenderer
import com.example.util.MenuPosterSection
import com.example.util.WhatsAppLaunchResult
import com.example.util.WhatsAppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.UUID

enum class AppTab {
    DASHBOARD,
    ORDERS,
    PRODUCTS,
    MENU,
    MONTHLY_STATS
}

@OptIn(ExperimentalCoroutinesApi::class)
class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val orderRepository: OrderRepository
    private val businessRepository: BusinessRepository
    private val appPreferences: AppPreferences
    private val cloudSyncManager: CloudSyncManager

    init {
        val db = AppDatabase.getDatabase(application)
        orderRepository = OrderRepository(db.orderDao())
        businessRepository = BusinessRepository(db.catalogDao(), db.menuDao(), db.expenseDao())
        appPreferences = AppPreferences(application)
        cloudSyncManager = CloudSyncManager(application, viewModelScope, orderRepository, businessRepository)

        viewModelScope.launch(Dispatchers.IO) {
            businessRepository.ensureInitialCatalog()
        }

        cloudSyncManager.configureAndStart(CloudConfig(), enableAdminListeners = true)

        viewModelScope.launch(Dispatchers.IO) {
            combine(
                appPreferences.cloudConfig,
                appPreferences.customerProfile
            ) { cfg, profile -> cfg to profile }
                .collectLatest { (cfg, profile) ->
                    cloudSyncManager.configureAndStart(
                        config = cfg,
                        enableAdminListeners = true,
                        currentUserId = profile.userId
                    )
                    if (profile.isRegistered) {
                        cloudSyncManager.upsertUserProfile(profile)
                    }
                }
        }
    }

    private val _appStage = MutableStateFlow(AppStage.SPLASH)
    val appStage: StateFlow<AppStage> = _appStage.asStateFlow()

    private val _selectedTab = MutableStateFlow(AppTab.DASHBOARD)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    val isDarkMode: StateFlow<Boolean> = appPreferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val adminPin: StateFlow<String> = appPreferences.adminPin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences.DEFAULT_ADMIN_PIN)

    val customerProfile: StateFlow<CustomerProfile> = appPreferences.customerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CustomerProfile())

    val isAdminDevice: StateFlow<Boolean> = appPreferences.isAdminDevice
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val favoriteMenuItemIds: StateFlow<Set<Long>> = appPreferences.favoriteMenuItemIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val lastReorderItems: StateFlow<List<Pair<Long, Double>>> = appPreferences.lastReorderItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminWhatsAppNumber: StateFlow<String> = combine(
        appPreferences.adminWhatsAppNumber,
        cloudSyncManager.cloudWhatsAppNumber
    ) { localPhone, cloudPhone ->
        if (!cloudPhone.isNullOrBlank()) cloudPhone else localPhone
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val kitchenOpen: StateFlow<Boolean> = combine(
        appPreferences.kitchenOpen,
        cloudSyncManager.cloudKitchenOpen
    ) { localOpen, cloudOpen ->
        cloudOpen ?: localOpen
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailyOfferText: StateFlow<String> = combine(
        appPreferences.dailyOfferText,
        cloudSyncManager.cloudDailyOffer
    ) { localOffer, cloudOffer ->
        cloudOffer ?: localOffer
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val deliveryZones: StateFlow<List<DeliveryZone>> = combine(
        appPreferences.localDeliveryZones,
        cloudSyncManager.cloudDeliveryZones
    ) { localZones, cloudZones ->
        if (!cloudZones.isNullOrEmpty()) cloudZones else localZones
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DeliveryZone.DEFAULT_ZONES)

    val cloudConfig: StateFlow<CloudConfig> = appPreferences.cloudConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CloudConfig())

    val cloudSyncState: StateFlow<CloudSyncState> = cloudSyncManager.syncState
    val cloudAppUsers: StateFlow<List<AppUser>> = cloudSyncManager.cloudAppUsers
    val customerTrackedOrders: StateFlow<List<TrackedCloudOrder>> = cloudSyncManager.cloudCustomerOrders
    val appVersionConfig: StateFlow<AppVersionConfig?> = cloudSyncManager.cloudVersionConfig

    private val _isUpdateDismissed = MutableStateFlow(false)
    val isUpdateDismissed: StateFlow<Boolean> = _isUpdateDismissed.asStateFlow()

    private val _selectedYearMonth = MutableStateFlow(DateUtils.getCurrentYearMonth())
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    private val _editingOrder = MutableStateFlow<Order?>(null)
    val editingOrder: StateFlow<Order?> = _editingOrder.asStateFlow()

    private val _editingDraftItems = MutableStateFlow<List<DraftOrderLineItem>>(emptyList())
    val editingDraftItems: StateFlow<List<DraftOrderLineItem>> = _editingDraftItems.asStateFlow()

    private val _isOrderDialogOpen = MutableStateFlow(false)
    val isOrderDialogOpen: StateFlow<Boolean> = _isOrderDialogOpen.asStateFlow()

    private val _selectedOrderDetails = MutableStateFlow<Order?>(null)
    val selectedOrderDetails: StateFlow<Order?> = _selectedOrderDetails.asStateFlow()

    private val _celebrationMessage = MutableStateFlow<String?>(null)
    val celebrationMessage: StateFlow<String?> = _celebrationMessage.asStateFlow()

    private val _previewBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val previewBitmaps: StateFlow<List<Bitmap>> = _previewBitmaps.asStateFlow()

    private val _selectedPreviewCategoryId = MutableStateFlow<Long?>(null)
    val selectedPreviewCategoryId: StateFlow<Long?> = _selectedPreviewCategoryId.asStateFlow()

    private val _isGeneratingPreview = MutableStateFlow(false)
    val isGeneratingPreview: StateFlow<Boolean> = _isGeneratingPreview.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _customerCart = MutableStateFlow<List<CustomerCartItem>>(emptyList())
    val customerCart: StateFlow<List<CustomerCartItem>> = _customerCart.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = businessRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = businessRepository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productImages: StateFlow<List<ProductImageEntity>> = businessRepository.allProductImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val primaryImagesMap: StateFlow> = productImages
        .combine(products) { imgs, _ ->
            imgs.groupBy { it.productId }
                .mapValues { (_, list) -> list.firstOrNull { it.isPrimary }?.imagePath ?: list.first().imagePath }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val imagesByProductMap: StateFlow>> = productImages
        .combine(products) { imgs, _ -> imgs.groupBy { it.productId } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val menuCategories: StateFlow<List<MenuCategoryEntity>> = businessRepository.menuCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val menuItems: StateFlow<List<MenuItemEntity>> = businessRepository.menuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storefrontCategories: StateFlow<List<MenuCategoryEntity>> = combine(
        menuCategories,
        cloudSyncManager.cloudMenuCategories
    ) { localCats, cloudCats ->
        val source = if (!cloudCats.isNullOrEmpty()) cloudCats else localCats
        source.filter { it.isVisible }
            .distinctBy { it.name.trim() }
            .sortedBy { it.sortOrder }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storefrontItems: StateFlow<List<MenuItemEntity>> = combine(
        menuItems,
        cloudSyncManager.cloudMenuItems
    ) { localItems, cloudItems ->
        val source = if (!cloudItems.isNullOrEmpty()) cloudItems else localItems
        source.filter { it.isVisible }.sortedBy { it.sortOrder }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedMenus: StateFlow<List<GeneratedMenuEntity>> = businessRepository.generatedMenus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = orderRepository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableMonths: StateFlow<List<String>> = orderRepository.distinctMonths
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(DateUtils.getCurrentYearMonth()))

    val ordersForSelectedMonth: StateFlow<List<Order>> = _selectedYearMonth
        .flatMapLatest { ym -> orderRepository.getOrdersByMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLineItems: StateFlow<List<OrderLineItemEntity>> = orderRepository.allLineItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderItemsMap: StateFlow>> = allLineItems
        .combine(ordersForSelectedMonth) { items, _ -> items.groupBy { it.orderId } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val lineItemsForSelectedMonth: StateFlow<List<OrderLineItemEntity>> = _selectedYearMonth
        .flatMapLatest { ym -> orderRepository.getLineItemsForMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expensesForSelectedMonth: StateFlow<List<ExpenseEntity>> = _selectedYearMonth
        .flatMapLatest { ym -> businessRepository.getExpensesByMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val treasuryTransactions: StateFlow<List<TreasuryTransactionEntity>> = orderRepository.allTreasuryTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Smart Treasury & Capital Summary across all business operations
    val treasurySummary: StateFlow<TreasurySummary> = combine(
        allOrders,
        treasuryTransactions,
        expensesForSelectedMonth
    ) { orders, txs, monthExpenses ->
        val activeOrders = orders.filter { it.status != OrderStatus.CANCELLED.name }
        val collectedFromOrders = activeOrders.sumOf { it.effectivePaidAmount }
        val pendingReceivables = activeOrders.sumOf { it.remainingAmount }
        val totalOrdersCost = activeOrders.sumOf { it.costPrice }
        val totalHistoricalProfit = activeOrders.sumOf { it.netProfit }

        val homeLoansIn = txs.filter { it.type == TreasuryTxType.HOME_LOAN_IN.name }.sumOf { it.amount }
        val homeLoansRepaid = txs.filter { it.type == TreasuryTxType.HOME_LOAN_REPAY.name }.sumOf { it.amount }
        val homeLoanDebt = (homeLoansIn - homeLoansRepaid).coerceAtLeast(0.0)

        val capitalInjections = txs.filter { it.type == TreasuryTxType.CAPITAL_INJECTION.name }.sumOf { it.amount }
        val manualMaterialsOut = txs.filter { it.type == TreasuryTxType.MATERIALS_PURCHASE.name }.sumOf { it.amount }
        val profitWithdrawn = txs.filter { it.type == TreasuryTxType.PROFIT_WITHDRAWAL.name }.sumOf { it.amount }
        val extraRevenueIn = txs.filter { it.type == TreasuryTxType.ORDER_REVENUE_IN.name }.sumOf { it.amount }

        val totalExpensesAll = monthExpenses.sumOf { it.amount }

        val cashInDrawer = (
            homeLoansIn - homeLoansRepaid +
                capitalInjections +
                collectedFromOrders +
                extraRevenueIn -
                totalOrdersCost -
                manualMaterialsOut -
                profitWithdrawn -
                totalExpensesAll
            )

        val baseCapital = homeLoanDebt + capitalInjections
        val profitReinvested = (manualMaterialsOut - baseCapital).coerceAtLeast(0.0)
        val revolvingCapital = (baseCapital + profitReinvested - manualMaterialsOut).coerceAtLeast(0.0)
        val availableProfit = (totalHistoricalProfit - profitWithdrawn - profitReinvested - totalExpensesAll).coerceAtLeast(0.0)

        TreasurySummary(
            cashInDrawer = cashInDrawer,
            revolvingCapital = revolvingCapital,
            homeLoanDebt = homeLoanDebt,
            totalHistoricalProfit = totalHistoricalProfit,
            profitReinvestedInMaterials = profitReinvested,
            availableProfitToWithdraw = availableProfit,
            totalCollectedFromOrders = collectedFromOrders,
            pendingCustomerReceivables = pendingReceivables
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TreasurySummary())

    // Aggregated Shopping List for Active / Preparing Orders
    val activeShoppingList: StateFlow<List<ShoppingListItem>> = combine(
        allOrders,
        allLineItems
    ) { orders, items ->
        val activeOrderIds = orders.filter {
            it.status == OrderStatus.PENDING_REVIEW.name ||
                it.status == OrderStatus.NEW_CUSTOMER.name ||
                it.status == OrderStatus.PENDING.name ||
                it.status == OrderStatus.PREPARING.name
        }.map { it.id }.toSet()

        items.filter { it.orderId in activeOrderIds }
            .groupBy { it.productNameSnapshot.trim() }
            .map { (name, list) ->
                ShoppingListItem(
                    productName = name,
                    totalQuantity = list.sumOf { it.quantity },
                    ordersCount = list.map { it.orderId }.distinct().count(),
                    estimatedCost = list.sumOf { it.totalPurchaseCost }
                )
            }
            .sortedByDescending { it.totalQuantity }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredOrders: StateFlow<List<Order>> = combine(
        ordersForSelectedMonth,
        _searchQuery,
        _statusFilter
    ) { orders, query, filter ->
        orders.filter { order ->
            val matchesQuery = if (query.isBlank()) true else {
                order.customerName.contains(query, ignoreCase = true) ||
                    order.itemsDescription.contains(query, ignoreCase = true) ||
                    order.customerPhone.contains(query, ignoreCase = true) ||
                    order.deliveryAddress.contains(query, ignoreCase = true) ||
                    order.deliveryZone.contains(query, ignoreCase = true) ||
                    order.notes.contains(query, ignoreCase = true)
            }
            val matchesStatus = when (filter) {
                null -> true
                "CUSTOMER" -> order.isFromCustomer || order.status == OrderStatus.NEW_CUSTOMER.name
                OrderStatus.PENDING.name -> order.status == OrderStatus.PENDING.name ||
                    order.status == OrderStatus.NEW_CUSTOMER.name ||
                    order.status == OrderStatus.PENDING_REVIEW.name ||
                    order.status == OrderStatus.PREPARING.name ||
                    order.status == OrderStatus.OUT_FOR_DELIVERY.name
                else -> order.status == filter
            }
            matchesQuery && matchesStatus
        }.sortedWith(
            compareBy<Order> { it.status == OrderStatus.DELIVERED.name || it.status == OrderStatus.CANCELLED.name }
                .thenBy { if (it.deliveryDate > 0L) it.deliveryDate else Long.MAX_VALUE }
                .thenByDescending { it.orderDate }
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyStats: StateFlow<MonthlyStats> = combine(
        ordersForSelectedMonth,
        lineItemsForSelectedMonth,
        expensesForSelectedMonth,
        _selectedYearMonth
    ) { orders, lineItems, expenses, ym ->
        calculateStats(orders, lineItems, expenses, ym)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyStats())

    private fun calculateStats(
        orders: List<Order>,
        lineItems: List<OrderLineItemEntity>,
        expenses: List<ExpenseEntity>,
        yearMonth: String
    ): MonthlyStats {
        val activeOrders = orders.filter { it.status != OrderStatus.CANCELLED.name }

        val totalSales = activeOrders.sumOf { it.salePrice }
        val totalPurchaseCost = activeOrders.sumOf { it.costPrice }
        val grossProfitBeforeExpenses = activeOrders.sumOf { it.netProfit }
        val profitMargin = if (totalSales > 0) (grossProfitBeforeExpenses / totalSales) * 100.0 else 0.0
        val totalExpenses = expenses.sumOf { it.amount }

        val uniqueCustomersCount = activeOrders
            .mapNotNull { order ->
                val cleanName = order.customerName.trim().lowercase(Locale.getDefault())
                cleanName.takeIf { it.isNotBlank() }
            }
            .distinct()
            .count()

        val deliveredCount = orders.count { it.status == OrderStatus.DELIVERED.name }
        val pendingCount = orders.count {
            it.status == OrderStatus.PENDING.name ||
                it.status == OrderStatus.NEW_CUSTOMER.name ||
                it.status == OrderStatus.PENDING_REVIEW.name ||
                it.status == OrderStatus.PREPARING.name ||
                it.status == OrderStatus.OUT_FOR_DELIVERY.name
        }
        val cancelledCount = orders.count { it.status == OrderStatus.CANCELLED.name }
        val avgProfit = if (activeOrders.isNotEmpty()) grossProfitBeforeExpenses / activeOrders.size else 0.0

        val cal = Calendar.getInstance()
        val dailyMap = mutableMapOf<Int, Triple<Double, Double, Int>>()
        for (order in activeOrders) {
            cal.timeInMillis = order.orderDate
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val curr = dailyMap[day] ?: Triple(0.0, 0.0, 0)
            dailyMap[day] = Triple(curr.first + order.salePrice, curr.second + order.netProfit, curr.third + 1)
        }
        val dailyPoints = dailyMap.map { (day, values) ->
            DailyStatPoint(day, values.first, values.second, values.third)
        }.sortedBy { it.dayNumber }

        val topProducts = lineItems
            .groupBy { it.productNameSnapshot }
            .map { (name, items) ->
                TopProductStat(
                    productName = name,
                    totalQuantity = items.sumOf { it.quantity },
                    totalSales = items.sumOf { it.totalSellingPrice },
                    totalGrossProfit = items.sumOf { it.itemGrossProfit },
                    orderCount = items.map { it.orderId }.distinct().count()
                )
            }
            .sortedByDescending { it.totalQuantity }

        return MonthlyStats(
            yearMonth = yearMonth,
            totalSales = totalSales,
            totalPurchaseCost = totalPurchaseCost,
            grossProfitBeforeExpenses = grossProfitBeforeExpenses,
            profitMarginPercent = profitMargin,
            totalExpenses = totalExpenses,
            totalOrdersCount = orders.size,
            deliveredCount = deliveredCount,
            pendingCount = pendingCount,
            cancelledCount = cancelledCount,
            uniqueCustomersCount = uniqueCustomersCount,
            averageOrderProfit = avgProfit,
            dailyPoints = dailyPoints,
            topProducts = topProducts
        )
    }

    // Customer Lookup Autofill by Phone
    fun findPreviousCustomerByPhone(phone: String): Order? {
        val clean = phone.filter { it.isDigit() }
        if (clean.length < 6) return null
        return allOrders.value.firstOrNull {
            it.customerPhone.filter { ch -> ch.isDigit() }.endsWith(clean.takeLast(9))
        }
    }

    // Treasury & Capital Actions
    fun recordTreasuryTransaction(
        type: TreasuryTxType,
        amount: Double,
        title: String,
        notes: String = ""
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val cid = UUID.randomUUID().toString()
            val cleanTitle = title.trim().ifBlank { type.titleAr }
            val tx = TreasuryTransactionEntity(
                cloudId = cid,
                type = type.name,
                amount = amount,
                title = cleanTitle,
                notes = notes.trim(),
                createdAt = now,
                yearMonth = DateUtils.getYearMonthFromTimestamp(now)
            )
            orderRepository.insertTreasuryTransaction(tx)
            cloudSyncManager.pushTreasuryTransactionToCloud(tx)
            withContext(Dispatchers.Main) {
                _uiMessage.value = "تم تسجيل الحركة في الخزنة بنجاح (${DateUtils.formatCurrency(amount)})"
            }
        }
    }

    fun deleteTreasuryTransaction(tx: TreasuryTransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            orderRepository.deleteTreasuryTransaction(tx)
            if (tx.cloudId.isNotBlank()) {
                cloudSyncManager.deleteTreasuryTransactionFromCloud(tx.cloudId)
            }
            withContext(Dispatchers.Main) {
                _uiMessage.value = "تم حذف الحركة من سجل الخزنة"
            }
        }
    }

    // Delete Test / App User Account
    fun deleteAppUser(user: AppUser) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = cloudSyncManager.deleteAppUserFromCloud(user.userId)
            withContext(Dispatchers.Main) {
                _uiMessage.value = if (res.isSuccess) {
                    "تم حذف حساب (${user.username}) نهائياً ✅"
                } else {
                    "تعذر حذف الحساب، تأكد من الاتصال بالإنترنت"
                }
            }
        }
    }

    fun completeSplash() {
        if (_appStage.value != AppStage.SPLASH) return
        viewModelScope.launch {
            val savedProfile = appPreferences.customerProfile.first()
            _appStage.value = if (savedProfile.isRegistered) {
                AppStage.CUSTOMER_STOREFRONT
            } else {
                AppStage.ONBOARDING
            }
        }
    }

    fun completeOnboarding(username: String, password: String) {
        val cleanName = username.trim()
        if (cleanName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val existing = appPreferences.customerProfile.first()
            val uid = existing.userId.ifBlank { UUID.randomUUID().toString() }
            val profile = CustomerProfile(
                userId = uid,
                username = cleanName,
                password = password,
                phone = existing.phone,
                address = existing.address
            )
            appPreferences.saveCustomerProfile(profile)
            cloudSyncManager.upsertUserProfile(profile)
            cloudSyncManager.updateTrackedCustomerUserId(uid)
            withContext(Dispatchers.Main) {
                _appStage.value = AppStage.CUSTOMER_STOREFRONT
            }
        }
    }

    fun enterAdminPanel(openOrdersTab: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setAdminDevice(true)
        }
        if (openOrdersTab) {
            _selectedTab.value = AppTab.ORDERS
        }
        _appStage.value = AppStage.ADMIN_PANEL
    }

    fun openAdminOrdersFromNotification() {
        viewModelScope.launch {
            if (appPreferences.isAdminDevice.first()) {
                _selectedTab.value = AppTab.ORDERS
                _appStage.value = AppStage.ADMIN_PANEL
            }
        }
    }

    fun returnToCustomerStorefront() {
        viewModelScope.launch {
            val savedProfile = appPreferences.customerProfile.first()
            _appStage.value = if (savedProfile.isRegistered) {
                AppStage.CUSTOMER_STOREFRONT
            } else {
                AppStage.ONBOARDING
            }
        }
    }

    fun setUserPresence(isOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = appPreferences.customerProfile.first()
            if (profile.isRegistered) {
                cloudSyncManager.updateUserPresence(profile.userId, isOnline)
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setDarkMode(!isDarkMode.value)
        }
    }

    fun updateAdminPin(newPin: String) {
        val trimmed = newPin.trim()
        if (trimmed.length < 4) {
            _uiMessage.value = "الرقم السري يجب أن يكون 4 أرقام أو أكثر"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setAdminPin(trimmed)
            withContext(Dispatchers.Main) {
                _uiMessage.value = "تم تحديث الرقم السري للإدارة بنجاح"
            }
        }
    }

    fun updateAdminWhatsAppNumber(phoneNumber: String) {
        val trimmed = phoneNumber.trim()
        val sanitized = WhatsAppUtils.sanitizeWhatsAppNumber(trimmed)
        if (trimmed.isNotBlank() && sanitized.length < 8) {
            _uiMessage.value = "يرجى إدخال رقم واتساب صحيح (مثال: 2010xxxxxxxxx)"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setAdminWhatsAppNumber(sanitized)
            if (cloudConfig.value.isConfigured) {
                cloudSyncManager.publishWhatsAppNumberToCloud(sanitized)
            }
            withContext(Dispatchers.Main) {
                _uiMessage.value = if (sanitized.isBlank()) {
                    "تم مسح رقم واتساب استقبال الطلبات"
                } else {
                    "تم حفظ رقم واتساب الإدارة ($sanitized) بنجاح"
                }
            }
        }
    }

    fun toggleKitchenOpen(isOpen: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setKitchenOpen(isOpen)
            cloudSyncManager.publishKitchenOpenToCloud(isOpen)
            withContext(Dispatchers.Main) {
                _uiMessage.value = if (isOpen) {
                    "تم فتح المطبخ لاستقبال طلبات العملاء"
                } else {
                    "تم تحويل حالة المطبخ إلى (مشغول حالياً)"
                }
            }
        }
    }

    fun updateDailyOffer(offerText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.setDailyOfferText(offerText)
            cloudSyncManager.publishDailyOfferToCloud(offerText)
            withContext(Dispatchers.Main) {
                _uiMessage.value = if (offerText.isBlank()) {
                    "تم إخفاء بانر عرض اليوم"
                } else {
                    "تم نشر العرض وإرسال إشعار فوري لجميع المستخدمين 🔥"
                }
            }
        }
    }

    fun saveDeliveryZones(zones: List<DeliveryZone>) {
        val cleanZones = zones.filter { it.name.isNotBlank() }
        if (cleanZones.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.saveDeliveryZones(cleanZones)
            cloudSyncManager.publishDeliveryZonesToCloud(cleanZones)
            withContext(Dispatchers.Main) {
                _uiMessage.value = "تم حفظ مناطق التوصيل وأسعار الشحن بنجاح"
            }
        }
    }

    fun dismissUpdateDialog() {
        _isUpdateDismissed.value = true
    }

    fun checkForUpdatesManually() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!cloudSyncState.value.isCloudConnected) {
                cloudSyncManager.configureAndStart(cloudConfig.value, enableAdminListeners = true)
            }
            val result = cloudSyncManager.fetchVersionConfigOnce()
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { remote ->
                        if (remote != null && remote.isUpdateAvailable(BuildConfig.VERSION_CODE)) {
                            _isUpdateDismissed.value = false
                            _uiMessage.value = "يتوفر تحديث جديد (${remote.latestVersionName}) ✨"
                        } else {
                            _uiMessage.value = "أنت على أحدث إصدار حالياً (${BuildConfig.VERSION_NAME}) ✅"
                        }
                    },
                    onFailure = {
                        _uiMessage.value = "تعذر التحقق من التحديثات حالياً، تأكد من اتصال الإنترنت"
                    }
                )
            }
        }
    }

    fun publishAppVersionConfig(
        latestVersionCode: Int,
        latestVersionName: String,
        downloadUrl: String,
        releaseNotes: String,
        isMandatory: Boolean
    ) {
        val cleanName = latestVersionName.trim()
        val cleanUrl = downloadUrl.trim()
        if (latestVersionCode < 1 || cleanName.isBlank() || cleanUrl.isBlank()) {
            _uiMessage.value = "يرجى إدخال كود الإصدار ورقمه ورابط التحميل المباشر"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val res = cloudSyncManager.publishAppVersionConfig(
                AppVersionConfig(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = cleanName,
                    downloadUrl = cleanUrl,
                    releaseNotes = releaseNotes.trim(),
                    isMandatory = isMandatory
                )
            )
            withContext(Dispatchers.Main) {
                _uiMessage.value = if (res.isSuccess) {
                    "تم نشر بيانات التحديث الجديد للعملاء بنجاح ✨"
                } else {
                    res.exceptionOrNull()?.localizedMessage ?: "تعذر حفظ بيانات التحديث في السحابة"
                }
            }
        }
    }

    fun syncPublishedMenuToCloudNow() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!cloudSyncState.value.isCloudConnected) {
                cloudSyncManager.configureAndStart(cloudConfig.value, enableAdminListeners = true)
            }
            val cats = menuCategories.value.ifEmpty { businessRepository.menuCategories.first() }
            val items = menuItems.value.ifEmpty { businessRepository.menuItems.first() }
            val res = cloudSyncManager.publishMenuToCloud(cats, items)
            val phone = adminWhatsAppNumber.value
            if (phone.isNotBlank()) {
                cloudSyncManager.publishWhatsAppNumberToCloud(phone)
            }
            cloudSyncManager.publishKitchenOpenToCloud(kitchenOpen.value)
            cloudSyncManager.publishDailyOfferToCloud(dailyOfferText.value)
            cloudSyncManager.publishDeliveryZonesToCloud(deliveryZones.value)

            withContext(Dispatchers.Main) {
                _uiMessage.value = if (res.isSuccess) {
                    "تمت مزامنة المنيو والصور والإعدادات مع السحابة بنجاح"
                } else {
                    res.exceptionOrNull()?.localizedMessage ?: "تعذر مزامنة البيانات مع السحابة"
                }
            }
        }
    }

    private fun autoSyncMenuIfConfigured() {
        if (!cloudConfig.value.isConfigured) return
        viewModelScope.launch(Dispatchers.IO) {
            val cats = businessRepository.menuCategories.first
