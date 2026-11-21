package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.model.TopProductStat
import com.example.model.TrackedCloudOrder
import com.example.BuildConfig
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

        // Immediately initialize Firebase with hardcoded akla-helwa credentials on launch
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

    // Navigation Stage (Splash -> Onboarding (if new) -> Customer Storefront / Admin Panel)
    private val _appStage = MutableStateFlow(AppStage.SPLASH)
    val appStage: StateFlow<AppStage> = _appStage.asStateFlow()

    private val _selectedTab = MutableStateFlow(AppTab.DASHBOARD)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // Persisted Preferences (Dark Mode defaults to true)
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

    // Orders & Search
    private val _selectedYearMonth = MutableStateFlow(DateUtils.getCurrentYearMonth())
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    // Order form dialog state
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

    // Menu preview & export state
    private val _previewBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val previewBitmaps: StateFlow<List<Bitmap>> = _previewBitmaps.asStateFlow()

    private val _selectedPreviewCategoryId = MutableStateFlow<Long?>(null)
    val selectedPreviewCategoryId: StateFlow<Long?> = _selectedPreviewCategoryId.asStateFlow()

    private val _isGeneratingPreview = MutableStateFlow(false)
    val isGeneratingPreview: StateFlow<Boolean> = _isGeneratingPreview.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Customer Storefront Cart
    private val _customerCart = MutableStateFlow<List<CustomerCartItem>>(emptyList())
    val customerCart: StateFlow<List<CustomerCartItem>> = _customerCart.asStateFlow()

    // Catalog Flows
    val categories: StateFlow<List<CategoryEntity>> = businessRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = businessRepository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productImages: StateFlow<List<ProductImageEntity>> = businessRepository.allProductImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val primaryImagesMap: StateFlow<Map<Long, String>> = productImages
        .combine(products) { imgs, _ ->
            imgs.groupBy { it.productId }
                .mapValues { (_, list) -> list.firstOrNull { it.isPrimary }?.imagePath ?: list.first().imagePath }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val imagesByProductMap: StateFlow<Map<Long, List<ProductImageEntity>>> = productImages
        .combine(products) { imgs, _ -> imgs.groupBy { it.productId } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Unified Published Menu Flows (Admin local DB + Firestore sync)
    val menuCategories: StateFlow<List<MenuCategoryEntity>> = businessRepository.menuCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val menuItems: StateFlow<List<MenuItemEntity>> = businessRepository.menuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Storefront Menu Flows (uses real-time Firestore menu when synced, falls back cleanly to local Room menu)
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

    // Orders & Expenses Flows
    val availableMonths: StateFlow<List<String>> = orderRepository.distinctMonths
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(DateUtils.getCurrentYearMonth()))

    val ordersForSelectedMonth: StateFlow<List<Order>> = _selectedYearMonth
        .flatMapLatest { ym -> orderRepository.getOrdersByMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLineItems: StateFlow<List<OrderLineItemEntity>> = orderRepository.allLineItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderItemsMap: StateFlow<Map<Long, List<OrderLineItemEntity>>> = allLineItems
        .combine(ordersForSelectedMonth) { items, _ -> items.groupBy { it.orderId } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val lineItemsForSelectedMonth: StateFlow<List<OrderLineItemEntity>> = _selectedYearMonth
        .flatMapLatest { ym -> orderRepository.getLineItemsForMonth(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expensesForSelectedMonth: StateFlow<List<ExpenseEntity>> = _selectedYearMonth
        .flatMapLatest { ym -> businessRepository.getExpensesByMonth(ym) }
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
        }
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

    // Customer-First App Stage & Onboarding
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

    // User Presence Heartbeat (Firestore app_users/{userId})
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
                _uiMessage.value = if (offerText.isBlank()) "تم إخفاء بانر عرض اليوم" else "تم نشر عرض اليوم للعملاء بنجاح"
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
            val cats = businessRepository.menuCategories.first()
            val items = businessRepository.menuItems.first()
            cloudSyncManager.publishMenuToCloud(cats, items)
        }
    }

    fun dismissCelebration() {
        _celebrationMessage.value = null
    }

    // Customer Favorites & Reorder
    fun toggleFavorite(menuItemId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferences.toggleFavoriteMenuItem(menuItemId)
        }
    }

    fun reorderLastOrder() {
        val lastItems = lastReorderItems.value
        if (lastItems.isEmpty()) {
            _uiMessage.value = "لا يوجد طلب سابق لإعادة طلبه"
            return
        }
        val availableMap = storefrontItems.value.associateBy { it.id }
        val imagesMap = primaryImagesMap.value
        val rebuilt = lastItems.mapNotNull { (id, qty) ->
            val menu = availableMap[id] ?: return@mapNotNull null
            val img = menu.photoList().firstOrNull() ?: menu.productId?.let { imagesMap[it] }
            CustomerCartItem(menuItem = menu, imagePath = img, quantity = qty)
        }
        if (rebuilt.isEmpty()) {
            _uiMessage.value = "أصناف الطلب السابق غير متوفرة حالياً"
        } else {
            _customerCart.value = rebuilt
            _uiMessage.value = "تمت إضافة أصناف طلبك السابق للسلة بنجاح 🍲"
        }
    }

    fun reorderFromTrackedOrder(order: TrackedCloudOrder) {
        val availableById = storefrontItems.value.associateBy { it.id }
        val availableByName = storefrontItems.value.associateBy { it.displayName.trim() }
        val imagesMap = primaryImagesMap.value
        val rebuilt = order.lineItems.mapNotNull { li ->
            val menu = availableById[li.menuItemId] ?: availableByName[li.productName.trim()] ?: return@mapNotNull null
            val img = menu.photoList().firstOrNull() ?: menu.productId?.let { imagesMap[it] }
            CustomerCartItem(menuItem = menu, imagePath = img, quantity = li.quantity)
        }
        if (rebuilt.isNotEmpty()) {
            _customerCart.value = rebuilt
            _uiMessage.value = "تم تجهيز نفس أصناف الطلب في السلة!"
        }
    }

    // Customer Storefront Cart Actions
    fun addToCustomerCart(menuItem: MenuItemEntity, imagePath: String?, quantityToAdd: Double = 1.0) {
        val current = _customerCart.value.toMutableList()
        val idx = current.indexOfFirst { it.menuItem.id == menuItem.id }
        if (idx >= 0) {
            val item = current[idx]
            val delta = if (quantityToAdd == 1.0) 0.5 else quantityToAdd
            current[idx] = item.copy(quantity = item.quantity + delta)
        } else {
            current.add(CustomerCartItem(menuItem = menuItem, imagePath = imagePath, quantity = quantityToAdd.coerceAtLeast(0.5)))
        }
        _customerCart.value = current
    }

    fun updateCustomerCartQuantity(menuItemId: Long, delta: Double) {
        val current = _customerCart.value.toMutableList()
        val idx = current.indexOfFirst { it.menuItem.id == menuItemId }
        if (idx >= 0) {
            val nextQty = current[idx].quantity + delta
            if (nextQty <= 0.0) {
                current.removeAt(idx)
            } else {
                current[idx] = current[idx].copy(quantity = nextQty)
            }
            _customerCart.value = current
        }
    }

    fun clearCustomerCart() {
        _customerCart.value = emptyList()
    }

    fun submitCustomerOrder(
        customerName: String,
        customerPhone: String,
        deliveryZone: DeliveryZone,
        deliveryAddress: String,
        notes: String,
        onOrderReadyForWhatsApp: ((whatsAppMessage: String, adminPhone: String) -> WhatsAppLaunchResult)? = null
    ) {
        val cart = _customerCart.value
        if (cart.isEmpty() || customerName.isBlank() || customerPhone.isBlank() || deliveryAddress.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val ym = DateUtils.getYearMonthFromTimestamp(now)
            val cloudId = UUID.randomUUID().toString()
            val profile = appPreferences.customerProfile.first()
            val userId = profile.userId

            appPreferences.updateCustomerContactInfo(customerPhone, deliveryAddress)
            appPreferences.saveLastReorderItems(cart.map { it.menuItem.id to it.quantity })

            val summaryDesc = cart.joinToString("، ") { item ->
                val q = WhatsAppUtils.formatQuantity(item.quantity)
                "$q × ${item.menuItem.displayName}"
            }

            val prodMap = businessRepository.getAllProductsOnce().associateBy { it.id }
            val lineEntities = cart.map { ci ->
                val unitSell = if (ci.menuItem.isVariablePrice && ci.menuItem.publishedPrice <= 0.0) 0.0 else ci.menuItem.publishedPrice
                val unitPurchase = ci.menuItem.productId?.let { prodMap[it]?.purchaseCost } ?: 0.0
                val totalSell = ci.quantity * unitSell
                val totalCost = ci.quantity * unitPurchase
                OrderLineItemEntity(
                    orderId = 0L,
                    productId = ci.menuItem.productId,
                    productNameSnapshot = ci.menuItem.displayName,
                    productImagePathSnapshot = ci.imagePath,
                    quantity = ci.quantity,
                    unitPurchaseCost = unitPurchase,
                    unitSellingPrice = unitSell,
                    totalPurchaseCost = totalCost,
                    totalSellingPrice = totalSell,
                    itemGrossProfit = totalSell - totalCost
                )
            }

            val totalItemsSelling = lineEntities.sumOf { it.totalSellingPrice }
            val totalCost = lineEntities.sumOf { it.totalPurchaseCost }
            val order = Order(
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                itemsDescription = summaryDesc,
                salePrice = totalItemsSelling,
                costPrice = totalCost,
                netProfit = totalItemsSelling - totalCost,
                orderDate = now,
                yearMonth = ym,
                status = OrderStatus.PENDING_REVIEW.name,
                paymentStatus = PaymentStatus.UNPAID.name,
                notes = notes.trim(),
                deliveryAddress = deliveryAddress.trim(),
                orderSource = OrderSource.CUSTOMER.name,
                cloudId = cloudId,
                userId = userId,
                deliveryZone = deliveryZone.name,
                shippingFee = deliveryZone.fee,
                isRated = false
            )

            orderRepository.saveOrderWithItems(order, lineEntities)
            cloudSyncManager.pushCustomerOrderToCloud(
                cloudId = cloudId,
                userId = userId,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                deliveryZone = deliveryZone.name,
                shippingFee = deliveryZone.fee,
                deliveryAddress = deliveryAddress.trim(),
                notes = notes.trim(),
                cartItems = cart,
                totalSalePrice = totalItemsSelling,
                orderDate = now,
                yearMonth = ym
            )
            if (userId.isNotBlank()) {
                cloudSyncManager.incrementUserOrdersCount(userId)
            }

            val whatsAppMessage = WhatsAppUtils.buildCustomerOrderWhatsAppMessage(
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = deliveryAddress,
                deliveryZone = deliveryZone.name,
                shippingFee = deliveryZone.fee,
                notes = notes,
                cartItems = cart,
                totalAmount = totalItemsSelling
            )
            val targetAdminPhone = adminWhatsAppNumber.value

            withContext(Dispatchers.Main) {
                _customerCart.value = emptyList()
                val launchResult = onOrderReadyForWhatsApp?.invoke(whatsAppMessage, targetAdminPhone)
                    ?: WhatsAppLaunchResult.NO_NUMBER_CONFIGURED
                _celebrationMessage.value = when (launchResult) {
                    WhatsAppLaunchResult.LAUNCHED_DIRECT ->
                        "تم تسجيل طلبكِ وإرسال تفاصيله لواتساب أ/ نهى ماهر! يمكنكِ متابعة حالة التجهيز من (طلباتي) ❤️"
                    WhatsAppLaunchResult.WHATSAPP_NOT_INSTALLED_FALLBACK_SHARE ->
                        "تم تسجيل طلبكِ بنجاح! يمكنكِ إرسال التفاصيل عبر تطبيق المراسلة المختار ❤️"
                    WhatsAppLaunchResult.NO_NUMBER_CONFIGURED ->
                        "تم تسجيل طلبكِ بنجاح في مطبخ أكلة حلوة! ويمكنكِ متابعة حالته لحظياً من (طلباتي) ❤️"
                }
            }
        }
    }

    fun submitCustomerOrderRating(trackedOrder: TrackedCloudOrder, stars: Int) {
        val clamped = stars.coerceIn(1, 5)
        viewModelScope.launch(Dispatchers.IO) {
            val allLocalMenu = businessRepository.getAllMenuItemsOnce()
            val byId = allLocalMenu.associateBy { it.id }
            val byName = allLocalMenu.associateBy { it.displayName.trim() }
            val updatedItems = mutableListOf<MenuItemEntity>()

            trackedOrder.lineItems.forEach { li ->
                val target = byId[li.menuItemId] ?: byName[li.productName.trim()]
                if (target != null) {
                    val prevCount = target.ratingCount
                    val newCount = prevCount + 1
                    val newAvg = if (prevCount <= 0) {
                        clamped.toDouble()
                    } else {
                        ((target.ratingAvg * prevCount) + clamped) / newCount
                    }
                    val updated = target.copy(
                        ratingAvg = (Math.round(newAvg * 10.0) / 10.0),
                        ratingCount = newCount
                    )
                    businessRepository.updateMenuItem(updated)
                    updatedItems.add(updated)
                }
            }

            if (trackedOrder.cloudId.isNotBlank()) {
                orderRepository.getOrderByCloudId(trackedOrder.cloudId)?.let { localOrder ->
                    orderRepository.updateOrder(localOrder.copy(isRated = true))
                }
            }

            cloudSyncManager.submitOrderRatingsToCloud(trackedOrder.cloudId, updatedItems)
            withContext(Dispatchers.Main) {
                _uiMessage.value = "شكراً لتقييمكِ! تم إرسال التقييم بنجاح ⭐"
            }
        }
    }

    // Navigation & Filters
    fun setTab(tab: AppTab) { _selectedTab.value = tab }
    fun selectMonth(yearMonth: String) { _selectedYearMonth.value = yearMonth }
    fun previousMonth() { _selectedYearMonth.value = DateUtils.getPreviousMonth(_selectedYearMonth.value) }
    fun nextMonth() { _selectedYearMonth.value = DateUtils.getNextMonth(_selectedYearMonth.value) }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setStatusFilter(status: String?) { _statusFilter.value = status }
    fun clearUiMessage() { _uiMessage.value = null }

    // Order Dialog & Multi-Product Line Items
    fun openAddOrderDialog(orderToEdit: Order? = null) {
        viewModelScope.launch {
            _editingOrder.value = orderToEdit
            if (orderToEdit != null && orderToEdit.id > 0L) {
                val existingItems = withContext(Dispatchers.IO) {
                    orderRepository.getLineItemsForOrder(orderToEdit.id)
                }
                _editingDraftItems.value = if (existingItems.isNotEmpty()) {
                    existingItems.map { item ->
                        DraftOrderLineItem(
                            productId = item.productId,
                            productNameSnapshot = item.productNameSnapshot,
                            productImagePathSnapshot = item.productImagePathSnapshot,
                            unitLabel = "كيلو",
                            quantity = item.quantity,
                            unitPurchaseCost = item.unitPurchaseCost,
                            unitSellingPrice = item.unitSellingPrice
                        )
                    }
                } else {
                    listOf(
                        DraftOrderLineItem(
                            productId = null,
                            productNameSnapshot = orderToEdit.itemsDescription.ifBlank { "أوردر" },
                            quantity = 1.0,
                            unitPurchaseCost = orderToEdit.costPrice,
                            unitSellingPrice = orderToEdit.salePrice
                        )
                    )
                }
            } else {
                _editingDraftItems.value = emptyList()
            }
            _isOrderDialogOpen.value = true
        }
    }

    fun closeOrderDialog() {
        _editingOrder.value = null
        _editingDraftItems.value = emptyList()
        _isOrderDialogOpen.value = false
    }

    fun showOrderDetails(order: Order?) {
        _selectedOrderDetails.value = order
    }

    fun saveMultiProductOrder(
        id: Long = 0L,
        customerName: String,
        customerPhone: String,
        lineItems: List<DraftOrderLineItem>,
        orderDate: Long,
        status: String,
        paymentStatus: String,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingOrder = if (id > 0L) _editingOrder.value else null
            val totalPurchase = lineItems.sumOf { it.totalPurchaseCost }
            val totalSelling = lineItems.sumOf { it.totalSellingPrice }
            val grossProfit = totalSelling - totalPurchase
            val summaryDesc = lineItems.joinToString("، ") { item ->
                val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
                "$qtyStr × ${item.productNameSnapshot}"
            }
            val ym = DateUtils.getYearMonthFromTimestamp(orderDate)

            val order = Order(
                id = id,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                itemsDescription = summaryDesc,
                salePrice = totalSelling,
                costPrice = totalPurchase,
                netProfit = grossProfit,
                orderDate = orderDate,
                yearMonth = ym,
                status = status,
                paymentStatus = paymentStatus,
                notes = notes.trim(),
                deliveryAddress = existingOrder?.deliveryAddress.orEmpty(),
                orderSource = existingOrder?.orderSource ?: OrderSource.ADMIN.name,
                cloudId = existingOrder?.cloudId.orEmpty(),
                userId = existingOrder?.userId.orEmpty(),
                deliveryZone = existingOrder?.deliveryZone.orEmpty(),
                shippingFee = existingOrder?.shippingFee ?: 0.0,
                isRated = existingOrder?.isRated ?: false
            )

            val entities = lineItems.map { draft ->
                OrderLineItemEntity(
                    orderId = id,
                    productId = draft.productId,
                    productNameSnapshot = draft.productNameSnapshot,
                    productImagePathSnapshot = draft.productImagePathSnapshot,
                    quantity = draft.quantity,
                    unitPurchaseCost = draft.unitPurchaseCost,
                    unitSellingPrice = draft.unitSellingPrice,
                    totalPurchaseCost = draft.totalPurchaseCost,
                    totalSellingPrice = draft.totalSellingPrice,
                    itemGrossProfit = draft.itemGrossProfit
                )
            }

            val savedId = orderRepository.saveOrderWithItems(order, entities)
            if (order.cloudId.isNotBlank()) {
                cloudSyncManager.updateOrderStatusInCloud(order.cloudId, status)
            }
            withContext(Dispatchers.Main) {
                if (_selectedOrderDetails.value?.id == id && id > 0L) {
                    _selectedOrderDetails.value = order.copy(id = savedId)
                }
                closeOrderDialog()
                _celebrationMessage.value = if (id == 0L) "تم تسجيل الأوردر وحساب الأرباح بنجاح!" else "تم تحديث بيانات الأوردر بنجاح!"
            }
        }
    }
fun deleteOrder(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            orderRepository.deleteOrder(order)
            if (order.cloudId.isNotBlank()) {
                cloudSyncManager.deleteOrderFromCloud(order.cloudId)
            }
            withContext(Dispatchers.Main) {
                if (_selectedOrderDetails.value?.id == order.id) {
                    _selectedOrderDetails.value = null
                }
                _uiMessage.value = "تم حذف الأوردر نهائياً من التطبيق والسحابة"
            }
        }
    }

    fun toggleOrderStatus(order: Order) {
        val next = OrderStatus.nextAdminStatus(order.status)
        updateOrderStatus(order, next.name)
    }

    fun updateOrderStatus(order: Order, nextStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = order.copy(status = nextStatus)
            orderRepository.updateOrder(updated)
            if (order.cloudId.isNotBlank()) {
                cloudSyncManager.updateOrderStatusInCloud(order.cloudId, nextStatus)
            }
            withContext(Dispatchers.Main) {
                if (_selectedOrderDetails.value?.id == order.id) {
                    _selectedOrderDetails.value = updated
                }
            }
        }
    }

    fun togglePaymentStatus(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            val nextPayment = if (order.paymentStatus == PaymentStatus.PAID.name) {
                PaymentStatus.UNPAID.name
            } else {
                PaymentStatus.PAID.name
            }
            val updated = order.copy(paymentStatus = nextPayment)
            orderRepository.updateOrder(updated)
            withContext(Dispatchers.Main) {
                if (_selectedOrderDetails.value?.id == order.id) {
                    _selectedOrderDetails.value = updated
                }
            }
        }
    }

    // Unified Catalog & Menu CRUD (synced cleanly to Firestore `menu_items`)
    fun saveCategory(id: Long = 0L, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val currentCount = categories.value.size
            if (id == 0L) {
                businessRepository.insertCategory(CategoryEntity(name = name.trim(), sortOrder = currentCount))
            } else {
                val existing = categories.value.find { it.id == id }
                if (existing != null) {
                    businessRepository.updateCategory(existing.copy(name = name.trim()))
                }
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val prodIds = products.value.filter { it.categoryId == category.id }.map { it.id }
            productImages.value.filter { it.productId in prodIds }.forEach {
                ImageStorageUtils.deleteStoredFile(it.imagePath)
            }
            businessRepository.deleteCategory(category)
        }
    }

    fun saveProduct(
        id: Long = 0L,
        name: String,
        categoryId: Long,
        unit: String,
        description: String,
        purchaseCost: Double,
        defaultSellingPrice: Double,
        isActive: Boolean = true
    ) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val cleanName = name.trim()
            val cleanUnit = unit.ifBlank { "كيلو" }.trim()
            val cleanDesc = description.trim()
            if (id == 0L) {
                val sortOrder = products.value.count { it.categoryId == categoryId }
                val newProdId = businessRepository.insertProduct(
                    ProductEntity(
                        name = cleanName,
                        categoryId = categoryId,
                        unit = cleanUnit,
                        description = cleanDesc,
                        purchaseCost = purchaseCost,
                        defaultSellingPrice = defaultSellingPrice,
                        isActive = isActive,
                        sortOrder = sortOrder
                    )
                )
                // Unified Menu & Items: automatically mirror to menu_items + Firestore
                val catName = categories.value.find { it.id == categoryId }?.name
                val targetMenuCatId = menuCategories.value.find { it.name == catName }?.id
                    ?: menuCategories.value.firstOrNull()?.id
                if (targetMenuCatId != null && targetMenuCatId > 0L) {
                    businessRepository.insertMenuItem(
                        MenuItemEntity(
                            menuCategoryId = targetMenuCatId,
                            productId = newProdId,
                            displayName = cleanName,
                            unitLabel = cleanUnit,
                            description = cleanDesc,
                            publishedPrice = defaultSellingPrice,
                            isVariablePrice = defaultSellingPrice <= 0.0,
                            sortOrder = sortOrder,
                            isVisible = isActive
                        )
                    )
                    autoSyncMenuIfConfigured()
                }
            } else {
                val existing = products.value.find { it.id == id }
                if (existing != null) {
                    businessRepository.updateProduct(
                        existing.copy(
                            name = cleanName,
                            categoryId = categoryId,
                            unit = cleanUnit,
                            description = cleanDesc,
                            purchaseCost = purchaseCost,
                            defaultSellingPrice = defaultSellingPrice,
                            isActive = isActive
                        )
                    )
                    businessRepository.getMenuItemByProductIdOnce(existing.id)?.let { linkedMenu ->
                        businessRepository.updateMenuItem(
                            linkedMenu.copy(
                                displayName = cleanName,
                                unitLabel = cleanUnit,
                                description = cleanDesc,
                                publishedPrice = defaultSellingPrice,
                                isVariablePrice = defaultSellingPrice <= 0.0,
                                isVisible = isActive
                            )
                        )
                        autoSyncMenuIfConfigured()
                    }
                }
            }
        }
    }

    fun toggleProductActive(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val nextActive = !product.isActive
            businessRepository.updateProduct(product.copy(isActive = nextActive))
            businessRepository.getMenuItemByProductIdOnce(product.id)?.let { linked ->
                businessRepository.updateMenuItem(linked.copy(isVisible = nextActive))
                autoSyncMenuIfConfigured()
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            productImages.value.filter { it.productId == product.id }.forEach {
                ImageStorageUtils.deleteStoredFile(it.imagePath)
            }
            businessRepository.getMenuItemByProductIdOnce(product.id)?.let { linked ->
                businessRepository.deleteMenuItem(linked)
                autoSyncMenuIfConfigured()
            }
            businessRepository.deleteProduct(product)
        }
    }

    // Product Images Gallery (Local Catalog + automatic Base64 sync to linked Menu Item if < 3)
    fun addProductImagesFromUris(productId: Long, uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val ctx = getApplication<Application>()
            val newBase64Photos = mutableListOf<String>()
            uris.forEach { uri ->
                val savedPath = ImageStorageUtils.copyProductImageToInternalStorage(ctx, uri, productId)
                if (savedPath != null) {
                    businessRepository.insertProductImage(
                        ProductImageEntity(productId = productId, imagePath = savedPath)
                    )
                }
                val encodedDataUri = ImageUtils.compressUriToBase64DataUri(ctx, uri)
                if (!encodedDataUri.isNullOrBlank()) {
                    newBase64Photos.add(encodedDataUri)
                }
            }
            if (newBase64Photos.isNotEmpty()) {
                businessRepository.getMenuItemByProductIdOnce(productId)?.let { linkedMenu ->
                    val existing = linkedMenu.photoList().toMutableList()
                    val slots = (3 - existing.size).coerceAtLeast(0)
                    if (slots > 0) {
                        existing.addAll(newBase64Photos.take(slots))
                        businessRepository.updateMenuItem(
                            linkedMenu.copy(photosJson = Base64ImageUtils.encodePhotosJson(existing))
                        )
                        autoSyncMenuIfConfigured()
                    }
                }
            }
        }
    }

    fun setPrimaryProductImage(productId: Long, imageId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            businessRepository.setPrimaryImage(productId, imageId)
        }
    }

    fun deleteProductImage(image: ProductImageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            ImageStorageUtils.deleteStoredFile(image.imagePath)
            businessRepository.deleteProductImage(image)
        }
    }

    // Unified Menu Multi-Photo Base64 Upload (up to 3 compressed <=640px JPEG 72% Data URIs synced to Firestore)
    fun addMenuItemPhotosFromUris(menuItem: MenuItemEntity, uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val ctx = getApplication<Application>()
            val latest = businessRepository.getMenuItemByIdOnce(menuItem.id) ?: menuItem
            val currentPhotos = latest.photoList().toMutableList()
            val remainingSlots = (3 - currentPhotos.size).coerceAtLeast(0)
            if (remainingSlots == 0) {
                withContext(Dispatchers.Main) {
                    _uiMessage.value = "الحد الأقصى للصور هو 3 صور لكل صنف"
                }
                return@launch
            }
            var addedCount = 0
            uris.take(remainingSlots).forEach { uri ->
                val encoded = ImageUtils.compressUriToBase64DataUri(ctx, uri)
                if (!encoded.isNullOrBlank()) {
                    currentPhotos.add(encoded)
                    addedCount++
                }
            }
            if (addedCount > 0) {
                businessRepository.updateMenuItem(
                    latest.copy(photosJson = Base64ImageUtils.encodePhotosJson(currentPhotos))
                )
                autoSyncMenuIfConfigured()
            }
            withContext(Dispatchers.Main) {
                _uiMessage.value = if (addedCount > 0) {
                    "تم ضغط ورفع $addedCount صورة ومزامنتها مع السحابة بنجاح 📸"
                } else {
                    "تعذر قراءة الصورة المختارة، يرجى المحاولة بصورة أخرى"
                }
            }
        }
    }

    fun removeMenuItemPhotoAt(menuItem: MenuItemEntity, photoIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val latest = businessRepository.getMenuItemByIdOnce(menuItem.id) ?: menuItem
            val currentPhotos = latest.photoList().toMutableList()
            if (photoIndex in currentPhotos.indices) {
                currentPhotos.removeAt(photoIndex)
                businessRepository.updateMenuItem(
                    latest.copy(photosJson = Base64ImageUtils.encodePhotosJson(currentPhotos))
                )
                autoSyncMenuIfConfigured()
            }
        }
    }

    // Published Menu Actions
    fun addMenuCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val nextOrder = (menuCategories.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
            businessRepository.insertMenuCategory(MenuCategoryEntity(name = name.trim(), sortOrder = nextOrder))
            autoSyncMenuIfConfigured()
        }
    }

    fun updateMenuCategory(category: MenuCategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            businessRepository.updateMenuCategory(category)
            autoSyncMenuIfConfigured()
        }
    }

    fun deleteMenuCategory(category: MenuCategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            businessRepository.deleteMenuCategory(category)
            autoSyncMenuIfConfigured()
        }
    }

    fun moveMenuCategory(category: MenuCategoryEntity, moveUp: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val sorted = menuCategories.value.sortedBy { it.sortOrder }
            val idx = sorted.indexOfFirst { it.id == category.id }
            val targetIdx = if (moveUp) idx - 1 else idx + 1
            if (idx in sorted.indices && targetIdx in sorted.indices) {
                val mutable = sorted.toMutableList()
                val moved = mutable.removeAt(idx)
                mutable.add(targetIdx, moved)
                businessRepository.updateMenuCategories(mutable.mapIndexed { i, c -> c.copy(sortOrder = i) })
                autoSyncMenuIfConfigured()
            }
        }
    }

    fun addProductToMenu(
        product: ProductEntity,
        menuCategoryId: Long,
        publishedPrice: Double = product.defaultSellingPrice,
        isVariablePrice: Boolean = publishedPrice <= 0.0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val nextOrder = (menuItems.value.filter { it.menuCategoryId == menuCategoryId }
                .maxOfOrNull { it.sortOrder } ?: -1) + 1
            businessRepository.insertMenuItem(
                MenuItemEntity(
                    menuCategoryId = menuCategoryId,
                    productId = product.id,
                    displayName = product.name,
                    unitLabel = product.unit,
                    description = product.description,
                    publishedPrice = publishedPrice,
                    isVariablePrice = isVariablePrice,
                    sortOrder = nextOrder,
                    isVisible = true
                )
            )
            autoSyncMenuIfConfigured()
        }
    }

    fun addManualMenuItem(
        menuCategoryId: Long,
        displayName: String,
        unitLabel: String,
        description: String,
        publishedPrice: Double,
        isVariablePrice: Boolean,
        isPopular: Boolean = false,
        isPreOrder24h: Boolean = false,
        photos: List<String> = emptyList()
    ) {
        if (displayName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val nextOrder = (menuItems.value.filter { it.menuCategoryId == menuCategoryId }
                .maxOfOrNull { it.sortOrder } ?: -1) + 1
            businessRepository.insertMenuItem(
                MenuItemEntity(
                    menuCategoryId = menuCategoryId,
                    productId = null,
                    displayName = displayName.trim(),
                    unitLabel = unitLabel.ifBlank { "كيلو" }.trim(),
                    description = description.trim(),
                    publishedPrice = publishedPrice,
                    isVariablePrice = isVariablePrice,
                    sortOrder = nextOrder,
                    isVisible = true,
                    photosJson = Base64ImageUtils.encodePhotosJson(photos),
                    isPopular = isPopular,
                    isPreOrder24h = isPreOrder24h
                )
            )
            autoSyncMenuIfConfigured()
        }
    }

    fun updateMenuItem(item: MenuItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            businessRepository.updateMenuItem(item)
            item.productId?.let { pid ->
                products.value.find { it.id == pid }?.let { linkedProd ->
                    businessRepository.updateProduct(
                        linkedProd.copy(
                            name = item.displayName,
                            unit = item.unitLabel,
                            description = item.description,
                            defaultSellingPrice = item.publishedPrice
                        )
                    )
                }
            }
            autoSyncMenuIfConfigured()
        }
    }

    fun restoreMenuItemDefaultPrice(item: MenuItemEntity) {
        val productId = item.productId ?: return
        val product = products.value.find { it.id == productId } ?: return
        viewModelScope.launch(Dispatchers.IO) {
            businessRepository.updateMenuItem(
                item.copy(
                    publishedPrice = product.defaultSellingPrice,
                    isVariablePrice = product.defaultSellingPrice <= 0.0
                )
            )
            autoSyncMenuIfConfigured()
        }
    }

    fun deleteMenuItem(item: MenuItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            businessRepository.deleteMenuItem(item)
            autoSyncMenuIfConfigured()
        }
    }

    fun moveMenuItem(item: MenuItemEntity, moveUp: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val sameCat = menuItems.value
                .filter { it.menuCategoryId == item.menuCategoryId }
                .sortedBy { it.sortOrder }
            val idx = sameCat.indexOfFirst { it.id == item.id }
            val targetIdx = if (moveUp) idx - 1 else idx + 1
            if (idx in sameCat.indices && targetIdx in sameCat.indices) {
                val mutable = sameCat.toMutableList()
                val moved = mutable.removeAt(idx)
                mutable.add(targetIdx, moved)
                businessRepository.updateMenuItems(mutable.mapIndexed { i, m -> m.copy(sortOrder = i) })
                autoSyncMenuIfConfigured()
            }
        }
    }

    // Live Menu Preview & High-Res PNG Export
    fun setPreviewCategoryFilter(categoryId: Long?) {
        _selectedPreviewCategoryId.value = categoryId
        refreshMenuPreview(categoryId)
    }

    fun refreshMenuPreview(singleCategoryId: Long? = _selectedPreviewCategoryId.value) {
        viewModelScope.launch {
            _isGeneratingPreview.value = true
            val cats = menuCategories.value.sortedBy { it.sortOrder }
            val itemsByCat = menuItems.value.sortedBy { it.sortOrder }.groupBy { it.menuCategoryId }
            val sections = cats.map { cat -> MenuPosterSection(cat, itemsByCat[cat.id].orEmpty()) }
            val ctx = getApplication<Application>()

            val bitmaps = withContext(Dispatchers.IO) {
                MenuPosterRenderer.renderMenuPages(ctx, sections, singleCategoryId)
            }
            _previewBitmaps.value = bitmaps
            _isGeneratingPreview.value = false
        }
    }

    fun exportMenuToPng(
        singleCategoryId: Long? = _selectedPreviewCategoryId.value,
        singlePageZeroIndex: Int? = null,
        onExportComplete: (List<String>) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isGeneratingPreview.value = true
            val cats = menuCategories.value.sortedBy { it.sortOrder }
            val itemsByCat = menuItems.value.sortedBy { it.sortOrder }.groupBy { it.menuCategoryId }
            val sections = cats.map { cat -> MenuPosterSection(cat, itemsByCat[cat.id].orEmpty()) }
            val ctx = getApplication<Application>()

            val savedPaths = withContext(Dispatchers.IO) {
                val rendered = MenuPosterRenderer.renderMenuPages(ctx, sections, singleCategoryId)
                _previewBitmaps.value = rendered
                val pagesToSave = if (singlePageZeroIndex != null && singlePageZeroIndex in rendered.indices) {
                    listOf(singlePageZeroIndex to rendered[singlePageZeroIndex])
                } else {
                    rendered.mapIndexed { i, bmp -> i to bmp }
                }

                val catName = singleCategoryId?.let { id -> cats.find { it.id == id }?.name } ?: "المنيو_الكامل"
                pagesToSave.mapNotNull { (idx, bmp) ->
                    val path = ImageStorageUtils.saveExportedMenuPng(ctx, bmp, "akleh_helwa_menu_p${idx + 1}")
                    if (path != null) {
                        businessRepository.insertGeneratedMenu(
                            GeneratedMenuEntity(
                                title = if (rendered.size > 1) "$catName - صفحة ${idx + 1}" else catName,
                                filePath = path,
                                pageIndex = idx + 1,
                                totalPages = rendered.size
                            )
                        )
                    }
                    path
                }
            }

            _isGeneratingPreview.value = false
            _uiMessage.value = if (savedPaths.isNotEmpty()) {
                "تم حفظ وتصدير المنيو (${savedPaths.size} صورة) في المعرض"
            } else {
                "تعذر تصدير المنيو"
            }
            onExportComplete(savedPaths)
        }
    }

    fun deleteGeneratedMenu(export: GeneratedMenuEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            ImageStorageUtils.deleteStoredFile(export.filePath)
            businessRepository.deleteGeneratedMenu(export)
        }
    }

    // Separate Expenses
    fun saveExpense(title: String, category: String, amount: Double, notes: String = "") {
        if (title.isBlank() || amount <= 0.0) return
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            businessRepository.insertExpense(
                ExpenseEntity(
                    title = title.trim(),
                    category = category.ifBlank { "تشغيل ومواصلات" },
                    amount = amount,
                    expenseDate = now,
                    yearMonth = _selectedYearMonth.value,
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            businessRepository.deleteExpense(expense)
        }
    }

    // Share text helpers
    fun generateMonthlyReportText(): String {
        val stats = monthlyStats.value
        val monthArabic = DateUtils.formatYearMonthArabic(_selectedYearMonth.value)
        return buildString {
            appendLine("🍲 *تقرير مبيعات وأرباح مشروع أكلة حلوة* 🍲")
            appendLine("👩‍🍳 *إدارة:* أ/ نهى ماهر")
            appendLine("📅 *شهر:* $monthArabic")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("💰 *إجمالي المبيعات:* ${DateUtils.formatCurrency(stats.totalSales)}")
            appendLine("🛒 *إجمالي تكلفة الشراء:* ${DateUtils.formatCurrency(stats.totalPurchaseCost)}")
            appendLine("💵 *إجمالي الربح (قبل المصاريف):* ${DateUtils.formatCurrency(stats.grossProfitBeforeExpenses)}")
            appendLine("🧾 *المصاريف المنفصلة:* ${DateUtils.formatCurrency(stats.totalExpenses)}")
            appendLine("📦 *عدد الأوردرات:* ${stats.totalOrdersCount} أوردر (${stats.uniqueCustomersCount} عميل)")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("أكلة حلوة.. بالهنا والشفا دائماً ❤️")
        }
    }

    fun generateOrderReceiptText(order: Order): String {
        val statusArabic = OrderStatus.trackingTitle(order.status)
        val paymentArabic = when (order.paymentStatus) {
            PaymentStatus.PAID.name -> PaymentStatus.PAID.titleAr
            PaymentStatus.PARTIAL.name -> PaymentStatus.PARTIAL.titleAr
            else -> PaymentStatus.UNPAID.titleAr
        }
        val items = orderItemsMap.value[order.id].orEmpty()

        return buildString {
            appendLine("🍲 *فاتورة طلب - مشروع أكلة حلوة* 🍲")
            appendLine("👩‍🍳 *بإدارة:* نهى ماهر")
            appendLine("👤 *العميل:* ${order.customerName}")
            if (order.customerPhone.isNotBlank()) appendLine("📱 *الهاتف:* ${order.customerPhone}")
            if (order.deliveryZone.isNotBlank()) appendLine("🗺️ *منطقة التوصيل:* ${order.deliveryZone}")
            if (order.deliveryAddress.isNotBlank()) appendLine("📍 *العنوان:* ${order.deliveryAddress}")
            appendLine("📅 *التاريخ:* ${DateUtils.formatDate(order.orderDate)}")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("🍽️ *الأصناف المطلوبة:*")
            if (items.isNotEmpty()) {
                items.forEach { li ->
                    val q = if (li.quantity % 1.0 == 0.0) li.quantity.toInt().toString() else li.quantity.toString()
                    appendLine("• $q × ${li.productNameSnapshot} (${DateUtils.formatCurrency(li.totalSellingPrice)})")
                }
            } else {
                appendLine(order.itemsDescription)
            }
            appendLine("━━━━━━━━━━━━━━━━━━")
            if (order.shippingFee > 0.0) {
                appendLine("🛵 *رسوم التوصيل:* ${DateUtils.formatCurrency(order.shippingFee)}")
            }
            appendLine("💵 *الإجمالي المطلوب:* ${DateUtils.formatCurrency(order.salePrice + order.shippingFee)}")
            appendLine("💳 *حالة الدفع:* $paymentArabic")
            appendLine("📦 *حالة الطلب:* $statusArabic")
            if (order.notes.isNotBlank()) appendLine("📝 *ملاحظات:* ${order.notes}")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("شكراً لاختياركم أكلة حلوة! بالهنا والشفا ❤️")
        }
    }
}
