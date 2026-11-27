package com.example.data

import android.content.Context
import com.example.model.AppUser
import com.example.model.AppVersionConfig
import com.example.model.CategoryEntity
import com.example.model.CloudConfig
import com.example.model.CloudSyncState
import com.example.model.CustomerCartItem
import com.example.model.CustomerProfile
import com.example.model.DeliveryZone
import com.example.model.ForceResyncReport
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import com.example.model.Order
import com.example.model.OrderLineItemEntity
import com.example.model.OrderSource
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.model.ProductEntity
import com.example.model.TrackedCloudOrder
import com.example.model.TrackedOrderLineItem
import com.example.model.TreasuryTransactionEntity
import com.example.model.TreasuryTxType
import com.example.service.OrderNotificationService
import com.example.util.Base64ImageUtils
import com.example.util.DateUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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

    private val _cloudVersionConfig = MutableStateFlow<AppVersionConfig?>(null)
    val cloudVersionConfig: StateFlow<AppVersionConfig?> = _cloudVersionConfig.asStateFlow()

    private var firestore: FirebaseFirestore? = null
    private var activeConfig: CloudConfig? = null
    private var activeEnableAdminListeners: Boolean = true
    private var activeTrackedUserId: String = ""
    private var lastOfferTimestampSeen: Long = -1L

    private var menuCatsListener: ListenerRegistration? = null
    private var menuItemsListener: ListenerRegistration? = null
    private var catalogCatsListener: ListenerRegistration? = null
    private var productsListener: ListenerRegistration? = null
    private var storeSettingsListener: ListenerRegistration? = null
    private var versionConfigListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null
    private var userOrdersListener: ListenerRegistration? = null
    private var treasuryListener: ListenerRegistration? = null

    private val catalogSyncMutex = Mutex()
    private val menuSyncMutex = Mutex()
    private val ordersSyncMutex = Mutex()
    private val treasurySyncMutex = Mutex()

    private val pendingOrderCloudIds = ConcurrentHashMap.newKeySet<String>()
    private val pendingTreasuryCloudIds = ConcurrentHashMap.newKeySet<String>()

    @Volatile
    private var didAutoSeedCatalogCategories = false
    @Volatile
    private var didAutoSeedCatalogProducts = false

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
            activeEnableAdminListeners = enableAdminListeners
            if (enableAdminListeners) {
                if (catalogCatsListener == null || productsListener == null) {
                    attachCatalogListeners(db)
                }
                if (ordersListener == null) {
                    attachAdminOrdersListener(db)
                }
                if (usersListener == null) {
                    attachAppUsersListener(db)
                }
                if (treasuryListener == null) {
                    attachTreasuryListener(db)
                }
            }
            if (currentUserId.isNotBlank() && currentUserId != activeTrackedUserId) {
                attachCustomerOrdersListener(db, currentUserId)
            }
            return
        }

        disconnectListenersOnly()
        _syncState.value = CloudSyncState.Connecting
        activeEnableAdminListeners = enableAdminListeners

        try {
            val db = getOrInitFirestore(context, config)
            firestore = db
            activeConfig = config

            attachAllConfiguredListeners(
                db = db,
                enableAdminListeners = enableAdminListeners,
                currentUserId = currentUserId
            )
            _syncState.value = CloudSyncState.Connected(config.projectId)
        } catch (e: Exception) {
            firestore = null
            activeConfig = null
            _syncState.value = CloudSyncState.Error(e.localizedMessage ?: "تعذر تهيئة الاتصال السحابي")
        }
    }

    private fun attachAllConfiguredListeners(
        db: FirebaseFirestore,
        enableAdminListeners: Boolean,
        currentUserId: String
    ) {
        attachMenuListeners(db, syncToLocalRoom = enableAdminListeners)
        attachStoreSettingsListener(db)
        attachVersionConfigListener(db)
        if (enableAdminListeners) {
            attachCatalogListeners(db)
            attachAdminOrdersListener(db)
            attachAppUsersListener(db)
            attachTreasuryListener(db)
        }
        if (currentUserId.isNotBlank()) {
            attachCustomerOrdersListener(db, currentUserId)
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
        _cloudVersionConfig.value = null
        _syncState.value = CloudSyncState.LocalOffline
    }

    private fun disconnectListenersOnly() {
        menuCatsListener?.remove(); menuCatsListener = null
        menuItemsListener?.remove(); menuItemsListener = null
        catalogCatsListener?.remove(); catalogCatsListener = null
        productsListener?.remove(); productsListener = null
        storeSettingsListener?.remove(); storeSettingsListener = null
        versionConfigListener?.remove(); versionConfigListener = null
        ordersListener?.remove(); ordersListener = null
        usersListener?.remove(); usersListener = null
        userOrdersListener?.remove(); userOrdersListener = null
        treasuryListener?.remove(); treasuryListener = null
    }

    // ------------------------------------------------------------------------
    // Real-time Catalog (Categories & Products) Listeners & Sync
    // ------------------------------------------------------------------------

    private fun attachCatalogListeners(db: FirebaseFirestore) {
        catalogCatsListener?.remove()
        catalogCatsListener = db.collection(COL_CATEGORIES)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    reconcileCatalogCategoriesSnapshot(db, snapshot, pruneMissing = false)
                }
            }

        productsListener?.remove()
        productsListener = db.collection(COL_PRODUCTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    reconcileProductsSnapshot(db, snapshot, pruneMissing = false)
                }
            }
    }

    private suspend fun reconcileCatalogCategoriesSnapshot(
        db: FirebaseFirestore,
        snapshot: QuerySnapshot,
        pruneMissing: Boolean
    ) {
        catalogSyncMutex.withLock {
            for (change in snapshot.documentChanges) {
                if (change.type == DocumentChange.Type.REMOVED) {
                    val removedId = change.document.getLong("id") ?: change.document.id.toLongOrNull()
                    if (removedId != null) {
                        businessRepository.deleteCategoryById(removedId)
                    }
                }
            }

            if (snapshot.isEmpty) {
                if (!snapshot.metadata.isFromCache && !didAutoSeedCatalogCategories) {
                    didAutoSeedCatalogCategories = true
                    val localCats = businessRepository.getAllCategoriesOnce()
                    val localProds = businessRepository.getAllProductsOnce()
                    if (localCats.isNotEmpty() || localProds.isNotEmpty()) {
                        publishCatalogToCloud(localCats, localProds)
                    }
                }
                return@withLock
            }

            val serverIds = HashSet<Long>()
            for (doc in snapshot.documents) {
                val cat = parseCategoryDoc(doc) ?: continue
                serverIds.add(cat.id)
                val existing = businessRepository.getCategoryByIdOnce(cat.id)
                if (existing == null) {
                    businessRepository.insertCategory(cat)
                } else if (existing != cat) {
                    businessRepository.updateCategory(cat)
                }
            }

            if (pruneMissing && !snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()) {
                businessRepository.getAllCategoriesOnce().forEach { local ->
                    if (local.id !in serverIds) {
                        businessRepository.deleteCategoryById(local.id)
                    }
                }
            }
            markConnectedNow()
        }
    }

    private suspend fun reconcileProductsSnapshot(
        db: FirebaseFirestore,
        snapshot: QuerySnapshot,
        pruneMissing: Boolean
    ) {
        catalogSyncMutex.withLock {
            for (change in snapshot.documentChanges) {
                if (change.type == DocumentChange.Type.REMOVED) {
                    val removedId = change.document.getLong("id") ?: change.document.id.toLongOrNull()
                    if (removedId != null) {
                        businessRepository.deleteProductById(removedId)
                    }
                }
            }

            if (snapshot.isEmpty) {
                if (!snapshot.metadata.isFromCache && !didAutoSeedCatalogProducts) {
                    didAutoSeedCatalogProducts = true
                    val localCats = businessRepository.getAllCategoriesOnce()
                    val localProds = businessRepository.getAllProductsOnce()
                    if (localCats.isNotEmpty() && localProds.isNotEmpty()) {
                        publishCatalogToCloud(localCats, localProds)
                    }
                }
                return@withLock
            }

            val knownCatIds = businessRepository.getAllCategoriesOnce().map { it.id }.toHashSet()
            val serverIds = HashSet<Long>()
            for (doc in snapshot.documents) {
                val product = parseProductDoc(doc) ?: continue
                if (product.categoryId !in knownCatIds) {
                    val ensured = ensureLocalCategoryExists(db, product.categoryId)
                    if (!ensured) continue
                    knownCatIds.add(product.categoryId)
                }
                serverIds.add(product.id)
                val existing = businessRepository.getProductByIdOnce(product.id)
                if (existing == null) {
                    businessRepository.insertProduct(product)
                } else if (existing != product) {
                    businessRepository.updateProduct(product)
                }
            }

            if (pruneMissing && !snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()) {
                businessRepository.getAllProductsOnce().forEach { local ->
                    if (local.id !in serverIds) {
                        businessRepository.deleteProductById(local.id)
                    }
                }
            }
            markConnectedNow()
        }
    }

    private suspend fun ensureLocalCategoryExists(db: FirebaseFirestore, categoryId: Long): Boolean {
        if (businessRepository.getCategoryByIdOnce(categoryId) != null) return true
        return try {
            val snap = db.collection(COL_CATEGORIES).document(categoryId.toString()).get().await()
            val cat = if (snap.exists()) parseCategoryDoc(snap) else null
            if (cat != null) {
                businessRepository.insertCategory(cat)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    // ------------------------------------------------------------------------
    // Real-time Menu (Categories & Items) Listeners & Room Sync
    // ------------------------------------------------------------------------

    private fun attachMenuListeners(db: FirebaseFirestore, syncToLocalRoom: Boolean) {
        menuCatsListener?.remove()
        menuCatsListener = db.collection(COL_MENU_CATEGORIES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncState.value = CloudSyncState.Error(error.localizedMessage ?: "خطأ في مزامنة أقسام المنيو")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        reconcileMenuCategoriesSnapshot(
                            snapshot = snapshot,
                            syncToLocalRoom = syncToLocalRoom,
                            pruneMissing = false
                        )
                    }
                }
            }

        menuItemsListener?.remove()
        menuItemsListener = db.collection(COL_MENU_ITEMS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncState.value = CloudSyncState.Error(error.localizedMessage ?: "خطأ في مزامنة أصناف المنيو")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        reconcileMenuItemsSnapshot(
                            db = db,
                            snapshot = snapshot,
                            syncToLocalRoom = syncToLocalRoom,
                            pruneMissing = false
                        )
                    }
                }
            }
    }

    private suspend fun reconcileMenuCategoriesSnapshot(
        snapshot: QuerySnapshot,
        syncToLocalRoom: Boolean,
        pruneMissing: Boolean
    ) {
        menuSyncMutex.withLock {
            if (syncToLocalRoom) {
                for (change in snapshot.documentChanges) {
                    if (change.type == DocumentChange.Type.REMOVED) {
                        val removedId = change.document.getLong("id") ?: change.document.id.toLongOrNull()
                        if (removedId != null) {
                            businessRepository.deleteMenuCategoryById(removedId)
                        }
                    }
                }
            }

            if (snapshot.isEmpty) {
                val hasRemoval = snapshot.documentChanges.any { it.type == DocumentChange.Type.REMOVED }
                if (hasRemoval || (_cloudMenuCategories.value != null && !snapshot.metadata.isFromCache)) {
                    _cloudMenuCategories.value = emptyList()
                }
                return@withLock
            }

            val cats = snapshot.documents.mapNotNull(::parseMenuCategoryDoc).sortedBy { it.sortOrder }
            _cloudMenuCategories.value = cats

            if (syncToLocalRoom) {
                val serverIds = cats.map { it.id }.toHashSet()
                for (cat in cats) {
                    val existing = businessRepository.getMenuCategoryByIdOnce(cat.id)
                    if (existing == null) {
                        businessRepository.insertMenuCategory(cat)
                    } else if (existing != cat) {
                        businessRepository.updateMenuCategory(cat)
                    }
                }
                if (pruneMissing && !snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()) {
                    businessRepository.getAllMenuCategoriesOnce().forEach { local ->
                        if (local.id !in serverIds) {
                            businessRepository.deleteMenuCategoryById(local.id)
                        }
                    }
                }
            }
            markConnectedNow()
        }
    }

    private suspend fun reconcileMenuItemsSnapshot(
        db: FirebaseFirestore,
        snapshot: QuerySnapshot,
        syncToLocalRoom: Boolean,
        pruneMissing: Boolean
    ) {
        menuSyncMutex.withLock {
            if (syncToLocalRoom) {
                for (change in snapshot.documentChanges) {
                    if (change.type == DocumentChange.Type.REMOVED) {
                        val removedId = change.document.getLong("id") ?: change.document.id.toLongOrNull()
                        if (removedId != null) {
                            businessRepository.deleteMenuItemById(removedId)
                        }
                    }
                }
            }

            if (snapshot.isEmpty) {
                val hasRemoval = snapshot.documentChanges.any { it.type == DocumentChange.Type.REMOVED }
                if (hasRemoval || (_cloudMenuItems.value != null && !snapshot.metadata.isFromCache)) {
                    _cloudMenuItems.value = emptyList()
                }
                return@withLock
            }

            val items = snapshot.documents.mapNotNull(::parseMenuItemDoc).sortedBy { it.sortOrder }
            _cloudMenuItems.value = items

            if (syncToLocalRoom) {
                val knownMenuCatIds = businessRepository.getAllMenuCategoriesOnce().map { it.id }.toHashSet()
                val serverIds = HashSet<Long>()
                for (item in items) {
                    if (item.menuCategoryId !in knownMenuCatIds) {
                        val ensured = ensureLocalMenuCategoryExists(db, item.menuCategoryId)
                        if (!ensured) continue
                        knownMenuCatIds.add(item.menuCategoryId)
                    }
                    serverIds.add(item.id)
                    val existing = businessRepository.getMenuItemByIdOnce(item.id)
                    if (existing == null) {
                        businessRepository.insertMenuItem(item)
                    } else if (existing != item) {
                        businessRepository.updateMenuItem(item)
                    }
                }
                if (pruneMissing && !snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()) {
                    businessRepository.getAllMenuItemsOnce().forEach { local ->
                        if (local.id !in serverIds) {
                            businessRepository.deleteMenuItemById(local.id)
                        }
                    }
                }
            }
            markConnectedNow()
        }
    }

    private suspend fun ensureLocalMenuCategoryExists(db: FirebaseFirestore, menuCategoryId: Long): Boolean {
        if (businessRepository.getMenuCategoryByIdOnce(menuCategoryId) != null) return true
        return try {
            val snap = db.collection(COL_MENU_CATEGORIES).document(menuCategoryId.toString()).get().await()
            val cat = if (snap.exists()) parseMenuCategoryDoc(snap) else null
            if (cat != null) {
                businessRepository.insertMenuCategory(cat)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    // ------------------------------------------------------------------------
    // Real-time Store Settings (Kitchen Status, Offer, Zones, WhatsApp) & Version
    // ------------------------------------------------------------------------

    private fun attachStoreSettingsListener(db: FirebaseFirestore) {
        storeSettingsListener?.remove()
        storeSettingsListener = db.collection(COL_STORE_SETTINGS)
            .document(DOC_STORE_PROFILE)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                applyStoreSettingsSnapshot(snapshot, notifyOnOffer = true)
            }
    }

    private fun applyStoreSettingsSnapshot(snapshot: DocumentSnapshot, notifyOnOffer: Boolean) {
        val phone = snapshot.getString("whatsappNumber")?.trim()
        if (!phone.isNullOrBlank()) {
            _cloudWhatsAppNumber.value = phone
        }
        if (snapshot.contains("kitchenOpen")) {
            _cloudKitchenOpen.value = snapshot.getBoolean("kitchenOpen") ?: true
        }
        if (snapshot.contains("dailyOfferText")) {
            val offer = snapshot.getString("dailyOfferText").orEmpty()
            val offerStamp = snapshot.getLong("dailyOfferUpdatedAt") ?: 0L
            _cloudDailyOffer.value = offer
            if (lastOfferTimestampSeen == -1L) {
                lastOfferTimestampSeen = offerStamp
            } else if (notifyOnOffer && offer.isNotBlank() && offerStamp > lastOfferTimestampSeen) {
                lastOfferTimestampSeen = offerStamp
                OrderNotificationService.showOfferBroadcastNotification(context, offer)
            } else if (offerStamp > lastOfferTimestampSeen) {
                lastOfferTimestampSeen = offerStamp
            }
        }
        val zonesJson = snapshot.getString("deliveryZonesJson")
        if (!zonesJson.isNullOrBlank()) {
            AppPreferences.decodeDeliveryZones(zonesJson)?.let { parsed ->
                _cloudDeliveryZones.value = parsed
            }
        }
        markConnectedNow()
    }

    private fun attachVersionConfigListener(db: FirebaseFirestore) {
        versionConfigListener?.remove()
        versionConfigListener = db.collection(COL_APP_CONFIG)
            .document(DOC_VERSION)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                applyVersionSnapshot(snapshot)
            }
    }

    private fun applyVersionSnapshot(snapshot: DocumentSnapshot): AppVersionConfig {
        val latestCode = (snapshot.getLong("latestVersionCode") ?: 0L).toInt()
        val latestName = snapshot.getString("latestVersionName")?.trim().orEmpty()
        val downloadUrl = snapshot.getString("downloadUrl")?.trim().orEmpty()
        val releaseNotes = snapshot.getString("releaseNotes")?.trim().orEmpty()
        val isMandatory = snapshot.getBoolean("isMandatory") ?: false
        val config = AppVersionConfig(
            latestVersionCode = latestCode,
            latestVersionName = latestName,
            downloadUrl = downloadUrl,
            releaseNotes = releaseNotes,
            isMandatory = isMandatory
        )
        _cloudVersionConfig.value = config
        return config
    }

    // ------------------------------------------------------------------------
    // Real-time Users & Customer Orders Listeners
    // ------------------------------------------------------------------------

    private fun attachAppUsersListener(db: FirebaseFirestore) {
        usersListener?.remove()
        usersListener = db.collection(COL_APP_USERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                applyAppUsersSnapshot(snapshot)
            }
    }

    private fun applyAppUsersSnapshot(snapshot: QuerySnapshot): List<AppUser> {
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
        return users
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
                    val rawItems = (doc.get("lineItems") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
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

    // ------------------------------------------------------------------------
    // Real-time Admin Orders Listener & Reconciliation
    // ------------------------------------------------------------------------

    private fun attachAdminOrdersListener(db: FirebaseFirestore) {
        ordersListener?.remove()
        ordersListener = db.collection(COL_ORDERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    reconcileOrdersSnapshot(snapshot, pruneMissing = true)
                }
            }
    }

    private suspend fun reconcileOrdersSnapshot(
        snapshot: QuerySnapshot,
        pruneMissing: Boolean
    ): Int {
        return ordersSyncMutex.withLock {
            for (change in snapshot.documentChanges) {
                if (change.type == DocumentChange.Type.REMOVED) {
                    val removedCloudId = change.document.getString("cloudId") ?: change.document.id
                    if (removedCloudId !in pendingOrderCloudIds) {
                        orderRepository.getOrderByCloudId(removedCloudId)?.let { localToDelete ->
                            orderRepository.deleteOrder(localToDelete)
                        }
                    }
                }
            }

            val prodMap = businessRepository.getAllProductsOnce().associateBy { it.id }
            val serverCloudIds = HashSet<String>()
            var syncedCount = 0

            for (doc in snapshot.documents) {
                val cloudId = doc.getString("cloudId") ?: doc.id
                serverCloudIds.add(cloudId)
                if (cloudId in pendingOrderCloudIds) continue

                val existing = orderRepository.getOrderByCloudId(cloudId)
                val customerName = doc.getString("customerName") ?: continue
                val customerPhone = doc.getString("customerPhone") ?: ""
                val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                val deliveryZone = doc.getString("deliveryZone") ?: ""
                val shippingFee = doc.getDouble("shippingFee") ?: 0.0
                val userId = doc.getString("userId") ?: ""
                val notes = doc.getString("notes") ?: ""
                val summary = doc.getString("itemsDescription") ?: ""
                val remoteStatus = doc.getString("status") ?: OrderStatus.PENDING_REVIEW.name
                val remotePayment = doc.getString("paymentStatus") ?: PaymentStatus.UNPAID.name
                val remotePaidAmount = doc.getDouble("paidAmount") ?: 0.0
                val remoteSource = doc.getString("orderSource") ?: OrderSource.CUSTOMER.name
                val remoteRated = doc.getBoolean("isRated") ?: false
                val extraPrepCost = (doc.getDouble("extraPrepCost") ?: 0.0).coerceAtLeast(0.0)
                val extraPrepNotes = doc.getString("extraPrepNotes").orEmpty()
                val orderDate = doc.getLong("orderDate") ?: System.currentTimeMillis()
                val deliveryDate = doc.getLong("deliveryDate") ?: 0L
                val ym = doc.getString("yearMonth") ?: DateUtils.getYearMonthFromTimestamp(orderDate)

                val rawItems = (doc.get("lineItems") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
                val lineEntities = rawItems.map { map ->
                    val productId = (map["productId"] as? Number)?.toLong()
                    val qty = (map["quantity"] as? Number)?.toDouble() ?: 1.0
                    val unitSell = (map["unitSellingPrice"] as? Number)?.toDouble() ?: 0.0
                    val isWholeQtyCost = (map["isTotalCostForWholeQty"] as? Boolean) ?: false
                    val explicitUnitPurchase = (map["unitPurchaseCost"] as? Number)?.toDouble()
                    val unitPurchase = explicitUnitPurchase ?: (productId?.let { prodMap[it]?.purchaseCost } ?: 0.0)
                    val totalSell = (map["totalSellingPrice"] as? Number)?.toDouble() ?: (qty * unitSell)
                    val explicitTotalCost = (map["totalPurchaseCost"] as? Number)?.toDouble()
                    val totalCost = explicitTotalCost ?: if (isWholeQtyCost) unitPurchase else (qty * unitPurchase)
                    OrderLineItemEntity(
                        orderId = existing?.id ?: 0L,
                        productId = productId,
                        productNameSnapshot = (map["productName"] as? String).orEmpty(),
                        productImagePathSnapshot = null,
                        quantity = qty,
                        unitPurchaseCost = unitPurchase,
                        unitSellingPrice = unitSell,
                        totalPurchaseCost = totalCost,
                        totalSellingPrice = totalSell,
                        itemGrossProfit = totalSell - totalCost,
                        isTotalCostForWholeQty = isWholeQtyCost
                    )
                }

                val itemsCostSum = lineEntities.sumOf { it.totalPurchaseCost }
                val totalCost = doc.getDouble("costPrice") ?: (itemsCostSum + extraPrepCost)
                val rawSalePrice = doc.getDouble("salePrice") ?: 0.0
                val itemsRevenue = if (doc.contains("itemsSalePrice")) {
                    doc.getDouble("itemsSalePrice") ?: 0.0
                } else {
                    (rawSalePrice - shippingFee).coerceAtLeast(0.0)
                }

                if (existing != null) {
                    val updatedLocal = existing.copy(
                        customerName = customerName,
                        customerPhone = customerPhone,
                        itemsDescription = summary.ifBlank { existing.itemsDescription },
                        salePrice = itemsRevenue,
                        costPrice = totalCost,
                        netProfit = itemsRevenue - totalCost,
                        orderDate = orderDate,
                        deliveryDate = deliveryDate,
                        yearMonth = ym,
                        status = remoteStatus,
                        paymentStatus = remotePayment,
                        paidAmount = remotePaidAmount,
                        notes = notes,
                        deliveryAddress = deliveryAddress,
                        deliveryZone = deliveryZone,
                        shippingFee = shippingFee,
                        isRated = remoteRated,
                        extraPrepCost = extraPrepCost,
                        extraPrepNotes = extraPrepNotes
                    )
                    val existingItems = if (lineEntities.isNotEmpty()) {
                        orderRepository.getLineItemsForOrder(existing.id)
                    } else {
                        emptyList()
                    }
                    val linesChanged = lineEntities.isNotEmpty() && !areLineItemsEquivalent(existingItems, lineEntities)
                    if (updatedLocal != existing || linesChanged) {
                        if (lineEntities.isNotEmpty()) {
                            orderRepository.saveOrderWithItems(updatedLocal, lineEntities)
                        } else {
                            orderRepository.updateOrder(updatedLocal)
                        }
                    }
                } else {
                    val newOrder = Order(
                        customerName = customerName,
                        customerPhone = customerPhone,
                        itemsDescription = summary,
                        salePrice = itemsRevenue,
                        costPrice = totalCost,
                        netProfit = itemsRevenue - totalCost,
                        orderDate = orderDate,
                        deliveryDate = deliveryDate,
                        yearMonth = ym,
                        status = remoteStatus,
                        paymentStatus = remotePayment,
                        paidAmount = remotePaidAmount,
                        notes = notes,
                        deliveryAddress = deliveryAddress,
                        orderSource = remoteSource,
                        cloudId = cloudId,
                        userId = userId,
                        deliveryZone = deliveryZone,
                        shippingFee = shippingFee,
                        isRated = remoteRated,
                        extraPrepCost = extraPrepCost,
                        extraPrepNotes = extraPrepNotes
                    )
                    orderRepository.saveOrderWithItems(newOrder, lineEntities)
                }
                syncedCount++
            }

            if (pruneMissing && !snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()) {
                val localOrders = orderRepository.getAllOrdersOnce()
                for (local in localOrders) {
                    if (local.cloudId.isNotBlank() &&
                        local.cloudId !in serverCloudIds &&
                        local.cloudId !in pendingOrderCloudIds
                    ) {
                        orderRepository.deleteOrder(local)
                    }
                }
            }

            markConnectedNow()
            syncedCount
        }
    }

    private fun areLineItemsEquivalent(
        local: List<OrderLineItemEntity>,
        remote: List<OrderLineItemEntity>
    ): Boolean {
        if (local.size != remote.size) return false
        for (i in local.indices) {
            val a = local[i]
            val b = remote[i]
            if (a.productId != b.productId ||
                a.productNameSnapshot != b.productNameSnapshot ||
                a.quantity != b.quantity ||
                a.unitPurchaseCost != b.unitPurchaseCost ||
                a.unitSellingPrice != b.unitSellingPrice ||
                a.totalPurchaseCost != b.totalPurchaseCost ||
                a.totalSellingPrice != b.totalSellingPrice ||
                a.isTotalCostForWholeQty != b.isTotalCostForWholeQty
            ) {
                return false
            }
        }
        return true
    }

    // ------------------------------------------------------------------------
    // Real-time Treasury Listener & Reconciliation
    // ------------------------------------------------------------------------

    private fun attachTreasuryListener(db: FirebaseFirestore) {
        treasuryListener?.remove()
        treasuryListener = db.collection(COL_TREASURY)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    reconcileTreasurySnapshot(snapshot, pruneMissing = true)
                }
            }
    }

    private suspend fun reconcileTreasurySnapshot(
        snapshot: QuerySnapshot,
        pruneMissing: Boolean
    ) {
        treasurySyncMutex.withLock {
            for (change in snapshot.documentChanges) {
                if (change.type == DocumentChange.Type.REMOVED) {
                    val removedId = change.document.getString("cloudId") ?: change.document.id
                    if (removedId !in pendingTreasuryCloudIds) {
                        orderRepository.getTreasuryTxByCloudId(removedId)?.let {
                            orderRepository.deleteTreasuryTransaction(it)
                        }
                    }
                }
            }

            val serverIds = HashSet<String>()
            for (doc in snapshot.documents) {
                val cid = doc.getString("cloudId") ?: doc.id
                serverIds.add(cid)
                if (cid in pendingTreasuryCloudIds) continue

                val amount = doc.getDouble("amount") ?: continue
                val title = doc.getString("title") ?: continue
                val type = doc.getString("type") ?: TreasuryTxType.HOME_LOAN_IN.name
                val notes = doc.getString("notes").orEmpty()
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                val ym = doc.getString("yearMonth") ?: DateUtils.getYearMonthFromTimestamp(createdAt)

                val existing = orderRepository.getTreasuryTxByCloudId(cid)
                if (existing == null) {
                    orderRepository.insertTreasuryTransaction(
                        TreasuryTransactionEntity(
                            cloudId = cid,
                            type = type,
                            amount = amount,
                            title = title,
                            notes = notes,
                            createdAt = createdAt,
                            yearMonth = ym
                        )
                    )
                } else {
                    val updated = existing.copy(
                        type = type,
                        amount = amount,
                        title = title,
                        notes = notes,
                        createdAt = createdAt,
                        yearMonth = ym
                    )
                    if (updated != existing) {
                        orderRepository.updateTreasuryTransaction(updated)
                    }
                }
            }

            if (pruneMissing && !snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()) {
                orderRepository.getAllTreasuryTransactionsOnce().forEach { local ->
                    if (local.cloudId.isNotBlank() &&
                        local.cloudId !in serverIds &&
                        local.cloudId !in pendingTreasuryCloudIds
                    ) {
                        orderRepository.deleteTreasuryTransaction(local)
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Emergency Force Re-Sync (Server Authoritative Pull + Merge + Re-attach)
    // ------------------------------------------------------------------------

    suspend fun forceFullResyncFromCloud(): Result<ForceResyncReport> {
        val cfg = activeConfig ?: CloudConfig()
        val db = firestore ?: try {
            val initialized = getOrInitFirestore(context, cfg)
            firestore = initialized
            activeConfig = cfg
            initialized
        } catch (e: Exception) {
            _syncState.value = CloudSyncState.Error(e.localizedMessage ?: "تعذر الاتصال بالسحابة")
            return Result.failure(e)
        }

        _syncState.value = CloudSyncState.Connecting
        return try {
            // 1. Store settings & version config
            val settingsSnap = db.collection(COL_STORE_SETTINGS)
                .document(DOC_STORE_PROFILE)
                .get(Source.SERVER)
                .await()
            if (settingsSnap.exists()) {
                applyStoreSettingsSnapshot(settingsSnap, notifyOnOffer = false)
            }

            val versionSnap = db.collection(COL_APP_CONFIG)
                .document(DOC_VERSION)
                .get(Source.SERVER)
                .await()
            if (versionSnap.exists()) {
                applyVersionSnapshot(versionSnap)
            }

            // 2. Catalog categories & products
            val catSnap = db.collection(COL_CATEGORIES).get(Source.SERVER).await()
            val prodSnap = db.collection(COL_PRODUCTS).get(Source.SERVER).await()
            if (catSnap.isEmpty && prodSnap.isEmpty) {
                val localCats = businessRepository.getAllCategoriesOnce()
                val localProds = businessRepository.getAllProductsOnce()
                if (localCats.isNotEmpty() || localProds.isNotEmpty()) {
                    publishCatalogToCloud(localCats, localProds)
                }
            } else {
                reconcileCatalogCategoriesSnapshot(db, catSnap, pruneMissing = !catSnap.isEmpty)
                reconcileProductsSnapshot(db, prodSnap, pruneMissing = !prodSnap.isEmpty)
            }

            // 3. Menu categories & items
            val menuCatSnap = db.collection(COL_MENU_CATEGORIES).get(Source.SERVER).await()
            val menuItemSnap = db.collection(COL_MENU_ITEMS).get(Source.SERVER).await()
            if (menuCatSnap.isEmpty && menuItemSnap.isEmpty) {
                val localMenuCats = businessRepository.getAllMenuCategoriesOnce()
                val localMenuItems = businessRepository.getAllMenuItemsOnce()
                if (localMenuCats.isNotEmpty() || localMenuItems.isNotEmpty()) {
                    publishMenuToCloud(localMenuCats, localMenuItems)
                }
            } else {
                reconcileMenuCategoriesSnapshot(
                    snapshot = menuCatSnap,
                    syncToLocalRoom = activeEnableAdminListeners,
                    pruneMissing = !menuCatSnap.isEmpty
                )
                reconcileMenuItemsSnapshot(
                    db = db,
                    snapshot = menuItemSnap,
                    syncToLocalRoom = activeEnableAdminListeners,
                    pruneMissing = !menuItemSnap.isEmpty
                )
            }

            // 4. Push any legacy local-only orders (blank cloudId) before pulling server state
            val localOnlyOrders = orderRepository.getAllOrdersOnce().filter { it.cloudId.isBlank() }
            for (localOrder in localOnlyOrders) {
                val generatedId = UUID.randomUUID().toString()
                val items = orderRepository.getLineItemsForOrder(localOrder.id)
                val updated = localOrder.copy(cloudId = generatedId)
                orderRepository.updateOrder(updated)
                pushAdminOrderToCloud(updated, items)
            }

            // Pull & reconcile orders
            val ordersSnap = db.collection(COL_ORDERS).get(Source.SERVER).await()
            val syncedOrders = reconcileOrdersSnapshot(ordersSnap, pruneMissing = true)

            // 5. Treasury transactions
            val localOnlyTreasury = orderRepository.getAllTreasuryTransactionsOnce().filter { it.cloudId.isBlank() }
            for (tx in localOnlyTreasury) {
                val generatedId = UUID.randomUUID().toString()
                val updatedTx = tx.copy(cloudId = generatedId)
                orderRepository.updateTreasuryTransaction(updatedTx)
                pushTreasuryTransactionToCloud(updatedTx)
            }
            val treasurySnap = db.collection(COL_TREASURY).get(Source.SERVER).await()
            reconcileTreasurySnapshot(treasurySnap, pruneMissing = true)

            // 6. Registered app users
            val usersSnap = db.collection(COL_APP_USERS).get(Source.SERVER).await()
            val users = applyAppUsersSnapshot(usersSnap)

            // 7. Re-attach fresh real-time listeners
            disconnectListenersOnly()
            attachAllConfiguredListeners(
                db = db,
                enableAdminListeners = activeEnableAdminListeners,
                currentUserId = activeTrackedUserId
            )

            val productsCount = businessRepository.getAllProductsOnce().size
            val menuItemsCount = businessRepository.getAllMenuItemsOnce().size
            _syncState.value = CloudSyncState.Connected(cfg.projectId)

            Result.success(
                ForceResyncReport(
                    ordersSynced = syncedOrders,
                    productsSynced = productsCount,
                    menuItemsSynced = menuItemsCount,
                    usersSynced = users.size
                )
            )
        } catch (e: Exception) {
            _syncState.value = CloudSyncState.Error(e.localizedMessage ?: "فشلت إعادة المزامنة الشاملة")
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------------
    // Catalog Cloud CRUD
    // ------------------------------------------------------------------------

    suspend fun pushCategoryToCloud(category: CategoryEntity): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        if (category.id <= 0L) return Result.success(Unit)
        return try {
            db.collection(COL_CATEGORIES)
                .document(category.id.toString())
                .set(
                    mapOf(
                        "id" to category.id,
                        "name" to category.name,
                        "sortOrder" to category.sortOrder,
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

    suspend fun deleteCategoryFromCloud(categoryId: Long, productIds: List<Long> = emptyList()): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        if (categoryId <= 0L) return Result.success(Unit)
        return try {
            val batch = db.batch()
            productIds.filter { it > 0L }.forEach { pid ->
                batch.delete(db.collection(COL_PRODUCTS).document(pid.toString()))
            }
            batch.delete(db.collection(COL_CATEGORIES).document(categoryId.toString()))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushProductToCloud(product: ProductEntity): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        if (product.id <= 0L) return Result.success(Unit)
        return try {
            db.collection(COL_PRODUCTS)
                .document(product.id.toString())
                .set(
                    mapOf(
                        "id" to product.id,
                        "name" to product.name,
                        "categoryId" to product.categoryId,
                        "unit" to product.unit,
                        "description" to product.description,
                        "purchaseCost" to product.purchaseCost,
                        "defaultSellingPrice" to product.defaultSellingPrice,
                        "isActive" to product.isActive,
                        "sortOrder" to product.sortOrder,
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

    suspend fun deleteProductFromCloud(productId: Long): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        if (productId <= 0L) return Result.success(Unit)
        return try {
            db.collection(COL_PRODUCTS).document(productId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishCatalogToCloud(
        categories: List<CategoryEntity>,
        products: List<ProductEntity>
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("الربط السحابي غير مفعّل"))
        return try {
            val batch = db.batch()
            val now = System.currentTimeMillis()
            categories.filter { it.id > 0L }.forEach { cat ->
                val ref = db.collection(COL_CATEGORIES).document(cat.id.toString())
                batch.set(
                    ref,
                    mapOf(
                        "id" to cat.id,
                        "name" to cat.name,
                        "sortOrder" to cat.sortOrder,
                        "updatedAt" to now
                    ),
                    SetOptions.merge()
                )
            }
            products.filter { it.id > 0L }.forEach { prod ->
                val ref = db.collection(COL_PRODUCTS).document(prod.id.toString())
                batch.set(
                    ref,
                    mapOf(
                        "id" to prod.id,
                        "name" to prod.name,
                        "categoryId" to prod.categoryId,
                        "unit" to prod.unit,
                        "description" to prod.description,
                        "purchaseCost" to prod.purchaseCost,
                        "defaultSellingPrice" to prod.defaultSellingPrice,
                        "isActive" to prod.isActive,
                        "sortOrder" to prod.sortOrder,
                        "updatedAt" to now
                    ),
                    SetOptions.merge()
                )
            }
            batch.commit().await()
            markConnectedNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMenuCategoryFromCloud(categoryId: Long, itemIds: List<Long> = emptyList()): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        if (categoryId <= 0L) return Result.success(Unit)
        return try {
            val batch = db.batch()
            itemIds.filter { it > 0L }.forEach { itemId ->
                batch.delete(db.collection(COL_MENU_ITEMS).document(itemId.toString()))
            }
            batch.delete(db.collection(COL_MENU_CATEGORIES).document(categoryId.toString()))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMenuItemFromCloud(itemId: Long): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        if (itemId <= 0L) return Result.success(Unit)
        return try {
            db.collection(COL_MENU_ITEMS).document(itemId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------------
    // Treasury Cloud CRUD
    // ------------------------------------------------------------------------

    suspend fun pushTreasuryTransactionToCloud(tx: TreasuryTransactionEntity): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        if (tx.cloudId.isBlank()) return Result.success(Unit)
        pendingTreasuryCloudIds.add(tx.cloudId)
        return try {
            db.collection(COL_TREASURY)
                .document(tx.cloudId)
                .set(
                    mapOf(
                        "cloudId" to tx.cloudId,
                        "type" to tx.type,
                        "amount" to tx.amount,
                        "title" to tx.title,
                        "notes" to tx.notes,
                        "createdAt" to tx.createdAt,
                        "yearMonth" to tx.yearMonth
                    ),
                    SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            pendingTreasuryCloudIds.remove(tx.cloudId)
        }
    }

    suspend fun deleteTreasuryTransactionFromCloud(cloudId: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        if (cloudId.isBlank()) return Result.success(Unit)
        return try {
            db.collection(COL_TREASURY).document(cloudId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------------
    // Users Cloud CRUD
    // ------------------------------------------------------------------------

    suspend fun deleteAppUserFromCloud(userId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("غير متصل بالسحابة"))
        if (userId.isBlank()) return Result.success(Unit)
        return try {
            db.collection(COL_APP_USERS).document(userId).delete().await()
            _cloudAppUsers.value = _cloudAppUsers.value.filterNot { it.userId == userId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
                .update(
                    mapOf(
                        "isOnline" to isOnline,
                        "lastSeen" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (_: Exception) {
        }
    }

    suspend fun incrementUserOrdersCount(userId: String) {
        if (userId.isBlank()) return
        val db = firestore ?: return
        try {
            db.collection(COL_APP_USERS)
                .document(userId)
                .update(
                    mapOf(
                        "ordersCount" to FieldValue.increment(1L),
                        "lastSeen" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (_: Exception) {
        }
    }

    // ------------------------------------------------------------------------
    // Store Settings & Menu Publishing
    // ------------------------------------------------------------------------

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
            val now = System.currentTimeMillis()
            val payload = mutableMapOf<String, Any>(
                "dailyOfferText" to offerText.trim(),
                "updatedAt" to now
            )
            if (offerText.trim().isNotBlank()) {
                payload["dailyOfferUpdatedAt"] = now
            }
            db.collection(COL_STORE_SETTINGS)
                .document(DOC_STORE_PROFILE)
                .set(payload, SetOptions.merge())
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
                        "photos" to item.photoList()
                            .filter { it.isNotBlank() && !it.startsWith("content://", ignoreCase = true) }
                            .take(3),
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
            markConnectedNow()
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = CloudSyncState.Error(e.localizedMessage ?: "فشل نشر المنيو للسحابة")
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------------
    // Orders Cloud CRUD
    // ------------------------------------------------------------------------

    suspend fun pushAdminOrderToCloud(
        order: Order,
        lineItems: List<OrderLineItemEntity>
    ): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        val docId = order.cloudId.ifBlank { return Result.success(Unit) }
        pendingOrderCloudIds.add(docId)
        return try {
            val payload = hashMapOf<String, Any>(
                "cloudId" to docId,
                "userId" to order.userId,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "deliveryZone" to order.deliveryZone,
                "shippingFee" to order.shippingFee,
                "deliveryAddress" to order.deliveryAddress,
                "notes" to order.notes,
                "itemsDescription" to order.itemsDescription,
                "itemsSalePrice" to order.salePrice,
                "salePrice" to (order.salePrice + order.shippingFee),
                "itemsCostPrice" to order.itemsCostPrice,
                "extraPrepCost" to order.extraPrepCost,
                "extraPrepNotes" to order.extraPrepNotes,
                "costPrice" to order.costPrice,
                "netProfit" to order.netProfit,
                "orderDate" to order.orderDate,
                "deliveryDate" to order.deliveryDate,
                "yearMonth" to order.yearMonth,
                "status" to order.status,
                "statusLabel" to OrderStatus.trackingTitle(order.status),
                "paymentStatus" to order.paymentStatus,
                "paidAmount" to order.paidAmount,
                "orderSource" to order.orderSource,
                "isRated" to order.isRated,
                "updatedAt" to System.currentTimeMillis(),
                "lineItems" to lineItems.map { li ->
                    mapOf(
                        "productId" to li.productId,
                        "productName" to li.productNameSnapshot,
                        "quantity" to li.quantity,
                        "unitPurchaseCost" to li.unitPurchaseCost,
                        "unitSellingPrice" to li.unitSellingPrice,
                        "totalPurchaseCost" to li.totalPurchaseCost,
                        "totalSellingPrice" to li.totalSellingPrice,
                        "itemGrossProfit" to li.itemGrossProfit,
                        "isTotalCostForWholeQty" to li.isTotalCostForWholeQty
                    )
                }
            )
            db.collection(COL_ORDERS)
                .document(docId)
                .set(payload, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            pendingOrderCloudIds.remove(docId)
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
        val docId = cloudId.ifBlank { UUID.randomUUID().toString() }
        pendingOrderCloudIds.add(docId)
        return try {
            val summaryDesc = cartItems.joinToString("، ") { item ->
                val q = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
                "$q × ${item.menuItem.displayName}"
            }
            val payload = hashMapOf(
                "cloudId" to docId,
                "userId" to userId,
                "customerName" to customerName,
                "customerPhone" to customerPhone,
                "deliveryZone" to deliveryZone,
                "shippingFee" to shippingFee,
                "deliveryAddress" to deliveryAddress,
                "notes" to notes,
                "itemsDescription" to summaryDesc,
                "itemsSalePrice" to totalSalePrice,
                "salePrice" to (totalSalePrice + shippingFee),
                "orderDate" to orderDate,
                "deliveryDate" to 0L,
                "yearMonth" to yearMonth,
                "status" to OrderStatus.PENDING_REVIEW.name,
                "statusLabel" to OrderStatus.trackingTitle(OrderStatus.PENDING_REVIEW.name),
                "paymentStatus" to PaymentStatus.UNPAID.name,
                "paidAmount" to 0.0,
                "orderSource" to OrderSource.CUSTOMER.name,
                "extraPrepCost" to 0.0,
                "extraPrepNotes" to "",
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
                .document(docId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            pendingOrderCloudIds.remove(docId)
        }
    }

    suspend fun updateOrderStatusInCloud(cloudId: String, newStatus: String): Result<Unit> {
        if (cloudId.isBlank()) return Result.success(Unit)
        val db = firestore ?: return Result.success(Unit)
        pendingOrderCloudIds.add(cloudId)
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
        } finally {
            pendingOrderCloudIds.remove(cloudId)
        }
    }

    suspend fun updateOrderPaymentInCloud(cloudId: String, paymentStatus: String, paidAmount: Double): Result<Unit> {
        if (cloudId.isBlank()) return Result.success(Unit)
        val db = firestore ?: return Result.success(Unit)
        pendingOrderCloudIds.add(cloudId)
        return try {
            db.collection(COL_ORDERS)
                .document(cloudId)
                .set(
                    mapOf(
                        "paymentStatus" to paymentStatus,
                        "paidAmount" to paidAmount,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            pendingOrderCloudIds.remove(cloudId)
        }
    }

    suspend fun deleteOrderFromCloud(cloudId: String): Result<Unit> {
        if (cloudId.isBlank()) return Result.success(Unit)
        val db = firestore ?: return Result.success(Unit)
        pendingOrderCloudIds.add(cloudId)
        return try {
            db.collection(COL_ORDERS)
                .document(cloudId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            pendingOrderCloudIds.remove(cloudId)
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

    suspend fun publishAppVersionConfig(config: AppVersionConfig): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("الربط السحابي غير مفعّل"))
        return try {
            db.collection(COL_APP_CONFIG)
                .document(DOC_VERSION)
                .set(
                    mapOf(
                        "latestVersionCode" to config.latestVersionCode,
                        "latestVersionName" to config.latestVersionName.trim(),
                        "downloadUrl" to config.downloadUrl.trim(),
                        "releaseNotes" to config.releaseNotes.trim(),
                        "isMandatory" to config.isMandatory,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            _cloudVersionConfig.value = config
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchVersionConfigOnce(): Result<AppVersionConfig?> {
        val db = firestore ?: return Result.failure(IllegalStateException("تعذر الاتصال بالسحابة حالياً"))
        return try {
            val snapshot = db.collection(COL_APP_CONFIG)
                .document(DOC_VERSION)
                .get()
                .await()
            if (!snapshot.exists()) {
                return Result.success(null)
            }
            Result.success(applyVersionSnapshot(snapshot))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------------
    // Helpers & Document Parsers
    // ------------------------------------------------------------------------

    private fun markConnectedNow() {
        activeConfig?.let {
            _syncState.value = CloudSyncState.Connected(it.projectId)
        }
    }

    private fun parseCategoryDoc(doc: DocumentSnapshot): CategoryEntity? {
        val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return null
        val name = doc.getString("name")?.trim().orEmpty()
        if (name.isBlank()) return null
        return CategoryEntity(
            id = id,
            name = name,
            sortOrder = (doc.getLong("sortOrder") ?: 0L).toInt()
        )
    }

    private fun parseProductDoc(doc: DocumentSnapshot): ProductEntity? {
        val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return null
        val name = doc.getString("name")?.trim().orEmpty()
        val categoryId = doc.getLong("categoryId") ?: return null
        if (name.isBlank()) return null
        return ProductEntity(
            id = id,
            name = name,
            categoryId = categoryId,
            unit = doc.getString("unit") ?: "كيلو",
            description = doc.getString("description").orEmpty(),
            purchaseCost = doc.getDouble("purchaseCost") ?: 0.0,
            defaultSellingPrice = doc.getDouble("defaultSellingPrice") ?: 0.0,
            isActive = doc.getBoolean("isActive") ?: true,
            sortOrder = (doc.getLong("sortOrder") ?: 0L).toInt()
        )
    }

    private fun parseMenuCategoryDoc(doc: DocumentSnapshot): MenuCategoryEntity? {
        val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return null
        return MenuCategoryEntity(
            id = id,
            name = doc.getString("name") ?: "",
            sortOrder = (doc.getLong("sortOrder") ?: 0L).toInt(),
            isVisible = doc.getBoolean("isVisible") ?: true
        )
    }

    private fun parseMenuItemDoc(doc: DocumentSnapshot): MenuItemEntity? {
        val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return null
        val rawPhotos = (doc.get("photos") as? List<*>)
            ?.mapNotNull { it as? String }
            ?.filter { it.isNotBlank() && !it.startsWith("content://", ignoreCase = true) }
            ?.take(3)
            ?: emptyList()
        return MenuItemEntity(
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
    }

    companion object {
        const val CLOUD_APP_NAME = "akleh_helwa_cloud"
        const val COL_CATEGORIES = "catalog_categories"
        const val COL_PRODUCTS = "products"
        const val COL_MENU_CATEGORIES = "menu_categories"
        const val COL_MENU_ITEMS = "menu_items"
        const val COL_ORDERS = "orders"
        const val COL_APP_USERS = "app_users"
        const val COL_TREASURY = "treasury_transactions"
        const val COL_STORE_SETTINGS = "store_settings"
        const val DOC_STORE_PROFILE = "profile"
        const val COL_APP_CONFIG = "app_config"
        const val DOC_VERSION = "version"

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
