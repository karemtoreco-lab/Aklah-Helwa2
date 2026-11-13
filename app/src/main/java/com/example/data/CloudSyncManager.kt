package com.example.data

import android.content.Context
import com.example.model.AppUser
import com.example.model.CloudConfig
import com.example.model.CloudSyncState
import com.example.model.CustomerCartItem
import com.example.model.CustomerProfile
import com.example.model.DeliveryZone
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import com.example.model.Order
import com.example.model.OrderLineItemEntity
import com.example.model.OrderSource
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.model.TrackedCloudOrder
import com.example.model.TrackedOrderLineItem
import com.example.util.Base64ImageUtils
import com.example.util.DateUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Runtime-configured Firebase Firestore Cloud Sync Manager.
 * Auto-initializes with hardcoded `akla-helwa` credentials so Customer & Admin devices
 * stay continuously synchronized in real time across menu items (with Base64 photos),
 * customer orders & live status tracking, user presence/analytics, and store settings.
 */
class CloudSyncManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val orderRepository: OrderRepository,
    private val businessRepository: BusinessRepository
) {
    private val _syncState = MutableStateFlow<CloudSyncState>(CloudSyncState.LocalOffline)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private val _cloudMenuCategories = MutableStateFlow<List<MenuCategoryEntity>?>(null)
    val cloudMenuCategories: StateFlow<List<MenuCategoryEntity>?> = _cloudMenuCategories.asStateFlow()

    private val _cloudMenuItems = MutableStateFlow<List<MenuItemEntity>?>(null)
    val cloudMenuItems: StateFlow<List<MenuItemEntity>?> = _cloudMenuItems.asStateFlow()

    private val _cloudWhatsAppNumber = MutableStateFlow<String?>(null)
    val cloudWhatsAppNumber: StateFlow<String?> = _cloudWhatsAppNumber.asStateFlow()

    private val _cloudKitchenOpen = MutableStateFlow<Boolean?>(null)
    val cloudKitchenOpen: StateFlow<Boolean?> = _cloudKitchenOpen.asStateFlow()

    private val _cloudDailyOffer = MutableStateFlow<String?>(null)
    val cloudDailyOffer: StateFlow<String?> = _cloudDailyOffer.asStateFlow()

    private val _cloudDeliveryZones = MutableStateFlow<List<DeliveryZone>?>(null)
    val cloudDeliveryZones: StateFlow<List<DeliveryZone>?> = _cloudDeliveryZones.asStateFlow()

    private val _cloudAppUsers = MutableStateFlow<List<AppUser>>(emptyList())
    val cloudAppUsers: StateFlow<List<AppUser>> = _cloudAppUsers.asStateFlow()

    private val _cloudCustomerOrders = MutableStateFlow<List<TrackedCloudOrder>>(emptyList())
    val cloudCustomerOrders: StateFlow<List<TrackedCloudOrder>> = _cloudCustomerOrders.asStateFlow()

    private var firestore: FirebaseFirestore? = null
    private var activeConfig: CloudConfig? = null
    private var activeTrackedUserId: String = ""

    private var menuCatsListener: ListenerRegistration? = null
    private var menuItemsListener: ListenerRegistration? = null
    private var storeSettingsListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null
    private var userOrdersListener: ListenerRegistration? = null

    fun configureAndStart(
        config: CloudConfig,
        enableAdminListeners: Boolean = true,
        currentUserId: String = ""
    ) {
        if (!config.isConfigured) {
            disconnect()
            return
        }

        if (activeConfig == config && firestore != null) {
            val db = firestore!!
            if (enableAdminListeners && ordersListener == null) {
                attachAdminOrdersListener(db)
            }
            if (enableAdminListeners && usersListener == null) {
                attachAppUsersListener(db)
            }
            if (currentUserId.isNotBlank() && currentUserId != activeTrackedUserId) {
                attachCustomerOrdersListener(db, currentUserId)
            }
            return
        }

        disconnectListenersOnly()
        _syncState.value = CloudSyncState.Connecting

        try {
            val db = getOrInitFirestore(context, config)
            firestore = db
            activeConfig = config

            attachMenuListeners(db)
            attachStoreSettingsListener(db)
            if (enableAdminListeners) {
                attachAdminOrdersListener(db)
                attachAppUsersListener(db)
            }
            if (currentUserId.isNotBlank()) {
                attachCustomerOrdersListener(db, currentUserId)
            }
            _syncState.value = CloudSyncState.Connected(config.projectId)
        } catch (e: Exception) {
            firestore = null
            activeConfig = null
            _syncState.value = CloudSyncState.Error(e.localizedMessage ?: "تعذر تهيئة الاتصال السحابي")
        }
    }

    fun updateTrackedCustomerUserId(userId: String) {
        if (userId.isBlank() || userId == activeTrackedUserId) return
        val db = firestore ?: return
        attachCustomerOrdersListener(db, userId)
    }

    fun disconnect() {
        disconnectListenersOnly()
        firestore = null
        activeConfig = null
        activeTrackedUserId = ""
        _cloudMenuCategories.value = null
        _cloudMenuItems.value = null
        _cloudWhatsAppNumber.value = null
        _cloudKitchenOpen.value = null
        _cloudDailyOffer.value = null
        _cloudDeliveryZones.value = null
        _cloudAppUsers.value = emptyList()
        _cloudCustomerOrders.value = emptyList()
        _syncState.value = CloudSyncState.LocalOffline
    }

    private fun disconnectListenersOnly() {
        menuCatsListener?.remove(); menuCatsListener = null
        menuItemsListener?.remove(); menuItemsListener = null
        storeSettingsListener?.remove(); storeSettingsListener = null
        ordersListener?.remove(); ordersListener = null
        usersListener?.remove(); usersListener = null
        userOrdersListener?.remove(); userOrdersListener = null
    }

    private fun attachMenuListeners(db: FirebaseFirestore) {
        menuCatsListener = db.collection(COL_MENU_CATEGORIES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncState.value = CloudSyncState.Error(error.localizedMessage ?: "خطأ في مزامنة أقسام المنيو")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val cats = snapshot.documents.mapNotNull { doc ->
                        val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                        MenuCategoryEntity(
                            id = id,
                            name = doc.getString("name") ?: "",
                            sortOrder = (doc.getLong("sortOrder") ?: 0L).toInt(),
                            isVisible = doc.getBoolean("isVisible") ?: true
                        )
                    }.sortedBy { it.sortOrder }
                    _cloudMenuCategories.value = cats
                    activeConfig?.let {
                        _syncState.value = CloudSyncState.Connected(it.projectId)
                    }
                }
            }

        menuItemsListener = db.collection(COL_MENU_ITEMS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncState.value = CloudSyncState.Error(error.localizedMessage ?: "خطأ في مزامنة أصناف المنيو")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                        @Suppress("UNCHECKED_CAST")
                        val rawPhotos = (doc.get("photos") as? List<*>)?.mapNotNull { it as? String }?.take(3)
                            ?: emptyList()
                        MenuItemEntity(
                            id = id,
                            menuCategoryId = doc.getLong("menuCategoryId") ?: 0L,
                            productId = doc.getLong("productId"),
                            displayName = doc.getString("displayName") ?: "",
                            unitLabel = doc.getString("unitLabel") ?: "كيلو",
                            description = doc.getString("description") ?: "",
                            publishedPrice = doc.getDouble("publishedPrice") ?: 0.0,
                            isVariablePrice = doc.getBoolean("isVariablePrice") ?: false,
                            sortOrder = (doc.getLong("sortOrder") ?: 0L).toInt(),
                            isVisible = doc.getBoolean("isVisible") ?: true,
                            photosJson = Base64ImageUtils.encodePhotosJson(rawPhotos),
                            isPopular = doc.getBoolean("isPopular") ?: false,
                            isPreOrder24h = doc.getBoolean("isPreOrder24h") ?: false,
                            ratingAvg = doc.getDouble("ratingAvg") ?: 4.9,
                            ratingCount = (doc.getLong("ratingCount") ?: 0L).toInt()
                        )
                    }.sortedBy { it.sortOrder }
                    _cloudMenuItems.value = items
                }
            }
    }

    private fun attachStoreSettingsListener(db: FirebaseFirestore) {
        storeSettingsListener = db.collection(COL_STORE_SETTINGS)
            .document(DOC_STORE_PROFILE)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val phone = snapshot.getString("whatsappNumber")?.trim()
                if (!phone.isNullOrBlank()) {
                    _cloudWhatsAppNumber.value = phone
                }
                if (snapshot.contains("kitchenOpen")) {
                    _cloudKitchenOpen.value = snapshot.getBoolean("kitchenOpen") ?: true
                }
                if (snapshot.contains("dailyOfferText")) {
                    _cloudDailyOffer.value = snapshot.getString("dailyOfferText").orEmpty()
                }
                val zonesJson = snapshot.getString("deliveryZonesJson")
                if (!zonesJson.isNullOrBlank()) {
                    AppPreferences.decodeDeliveryZones(zonesJson)?.let { parsed ->
                        _cloudDeliveryZones.value = parsed
                    }
                }
            }
    }

    private fun attachAppUsersListener(db: FirebaseFirestore) {
        usersListener?.remove()
        usersListener = db.collection(COL_APP_USERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val users = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("userId") ?: doc.id
                    val uname = doc.getString("username")?.trim().orEmpty()
                    if (uname.isBlank()) return@mapNotNull null
                    AppUser(
                        userId = uid,
                        username = uname,
                        password = doc.getString("password").orEmpty(),
                        joinedAt = doc.getLong("joinedAt") ?: 0L,
                        lastSeen = doc.getLong("lastSeen") ?: 0L,
                        isOnline = doc.getBoolean("isOnline") ?: false,
                        ordersCount = (doc.getLong("ordersCount") ?: 0L).toInt()
                    )
                }.sortedWith(compareByDescending<AppUser> { it.isLiveOnline() }.thenByDescending { it.lastSeen })
                _cloudAppUsers.value = users
            }
    }

    private fun attachCustomerOrdersListener(db: FirebaseFirestore, userId: String) {
        userOrdersListener?.remove()
        activeTrackedUserId = userId
        userOrdersListener = db.collection(COL_ORDERS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val tracked = snapshot.documents.mapNotNull { doc ->
                    val cid = doc.getString("cloudId") ?: doc.id
                    @Suppress("UNCHECKED_CAST")
                    val rawItems = doc.get("lineItems") as? List<Map<String, Any?>> ?: emptyList()
                    val items = rawItems.map { map ->
                        TrackedOrderLineItem(
                            menuItemId = (map["menuItemId"] as? Number)?.toLong() ?: 0L,
                            productId = (map["productId"] as? Number)?.toLong(),
                            productName = (map["productName"] as? String).orEmpty(),
                            quantity = (map["quantity"] as? Number)?.toDouble() ?: 1.0,
                            unitSellingPrice = (map["unitSellingPrice"] as? Number)?.toDouble() ?: 0.0,
                            totalSellingPrice = (map["totalSellingPrice"] as? Number)?.toDouble() ?: 0.0
                        )
                    }
                    TrackedCloudOrder(
                        cloudId = cid,
                        userId = doc.getString("userId").orEmpty(),
                        customerName = doc.getString("customerName").orEmpty(),
                        customerPhone = doc.getString("customerPhone").orEmpty(),
                        deliveryZone = doc.getString("deliveryZone").orEmpty(),
                        shippingFee = doc.getDouble("shippingFee") ?: 0.0,
                        deliveryAddress = doc.getString("deliveryAddress").orEmpty(),
                        itemsDescription = doc.getString("itemsDescription").orEmpty(),
                        salePrice = doc.getDouble("salePrice") ?: 0.0,
                        orderDate = doc.getLong("orderDate") ?: 0L,
                        status = doc.getString("status") ?: OrderStatus.PENDING_REVIEW.name,
                        isRated = doc.getBoolean("isRated") ?: false,
                        lineItems = items
                    )
                }.sortedByDescending { it.orderDate }
                _cloudCustomerOrders.value = tracked
            }
    }

    private fun attachAdminOrdersListener(db: FirebaseFirestore) {
        ordersListener?.remove()
        ordersListener = db.collection(COL_ORDERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val prodMap = businessRepository.getAllProductsOnce().associateBy { it.id }
                    for (doc in snapshot.documents) {
                        val cloudId = doc.getString("cloudId") ?: doc.id
                        val existing = orderRepository.getOrderByCloudId(cloudId)
                        val remoteStatus = doc.getString("status") ?: OrderStatus.PENDING_REVIEW.name
                        val remoteRated = doc.getBoolean("isRated") ?: false

                        if (existing != null) {
                            if (existing.isRated != remoteRated) {
                                orderRepository.updateOrder(existing.copy(isRated = remoteRated))
                            }
                            continue
                        }

                        val customerName = doc.getString("customerName") ?: continue
                        val customerPhone = doc.getString("customerPhone") ?: ""
                        val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                        val deliveryZone = doc.getString("deliveryZone") ?: ""
                        val shippingFee = doc.getDouble("shippingFee") ?: 0.0
                        val userId = doc.getString("userId") ?: ""
                        val notes = doc.getString("notes") ?: ""
                        val summary = doc.getString("itemsDescription") ?: ""
                        val salePrice = doc.getDouble("salePrice") ?: 0.0
                        val orderDate = doc.getLong("orderDate") ?: System.currentTimeMillis()
                        val ym = doc.getString("yearMonth") ?: DateUtils.getYearMonthFromTimestamp(orderDate)

                        @Suppress("UNCHECKED_CAST")
                        val rawItems = doc.get("lineItems") as? List<Map<String, Any?>> ?: emptyList()
                        val lineEntities = rawItems.map { map ->
                            val productId = (map["productId"] as? Number)?.toLong()
                            val qty = (map["quantity"] as? Number)?.toDouble() ?: 1.0
                            val unitSell = (map["unitSellingPrice"] as? Number)?.toDouble() ?: 0.0
                            val unitPurchase = productId?.let { prodMap[it]?.purchaseCost } ?: 0.0
                            val totalSell = (map["totalSellingPrice"] as? Number)?.toDouble() ?: (qty * unitSell)
                            val totalCost = qty * unitPurchase
                            OrderLineItemEntity(
                                orderId = 0L,
                                productId = productId,
                                productNameSnapshot = (map["productName"] as? String).orEmpty(),
                                productImagePathSnapshot = null,
                                quantity = qty,
                                unitPurchaseCost = unitPurchase,
                                unitSellingPrice = unitSell,
                                totalPurchaseCost = totalCost,
                                totalSellingPrice = totalSell,
                                itemGrossProfit = totalSell - totalCost
                            )
                        }

                        val totalCost = lineEntities.sumOf { it.totalPurchaseCost }
                        val itemsRevenue = (salePrice - shippingFee).coerceAtLeast(0.0)
                        val newOrder = Order(
                            customerName = customerName,
                            customerPhone = customerPhone,
                            itemsDescription = summary,
                            salePrice = itemsRevenue,
                            costPrice = totalCost,
                            netProfit = itemsRevenue - totalCost,
                            orderDate = orderDate,
                            yearMonth = ym,
                            status = remoteStatus,
                            paymentStatus = PaymentStatus.UNPAID.name,
                            notes = notes,
                            deliveryAddress = deliveryAddress,
                            orderSource = OrderSource.CUSTOMER.name,
                            cloudId = cloudId,
                            userId = userId,
                            deliveryZone = deliveryZone,
                            shippingFee = shippingFee,
                            isRated = remoteRated
                        )
                        orderRepository.saveOrderWithItems(newOrder, lineEntities)
                    }
                }
            }
    }

    suspend fun upsertUserProfile(profile: CustomerProfile): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        if (!profile.isRegistered) return Result.success(Unit)
        return try {
            val now = System.currentTimeMillis()
            val payload = hashMapOf<String, Any>(
                "userId" to profile.userId,
                "username" to profile.username.trim(),
                "password" to profile.password,
                "lastSeen" to now,
                "isOnline" to true
            )
            val docRef = db.collection(COL_APP_USERS).document(profile.userId)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                payload["joinedAt"] = now
                payload["ordersCount"] = 0
            }
            docRef.set(payload, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserPresence(userId: String, isOnline: Boolean) {
        if (userId.isBlank()) return
        val db = firestore ?: return
        try {
            db.collection(COL_APP_USERS)
                .document(userId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "isOnline" to isOnline,
                        "lastSeen" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
        } catch (_: Exception) {
            // Presence updates fail silently offline
        }
    }

    suspend fun incrementUserOrdersCount(userId: String) {
        if (userId.isBlank()) return
        val db = firestore ?: return
        try {
            db.collection(COL_APP_USERS)
                .document(userId)
                .set(
                    mapOf(
                        "ordersCount" to FieldValue.increment(1L),
                        "lastSeen" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
        } catch (_: Exception) {
        }
    }

    suspend fun publishWhatsAppNumberToCloud(phoneNumber: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COL_STORE_SETTINGS)
                .document(DOC_STORE_PROFILE)
                .set(
                    mapOf(
                        "whatsappNumber" to phoneNumber.trim(),
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishKitchenOpenToCloud(isOpen: Boolean): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COL_STORE_SETTINGS)
                .document(DOC_STORE_PROFILE)
                .set(
                    mapOf(
                        "kitchenOpen" to isOpen,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishDailyOfferToCloud(offerText: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COL_STORE_SETTINGS)
                .document(DOC_STORE_PROFILE)
                .set(
                    mapOf(
                        "dailyOfferText" to offerText.trim(),
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishDeliveryZonesToCloud(zones: List<DeliveryZone>): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COL_STORE_SETTINGS)
                .document(DOC_STORE_PROFILE)
                .set(
                    mapOf(
                        "deliveryZonesJson" to AppPreferences.encodeDeliveryZones(zones),
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishMenuToCloud(
        categories: List<MenuCategoryEntity>,
        items: List<MenuItemEntity>
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("الربط السحابي غير مفعّل"))
        return try {
            val batch = db.batch()
            categories.forEach { cat ->
                val ref = db.collection(COL_MENU_CATEGORIES).document(cat.id.toString())
                batch.set(
                    ref,
                    mapOf(
                        "id" to cat.id,
                        "name" to cat.name,
                        "sortOrder" to cat.sortOrder,
                        "isVisible" to cat.isVisible,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            }
            items.forEach { item ->
                val ref = db.collection(COL_MENU_ITEMS).document(item.id.toString())
                batch.set(
                    ref,
                    mapOf(
                        "id" to item.id,
                        "menuCategoryId" to item.menuCategoryId,
                        "productId" to item.productId,
                        "displayName" to item.displayName,
                        "unitLabel" to item.unitLabel,
                        "description" to item.description,
                        "publishedPrice" to item.publishedPrice,
                        "isVariablePrice" to item.isVariablePrice,
                        "sortOrder" to item.sortOrder,
                        "isVisible" to item.isVisible,
                        "photos" to item.photoList(),
                        "isPopular" to item.isPopular,
                        "isPreOrder24h" to item.isPreOrder24h,
                        "ratingAvg" to item.ratingAvg,
                        "ratingCount" to item.ratingCount,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            }
            batch.commit().await()
            activeConfig?.let {
                _syncState.value = CloudSyncState.Connected(it.projectId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = CloudSyncState.Error(e.localizedMessage ?: "فشل نشر المنيو للسحابة")
            Result.failure(e)
        }
    }

    suspend fun pushCustomerOrderToCloud(
        cloudId: String,
        userId: String,
        customerName: String,
        customerPhone: String,
        deliveryZone: String,
        shippingFee: Double,
        deliveryAddress: String,
        notes: String,
        cartItems: List<CustomerCartItem>,
        totalSalePrice: Double,
        orderDate: Long,
        yearMonth: String
    ): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val summaryDesc = cartItems.joinToString("، ") { item ->
                val q = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
                "$q × ${item.menuItem.displayName}"
            }
            val payload = hashMapOf(
                "cloudId" to cloudId,
                "userId" to userId,
                "customerName" to customerName,
                "customerPhone" to customerPhone,
                "deliveryZone" to deliveryZone,
                "shippingFee" to shippingFee,
                "deliveryAddress" to deliveryAddress,
                "notes" to notes,
                "itemsDescription" to summaryDesc,
                "salePrice" to (totalSalePrice + shippingFee),
                "orderDate" to orderDate,
                "yearMonth" to yearMonth,
                "status" to OrderStatus.PENDING_REVIEW.name,
                "statusLabel" to OrderStatus.trackingTitle(OrderStatus.PENDING_REVIEW.name),
                "orderSource" to OrderSource.CUSTOMER.name,
                "isRated" to false,
                "lineItems" to cartItems.map { ci ->
                    mapOf(
                        "menuItemId" to ci.menuItem.id,
                        "productId" to ci.menuItem.productId,
                        "productName" to ci.menuItem.displayName,
                        "quantity" to ci.quantity,
                        "unitSellingPrice" to ci.menuItem.publishedPrice,
                        "totalSellingPrice" to ci.lineTotal
                    )
                }
            )
            db.collection(COL_ORDERS)
                .document(cloudId.ifBlank { UUID.randomUUID().toString() })
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatusInCloud(cloudId: String, newStatus: String): Result<Unit> {
        if (cloudId.isBlank()) return Result.success(Unit)
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(COL_ORDERS)
                .document(cloudId)
                .set(
                    mapOf(
                        "status" to newStatus,
                        "statusLabel" to OrderStatus.trackingTitle(newStatus),
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitOrderRatingsToCloud(
        orderCloudId: String,
        ratedMenuItems: List<MenuItemEntity>
    ): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val batch = db.batch()
            if (orderCloudId.isNotBlank()) {
                batch.set(
                    db.collection(COL_ORDERS).document(orderCloudId),
                    mapOf("isRated" to true),
                    SetOptions.merge()
                )
            }
            ratedMenuItems.forEach { item ->
                batch.set(
                    db.collection(COL_MENU_ITEMS).document(item.id.toString()),
                    mapOf(
                        "ratingAvg" to item.ratingAvg,
                        "ratingCount" to item.ratingCount,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val CLOUD_APP_NAME = "akleh_helwa_cloud"
        const val COL_MENU_CATEGORIES = "menu_categories"
        const val COL_MENU_ITEMS = "menu_items"
        const val COL_ORDERS = "orders"
        const val COL_APP_USERS = "app_users"
        const val COL_STORE_SETTINGS = "store_settings"
        const val DOC_STORE_PROFILE = "profile"

        fun getOrInitFirestore(
            context: Context,
            config: CloudConfig = CloudConfig()
        ): FirebaseFirestore {
            val existingApp = FirebaseApp.getApps(context).firstOrNull { it.name == CLOUD_APP_NAME }
            if (existingApp != null) {
                val opts = existingApp.options
                if (opts.projectId != config.projectId ||
                    opts.applicationId != config.appId ||
                    opts.apiKey != config.apiKey
                ) {
                    existingApp.delete()
                }
            }
            val app = FirebaseApp.getApps(context).firstOrNull { it.name == CLOUD_APP_NAME }
                ?: FirebaseApp.initializeApp(
                    context.applicationContext,
                    FirebaseOptions.Builder()
                        .setProjectId(config.projectId.trim())
                        .setApplicationId(config.appId.trim())
                        .setApiKey(config.apiKey.trim())
                        .build(),
                    CLOUD_APP_NAME
                )
            return FirebaseFirestore.getInstance(app)
        }
    }
}
