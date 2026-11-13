package com.example.model

import java.util.UUID

enum class AppStage {
    SPLASH,
    ONBOARDING,
    CUSTOMER_STOREFRONT,
    ADMIN_PANEL
}

data class CustomerProfile(
    val userId: String = "",
    val username: String = "",
    val password: String = "",
    val phone: String = "",
    val address: String = ""
) {
    val isRegistered: Boolean get() = userId.isNotBlank() && username.isNotBlank()
}

data class AppUser(
    val userId: String = "",
    val username: String = "",
    val password: String = "",
    val joinedAt: Long = 0L,
    val lastSeen: Long = 0L,
    val isOnline: Boolean = false,
    val ordersCount: Int = 0
) {
    fun isLiveOnline(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return isOnline && lastSeen > 0L && (nowMillis - lastSeen) < PRESENCE_TIMEOUT_MS
    }

    companion object {
        const val PRESENCE_TIMEOUT_MS = 90_000L
    }
}

data class DeliveryZone(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val fee: Double
) {
    companion object {
        val DEFAULT_ZONES = listOf(
            DeliveryZone(id = "pickup", name = "استلام من المطبخ (بدون توصيل)", fee = 0.0),
            DeliveryZone(id = "nasr_city", name = "مدينة نصر ومصر الجديدة", fee = 35.0),
            DeliveryZone(id = "maadi", name = "المعادي والمقطم", fee = 45.0),
            DeliveryZone(id = "october_zayed", name = "أكتوبر والشيخ زايد", fee = 60.0)
        )
    }
}

data class TrackedOrderLineItem(
    val menuItemId: Long = 0L,
    val productId: Long? = null,
    val productName: String = "",
    val quantity: Double = 1.0,
    val unitSellingPrice: Double = 0.0,
    val totalSellingPrice: Double = 0.0
)

data class TrackedCloudOrder(
    val cloudId: String,
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryZone: String = "",
    val shippingFee: Double = 0.0,
    val deliveryAddress: String = "",
    val itemsDescription: String = "",
    val salePrice: Double = 0.0,
    val orderDate: Long = 0L,
    val status: String = OrderStatus.PENDING_REVIEW.name,
    val isRated: Boolean = false,
    val lineItems: List<TrackedOrderLineItem> = emptyList()
)

data class DailyStatPoint(
    val dayNumber: Int,
    val sales: Double,
    val grossProfit: Double,
    val ordersCount: Int
)

data class TopProductStat(
    val productName: String,
    val totalQuantity: Double,
    val totalSales: Double,
    val totalGrossProfit: Double,
    val orderCount: Int
)

data class MonthlyStats(
    val yearMonth: String = "",
    val totalSales: Double = 0.0,
    val totalPurchaseCost: Double = 0.0,
    val grossProfitBeforeExpenses: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalOrdersCount: Int = 0,
    val deliveredCount: Int = 0,
    val pendingCount: Int = 0,
    val cancelledCount: Int = 0,
    val uniqueCustomersCount: Int = 0,
    val averageOrderProfit: Double = 0.0,
    val dailyPoints: List<DailyStatPoint> = emptyList(),
    val topProducts: List<TopProductStat> = emptyList()
)

data class DraftOrderLineItem(
    val tempId: String = UUID.randomUUID().toString(),
    val productId: Long? = null,
    val productNameSnapshot: String,
    val productImagePathSnapshot: String? = null,
    val unitLabel: String = "كيلو",
    val quantity: Double = 1.0,
    val unitPurchaseCost: Double = 0.0,
    val unitSellingPrice: Double = 0.0
) {
    val totalPurchaseCost: Double get() = quantity * unitPurchaseCost
    val totalSellingPrice: Double get() = quantity * unitSellingPrice
    val itemGrossProfit: Double get() = totalSellingPrice - totalPurchaseCost
}

data class CustomerCartItem(
    val menuItem: MenuItemEntity,
    val imagePath: String? = null,
    val quantity: Double = 1.0
) {
    val lineTotal: Double
        get() = if (menuItem.isVariablePrice && menuItem.publishedPrice <= 0.0) {
            0.0
        } else {
            quantity * menuItem.publishedPrice
        }
}

data class CloudConfig(
    val projectId: String = DEFAULT_PROJECT_ID,
    val appId: String = DEFAULT_APP_ID,
    val apiKey: String = DEFAULT_API_KEY
) {
    val isConfigured: Boolean
        get() = projectId.isNotBlank() && appId.isNotBlank() && apiKey.isNotBlank()

    companion object {
        const val DEFAULT_PROJECT_ID = "akla-helwa"
        const val DEFAULT_APP_ID = "1:689898276457:android:3d7dcc504a144e9b2144f8"
        const val DEFAULT_API_KEY = "AIzaSyBG6YJFv-n-heOUNVYV7oxMQMYxh1eOpnY"
    }
}

sealed class CloudSyncState {
    data object LocalOffline : CloudSyncState()
    data object Connecting : CloudSyncState()
    data class Connected(val projectId: String, val lastSyncMillis: Long = System.currentTimeMillis()) : CloudSyncState()
    data class Error(val message: String) : CloudSyncState()

    val isCloudConnected: Boolean
        get() = this is Connected

    val statusMessage: String
        get() = when (this) {
            is LocalOffline -> "جاري التهيئة السحابية..."
            is Connecting -> "جاري الاتصال بسحابة Firebase..."
            is Connected -> "متصل بالسحابة تلقائياً ($projectId)"
            is Error -> "تعذر الاتصال بالسحابة"
        }

    val lastError: String?
        get() = (this as? Error)?.message
}

data class AppVersionConfig(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0.0",
    val downloadUrl: String = "",
    val releaseNotes: String = "",
    val isMandatory: Boolean = false
) {
    fun isUpdateAvailable(installedVersionCode: Int): Boolean {
        return latestVersionCode > installedVersionCode && downloadUrl.isNotBlank()
    }
}

