package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.OrderRepository
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppTab {
    DASHBOARD,
    ORDERS,
    MENU,
    MONTHLY_STATS
}

data class MenuItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val estimatedCost: Double,
    val description: String,
    val imageRes: Int? = null
)

data class DailyStatPoint(
    val dayNumber: Int,
    val sales: Double,
    val profit: Double
)

data class MonthlyStats(
    val yearMonth: String = "",
    val totalSales: Double = 0.0,
    val totalCost: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val totalOrdersCount: Int = 0,
    val deliveredCount: Int = 0,
    val pendingCount: Int = 0,
    val cancelledCount: Int = 0,
    val averageOrderProfit: Double = 0.0,
    val dailyPoints: List<DailyStatPoint> = emptyList()
)

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OrderRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = OrderRepository(db.orderDao())
        viewModelScope.launch {
            repository.ensureDataExists()
        }
    }

    private val _selectedTab = MutableStateFlow(AppTab.DASHBOARD)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _selectedYearMonth = MutableStateFlow(DateUtils.getCurrentYearMonth())
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null) // null = ALL
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    // Dialog state
    private val _editingOrder = MutableStateFlow<Order?>(null)
    val editingOrder: StateFlow<Order?> = _editingOrder.asStateFlow()
    private val _isOrderDialogOpen = MutableStateFlow(false)
    val isOrderDialogOpen: StateFlow<Boolean> = _isOrderDialogOpen.asStateFlow()

    // Detail dialog state
    private val _selectedOrderDetails = MutableStateFlow<Order?>(null)
    val selectedOrderDetails: StateFlow<Order?> = _selectedOrderDetails.asStateFlow()

    val availableMonths: StateFlow<List<String>> = repository.distinctMonths
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(DateUtils.getCurrentYearMonth())
        )

    val ordersForSelectedMonth: StateFlow<List<Order>> = _selectedYearMonth
        .flatMapLatest { ym -> repository.getOrdersByMonth(ym) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
                        order.notes.contains(query, ignoreCase = true)
            }
            val matchesStatus = if (filter == null) true else order.status == filter
            matchesQuery && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val monthlyStats: StateFlow<MonthlyStats> = ordersForSelectedMonth.combine(_selectedYearMonth) { orders, ym ->
        calculateStats(orders, ym)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyStats()
    )

    private fun calculateStats(orders: List<Order>, yearMonth: String): MonthlyStats {
        // Active orders: exclude CANCELLED from revenue and cost
        val nonCancelled = orders.filter { it.status != OrderStatus.CANCELLED.name }

        val totalSales = nonCancelled.sumOf { it.salePrice }
        val totalCost = nonCancelled.sumOf { it.costPrice }
        val netProfit = nonCancelled.sumOf { it.netProfit }

        val profitMargin = if (totalSales > 0) (netProfit / totalSales) * 100.0 else 0.0

        val totalCount = orders.size
        val deliveredCount = orders.count { it.status == OrderStatus.DELIVERED.name }
        val pendingCount = orders.count { it.status == OrderStatus.PENDING.name }
        val cancelledCount = orders.count { it.status == OrderStatus.CANCELLED.name }

        val avgProfit = if (nonCancelled.isNotEmpty()) netProfit / nonCancelled.size else 0.0

        // Group by day of month
        val cal = Calendar.getInstance()
        val dailyMap = mutableMapOf<Int, Pair<Double, Double>>() // day -> (sales, profit)

        for (order in nonCancelled) {
            cal.timeInMillis = order.orderDate
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val current = dailyMap[day] ?: Pair(0.0, 0.0)
            dailyMap[day] = Pair(current.first + order.salePrice, current.second + order.netProfit)
        }

        val dailyPoints = dailyMap.map { (day, values) ->
            DailyStatPoint(dayNumber = day, sales = values.first, profit = values.second)
        }.sortedBy { it.dayNumber }

        return MonthlyStats(
            yearMonth = yearMonth,
            totalSales = totalSales,
            totalCost = totalCost,
            netProfit = netProfit,
            profitMarginPercent = profitMargin,
            totalOrdersCount = totalCount,
            deliveredCount = deliveredCount,
            pendingCount = pendingCount,
            cancelledCount = cancelledCount,
            averageOrderProfit = avgProfit,
            dailyPoints = dailyPoints
        )
    }

    fun setTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun selectMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun previousMonth() {
        _selectedYearMonth.value = DateUtils.getPreviousMonth(_selectedYearMonth.value)
    }

    fun nextMonth() {
        _selectedYearMonth.value = DateUtils.getNextMonth(_selectedYearMonth.value)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
    }

    fun openAddOrderDialog(orderToEdit: Order? = null) {
        _editingOrder.value = orderToEdit
        _isOrderDialogOpen.value = true
    }

    fun closeOrderDialog() {
        _editingOrder.value = null
        _isOrderDialogOpen.value = false
    }

    fun showOrderDetails(order: Order?) {
        _selectedOrderDetails.value = order
    }

    fun saveOrder(
        id: Long = 0,
        customerName: String,
        customerPhone: String,
        itemsDescription: String,
        salePrice: Double,
        costPrice: Double,
        orderDate: Long,
        status: String,
        paymentStatus: String,
        notes: String
    ) {
        viewModelScope.launch {
            val ym = DateUtils.getYearMonthFromTimestamp(orderDate)
            val order = Order(
                id = id,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                itemsDescription = itemsDescription.trim(),
                salePrice = salePrice,
                costPrice = costPrice,
                netProfit = salePrice - costPrice,
                orderDate = orderDate,
                yearMonth = ym,
                status = status,
                paymentStatus = paymentStatus,
                notes = notes.trim()
            )

            if (id == 0L) {
                repository.insertOrder(order)
            } else {
                repository.updateOrder(order)
                if (_selectedOrderDetails.value?.id == id) {
                    _selectedOrderDetails.value = order
                }
            }
            closeOrderDialog()
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            if (_selectedOrderDetails.value?.id == order.id) {
                _selectedOrderDetails.value = null
            }
        }
    }

    fun toggleOrderStatus(order: Order) {
        viewModelScope.launch {
            val nextStatus = when (order.status) {
                OrderStatus.PENDING.name -> OrderStatus.DELIVERED.name
                OrderStatus.DELIVERED.name -> OrderStatus.PENDING.name
                else -> OrderStatus.PENDING.name
            }
            val updated = order.copy(status = nextStatus)
            repository.updateOrder(updated)
            if (_selectedOrderDetails.value?.id == order.id) {
                _selectedOrderDetails.value = updated
            }
        }
    }

    fun togglePaymentStatus(order: Order) {
        viewModelScope.launch {
            val nextPayment = if (order.paymentStatus == PaymentStatus.PAID.name) {
                PaymentStatus.UNPAID.name
            } else {
                PaymentStatus.PAID.name
            }
            val updated = order.copy(paymentStatus = nextPayment)
            repository.updateOrder(updated)
            if (_selectedOrderDetails.value?.id == order.id) {
                _selectedOrderDetails.value = updated
            }
        }
    }

    val menuItems = listOf(
        MenuItem("m1", "طاجن عكاوي بالبصل القاورما", "طواجن", 380.0, 220.0, "طاجن عكاوي بلدي مع بصل مكرمل وصوص بهارات مميز", com.example.R.drawable.img_dish_tajine),
        MenuItem("m2", "محشي مشكل فاخر", "محاشي", 320.0, 180.0, "تشكيلة محشي كرنب، ورق عنب، وكوسة بالسمنة البلدي", com.example.R.drawable.img_dish_mahshi),
        MenuItem("m3", "طاجن مكرونة بشاميل باللحمة المفرومة", "صواني", 220.0, 130.0, "طبقات مكرونة غنية بالصوص الأبيض ولحمة بلدي معصجة", com.example.R.drawable.img_food_banner),
        MenuItem("m4", "صينية كفتة حاتي مشوية", "مشويات", 360.0, 210.0, "كيلو كفتة حاتي لحم بلدي مع أرز بسمتي وسلطة طحينة", com.example.R.drawable.img_dish_tajine),
        MenuItem("m5", "بطة بلدي محمرة بالبرتقال", "محمرات", 550.0, 320.0, "بطة فلاحي متبلة ومحمرة مع صوص البرتقال والأرز بالمكسرات", com.example.R.drawable.img_dish_mahshi),
        MenuItem("m6", "حواوشي إسكندراني بلدي (٥ أرغفة)", "معجنات", 260.0, 150.0, "لحمة بلدي متبلة بالخلطة الإسكندرانية والموتزاريلا", com.example.R.drawable.img_food_banner),
        MenuItem("m7", "صينية بسبوسة مكرملة بالسمنة البلدي", "حلويات", 190.0, 95.0, "بسبوسة دايبة مكرملة بالمكسرات وقشطة بلدي", com.example.R.drawable.img_dish_tajine),
        MenuItem("m8", "صينية كنافة نابلسية بالقشطة", "حلويات", 240.0, 120.0, "كنافة مقرمشة محشية قشطة طازجة وفستق حلبي", com.example.R.drawable.img_dish_mahshi)
    )

    fun openAddOrderForMenuItem(item: MenuItem) {
        val templateOrder = Order(
            customerName = "",
            customerPhone = "",
            itemsDescription = item.name,
            salePrice = item.price,
            costPrice = item.estimatedCost,
            netProfit = item.price - item.estimatedCost,
            orderDate = System.currentTimeMillis(),
            yearMonth = _selectedYearMonth.value,
            status = OrderStatus.PENDING.name,
            paymentStatus = PaymentStatus.PAID.name,
            notes = "طلب من المنيو: ${item.name}"
        )
        openAddOrderDialog(templateOrder)
    }

    fun generateMonthlyReportText(): String {
        val stats = monthlyStats.value
        val monthArabic = DateUtils.formatYearMonthArabic(_selectedYearMonth.value)
        return buildString {
            appendLine("🍲 *تقرير مبيعات وأرباح مشروع أكلة حلوة* 🍲")
            appendLine("📅 *شهر:* $monthArabic")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("💰 *إجمالي المبيعات:* ${DateUtils.formatCurrency(stats.totalSales)}")
            appendLine("📉 *إجمالي التكاليف:* ${DateUtils.formatCurrency(stats.totalCost)}")
            appendLine("💵 *صافي الأرباح:* ${DateUtils.formatCurrency(stats.netProfit)}")
            appendLine("📊 *نسبة هامش الربح:* ${String.format(java.util.Locale.US, "%.1f", stats.profitMarginPercent)}%")
            appendLine("📦 *عدد الأوردرات:* ${stats.totalOrdersCount} أوردر")
            appendLine("✅ *تم تسليمها:* ${stats.deliveredCount}")
            appendLine("⏳ *قيد التجهيز:* ${stats.pendingCount}")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("أكلة حلوة.. بالهنا والشفا دائماً ❤️")
        }
    }

    fun generateOrderReceiptText(order: Order): String {
        val statusArabic = when (order.status) {
            OrderStatus.PENDING.name -> OrderStatus.PENDING.titleAr
            OrderStatus.DELIVERED.name -> OrderStatus.DELIVERED.titleAr
            else -> OrderStatus.CANCELLED.titleAr
        }
        val paymentArabic = when (order.paymentStatus) {
            PaymentStatus.PAID.name -> PaymentStatus.PAID.titleAr
            PaymentStatus.PARTIAL.name -> PaymentStatus.PARTIAL.titleAr
            else -> PaymentStatus.UNPAID.titleAr
        }

        return buildString {
            appendLine("🍲 *فاتورة طلب - مشروع أكلة حلوة* 🍲")
            appendLine("👤 *العميل:* ${order.customerName}")
            if (order.customerPhone.isNotBlank()) {
                appendLine("📱 *الهاتف:* ${order.customerPhone}")
            }
            appendLine("📅 *التاريخ:* ${DateUtils.formatDate(order.orderDate)}")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("🍽️ *الأصناف والوجبات:*")
            appendLine(order.itemsDescription)
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("💵 *المبلغ المطلوب:* ${DateUtils.formatCurrency(order.salePrice)}")
            appendLine("💳 *حالة الدفع:* $paymentArabic")
            appendLine("📦 *حالة الطلب:* $statusArabic")
            if (order.notes.isNotBlank()) {
                appendLine("📝 *ملاحظات:* ${order.notes}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("شكراً لاختياركم أكلة حلوة! بالهنا والشفا ❤️")
        }
    }
}
