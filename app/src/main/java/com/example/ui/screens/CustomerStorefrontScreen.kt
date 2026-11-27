package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.model.CloudSyncState
import com.example.model.CustomerCartItem
import com.example.model.CustomerProfile
import com.example.model.DeliveryZone
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import com.example.model.TrackedCloudOrder
import com.example.ui.components.AdminPinDialog
import com.example.ui.components.AnimatedCurrencyCounter
import com.example.ui.components.CulinaryPillChip
import com.example.ui.components.CulinaryQuantityStepper
import com.example.ui.components.DishPhotoCarousel
import com.example.ui.components.LuxuryCard
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.StaggeredAppearItem
import com.example.ui.components.ThemeToggleIconButton
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import com.example.util.WhatsAppUtils
import kotlinx.coroutines.delay
import java.util.Locale

private const val FILTER_ALL = -1L
private const val FILTER_FAVORITES = -2L

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomerStorefrontScreen(
    categories: List<MenuCategoryEntity>,
    items: List<MenuItemEntity>,
    primaryImages: Map<Long, String>,
    cartItems: List<CustomerCartItem>,
    customerProfile: CustomerProfile,
    favoriteIds: Set<Long>,
    hasPreviousOrder: Boolean,
    kitchenOpen: Boolean,
    dailyOfferText: String,
    deliveryZones: List<DeliveryZone>,
    trackedOrders: List<TrackedCloudOrder>,
    isDarkMode: Boolean,
    adminPin: String,
    cloudSyncState: CloudSyncState,
    uiMessage: String? = null,
    onClearUiMessage: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    onToggleTheme: () -> Unit,
    onEnterAdmin: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onReorderLastOrder: () -> Unit,
    onReorderFromTrackedOrder: (TrackedCloudOrder) -> Unit,
    onSubmitOrderRating: (TrackedCloudOrder, Int) -> Unit,
    onAddToCart: (MenuItemEntity, String?, Double) -> Unit,
    onUpdateCartQuantity: (Long, Double) -> Unit,
    onClearCart: () -> Unit,
    onSubmitOrder: (
        customerName: String,
        customerPhone: String,
        deliveryZone: DeliveryZone,
        deliveryAddress: String,
        notes: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }
    var selectedFilterId by remember { mutableStateOf(FILTER_ALL) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var selectedDishForSheet by remember { mutableStateOf<MenuItemEntity?>(null) }
    var showMyOrdersSheet by remember { mutableStateOf(false) }
    var showInfoAndUpdatesSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiMessage) {
        val msg = uiMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onClearUiMessage()
        }
    }

    val activeCategories by remember(categories, items) {
        derivedStateOf {
            categories
                .filter { cat -> items.any { it.menuCategoryId == cat.id } }
                .distinctBy { it.name.trim() }
        }
    }

    LaunchedEffect(activeCategories) {
        if (selectedFilterId > 0L && activeCategories.none { it.id == selectedFilterId }) {
            selectedFilterId = FILTER_ALL
        }
    }

    val categoryMap by remember(activeCategories) {
        derivedStateOf { activeCategories.associateBy { it.id } }
    }

    val popularItems by remember(items) {
        derivedStateOf { items.filter { it.isPopular } }
    }

    val filteredItems by remember(items, searchQuery, selectedFilterId, favoriteIds) {
        derivedStateOf {
            items.filter { item ->
                val matchesCat = when (selectedFilterId) {
                    FILTER_ALL -> true
                    FILTER_FAVORITES -> item.id in favoriteIds
                    else -> item.menuCategoryId == selectedFilterId
                }
                val matchesQuery = searchQuery.isBlank() ||
                    item.displayName.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
                matchesCat && matchesQuery
            }
        }
    }

    val cartQuantityMap by remember(cartItems) {
        derivedStateOf { cartItems.associate { it.menuItem.id to it.quantity } }
    }
    val cartTotal by remember(cartItems) {
        derivedStateOf { cartItems.sumOf { it.lineTotal } }
    }
    val cartLinesCount by remember(cartItems) {
        derivedStateOf { cartItems.size }
    }
    val cartUnitsSignature by remember(cartItems) {
        derivedStateOf { cartItems.sumOf { it.quantity } }
    }

    val isHeaderCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 70
        }
    }

    val heroCollapseProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 340f).coerceIn(0f, 1f)
        }
    }

    val resolveItemPhotos: (MenuItemEntity) -> List<String> = { item ->
        val own = item.photoList()
        if (own.isNotEmpty()) own
        else listOfNotNull(item.productId?.let { primaryImages[it] })
    }

    if (showAdminPinDialog) {
        AdminPinDialog(
            expectedPin = adminPin,
            onUnlocked = {
                showAdminPinDialog = false
                onEnterAdmin()
            },
            onDismiss = { showAdminPinDialog = false }
        )
    }

    selectedDishForSheet?.let { dish ->
        val photos = resolveItemPhotos(dish)
        val catName = categoryMap[dish.menuCategoryId]?.name.orEmpty()
        DishDetailsBottomSheet(
            menuItem = dish,
            categoryName = catName,
            photos = photos,
            isFavorite = dish.id in favoriteIds,
            onToggleFavorite = { onToggleFavorite(dish.id) },
            onAddQuantityToCart = { qty ->
                onAddToCart(dish, photos.firstOrNull(), qty)
            },
            onDismiss = { selectedDishForSheet = null }
        )
    }

    if (showMyOrdersSheet) {
        CustomerOrdersTrackingSheet(
            orders = trackedOrders,
            onSubmitRating = onSubmitOrderRating,
            onReorder = onReorderFromTrackedOrder,
            onDismiss = { showMyOrdersSheet = false }
        )
    }

    if (showInfoAndUpdatesSheet) {
        CustomerAccountInfoSheet(
            customerProfile = customerProfile,
            cloudSyncState = cloudSyncState,
            trackedOrdersCount = trackedOrders.size,
            onCheckForUpdates = {
                showInfoAndUpdatesSheet = false
                onCheckForUpdates()
            },
            onOpenOrders = {
                showInfoAndUpdatesSheet = false
                showMyOrdersSheet = true
            },
            onOpenAdmin = {
                showInfoAndUpdatesSheet = false
                showAdminPinDialog = true
            },
            onDismiss = { showInfoAndUpdatesSheet = false }
        )
    }

    if (showCheckoutDialog) {
        CustomerCheckoutSheet(
            cartItems = cartItems,
            itemsSubtotal = cartTotal,
            initialName = customerProfile.username,
            initialPhone = customerProfile.phone,
            initialAddress = customerProfile.address,
            deliveryZones = deliveryZones.ifEmpty { DeliveryZone.DEFAULT_ZONES },
            onUpdateQuantity = onUpdateCartQuantity,
            onClearCart = {
                onClearCart()
                showCheckoutDialog = false
            },
            onConfirmSubmit = { name, phone, zone, address, notes ->
                onSubmitOrder(name, phone, zone, address, notes)
                showCheckoutDialog = false
            },
            onDismiss = { showCheckoutDialog = false }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundGradient)
            .testTag("customer_storefront_screen"),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            StorefrontTopBar(
                kitchenOpen = kitchenOpen,
                isDarkMode = isDarkMode,
                isCollapsed = isHeaderCollapsed,
                searchExpanded = searchExpanded || searchQuery.isNotEmpty(),
                searchQuery = searchQuery,
                trackedOrdersCount = trackedOrders.size,
                onSearchQueryChange = { searchQuery = it },
                onToggleSearch = {
                    if (searchExpanded && searchQuery.isNotEmpty()) {
                        searchQuery = ""
                    }
                    searchExpanded = !searchExpanded
                },
                onToggleTheme = onToggleTheme,
                onOpenOrders = { showMyOrdersSheet = true },
                onOpenInfo = { showInfoAndUpdatesSheet = true },
                onOpenAdmin = { showAdminPinDialog = true }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundGradient),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWideLayout = maxWidth >= 640.dp
            val popularListState = rememberLazyListState()
            val popularSnapBehavior = rememberSnapFlingBehavior(lazyListState = popularListState)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .widthIn(max = 860.dp)
                    .fillMaxSize()
                    .testTag("customer_menu_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 132.dp
                )
            ) {
                // 1. Collapsing Luxury Promo Hero Card (24dp corners, rich gradient, icons & bold headline)
                item(key = "promo_hero_banner") {
                    val heroScale = 1f - (heroCollapseProgress * 0.045f)
                    val heroAlpha = 1f - (heroCollapseProgress * 0.25f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer {
                                scaleX = heroScale
                                scaleY = heroScale
                                alpha = heroAlpha
                                translationY = -heroCollapseProgress * 12f
                            }
                    ) {
                        PromoHeroCard(
                            dailyOfferText = dailyOfferText,
                            kitchenOpen = kitchenOpen,
                            customerName = customerProfile.username,
                            hasPreviousOrder = hasPreviousOrder,
                            onReorderLastOrder = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onReorderLastOrder()
                            },
                            onExplorePopular = {
                                selectedFilterId = FILTER_ALL
                            }
                        )
                    }
                }

                // 2. Chef's Signature Carousel ("الأكثر طلباً") when browsing all
                if (popularItems.isNotEmpty() && searchQuery.isBlank() && selectedFilterId == FILTER_ALL) {
                    item(key = "popular_showcase") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(colors.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = colors.primaryBrand,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "اختيارات الشيف المميزة",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textPrimary
                                    )
                                }
                                Surface(
                                    color = colors.secondaryContainer,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = "الأكثر طلباً",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.goldText,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            LazyRow(
                                state = popularListState,
                                flingBehavior = popularSnapBehavior,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("popular_dishes_carousel")
                            ) {
                                items(popularItems, key = { "pop_${it.id}" }) { popItem ->
                                    val photos = resolveItemPhotos(popItem)
                                    val catName = categoryMap[popItem.menuCategoryId]?.name.orEmpty()
                                    val qtyInCart = cartQuantityMap[popItem.id] ?: 0.0
                                    ChefSignatureMiniCard(
                                        menuItem = popItem,
                                        categoryName = catName,
                                        photos = photos,
                                        quantityInCart = qtyInCart,
                                        onCardClick = { selectedDishForSheet = popItem },
                                        onQuickAdd = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onAddToCart(popItem, photos.firstOrNull(), 1.0)
                                        },
                                        onIncrease = { onUpdateCartQuantity(popItem.id, 0.5) },
                                        onDecrease = { onUpdateCartQuantity(popItem.id, -0.5) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Sticky Interactive Category Pills
                stickyHeader(key = "sticky_categories") {
                    StickyCategoryBar(
                        activeCategories = activeCategories,
                        selectedFilterId = selectedFilterId,
                        favoritesCount = favoriteIds.size,
                        onSelectFilter = { newId ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedFilterId = newId
                        }
                    )
                }

                // 4. Modern Food Cards with Smooth AnimatedContent Transition Between Sections
                item(key = "category_menu_stage") {
                    AnimatedContent(
                        targetState = selectedFilterId to searchQuery.trim(),
                        transitionSpec = {
                            (fadeIn(tween(240, easing = FastOutSlowInEasing)) +
                                slideInVertically(
                                    initialOffsetY = { 28 },
                                    animationSpec = spring(
                                        dampingRatio = 0.85f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )) togetherWith
                                (fadeOut(tween(150)) +
                                    slideOutVertically(targetOffsetY = { -16 }))
                        },
                        label = "category_section_transition"
                    ) { (currentFilter, currentQuery) ->
                        val displayItems = remember(items, currentFilter, currentQuery, favoriteIds) {
                            items.filter { item ->
                                val matchesCat = when (currentFilter) {
                                    FILTER_ALL -> true
                                    FILTER_FAVORITES -> item.id in favoriteIds
                                    else -> item.menuCategoryId == currentFilter
                                }
                                val matchesQ = currentQuery.isBlank() ||
                                    item.displayName.contains(currentQuery, ignoreCase = true) ||
                                    item.description.contains(currentQuery, ignoreCase = true)
                                matchesCat && matchesQ
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val sectionHeading = when {
                                currentQuery.isNotBlank() -> "نتائج البحث (${displayItems.size})"
                                currentFilter == FILTER_ALL -> "قائمة الطعام الكاملة (${displayItems.size})"
                                currentFilter == FILTER_FAVORITES -> "مفضلتك (${displayItems.size})"
                                else -> "${categoryMap[currentFilter]?.name ?: "الأصناف"} (${displayItems.size})"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sectionHeading,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary
                                )
                                if (currentFilter != FILTER_ALL || currentQuery.isNotBlank()) {
                                    TextButton(
                                        onClick = {
                                            selectedFilterId = FILTER_ALL
                                            searchQuery = ""
                                        }
                                    ) {
                                        Text(
                                            text = "عرض الكل",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = colors.primaryBrand,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (displayItems.isEmpty()) {
                                LuxuryCard(cornerRadius = 24.dp, elevation = 3.dp) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(30.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(colors.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RestaurantMenu,
                                                contentDescription = null,
                                                tint = colors.primaryBrand,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (currentFilter == FILTER_FAVORITES) {
                                                "لم تقم بإضافة أصناف للمفضلة بعد"
                                            } else {
                                                "لا توجد أصناف مطابقة للبحث حالياً"
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = colors.textPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "جرّب استعراض تصنيف آخر أو البحث باسم الأكلة",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else if (isWideLayout) {
                                val rows = displayItems.chunked(2)
                                rows.forEachIndexed { rowIdx, pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        pair.forEach { menuItem ->
                                            val catName = categoryMap[menuItem.menuCategoryId]?.name.orEmpty()
                                            val photos = resolveItemPhotos(menuItem)
                                            val qtyInCart = cartQuantityMap[menuItem.id] ?: 0.0
                                            val isFav = menuItem.id in favoriteIds
                                            Box(modifier = Modifier.weight(1f)) {
                                                StaggeredAppearItem(index = rowIdx) {
                                                    ModernFoodCard(
                                                        menuItem = menuItem,
                                                        categoryName = catName,
                                                        photos = photos,
                                                        quantityInCart = qtyInCart,
                                                        isFavorite = isFav,
                                                        onCardClick = { selectedDishForSheet = menuItem },
                                                        onToggleFavorite = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            onToggleFavorite(menuItem.id)
                                                        },
                                                        onAdd = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            onAddToCart(menuItem, photos.firstOrNull(), 1.0)
                                                        },
                                                        onIncrease = { onUpdateCartQuantity(menuItem.id, 0.5) },
                                                        onDecrease = { onUpdateCartQuantity(menuItem.id, -0.5) }
                                                    )
                                                }
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                displayItems.forEachIndexed { idx, menuItem ->
                                    val catName = categoryMap[menuItem.menuCategoryId]?.name.orEmpty()
                                    val photos = resolveItemPhotos(menuItem)
                                    val qtyInCart = cartQuantityMap[menuItem.id] ?: 0.0
                                    val isFav = menuItem.id in favoriteIds

                                    StaggeredAppearItem(index = idx) {
                                        ModernFoodCard(
                                            menuItem = menuItem,
                                            categoryName = catName,
                                            photos = photos,
                                            quantityInCart = qtyInCart,
                                            isFavorite = isFav,
                                            onCardClick = { selectedDishForSheet = menuItem },
                                            onToggleFavorite = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onToggleFavorite(menuItem.id)
                                            },
                                            onAdd = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onAddToCart(menuItem, photos.firstOrNull(), 1.0)
                                            },
                                            onIncrease = { onUpdateCartQuantity(menuItem.id, 0.5) },
                                            onDecrease = { onUpdateCartQuantity(menuItem.id, -0.5) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Subtle Storefront Footer
                item(key = "footer_branding") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (cloudSyncState.isCloudConnected) {
                                "أكلة حلوة — مطبخ أ/ نهى ماهر • متصل لحظياً"
                            } else {
                                "أكلة حلوة — مطبخ أ/ نهى ماهر"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            onClick = onCheckForUpdates,
                            modifier = Modifier.testTag("footer_check_updates_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = colors.goldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "التحقق من التحديثات (v${BuildConfig.VERSION_NAME})",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.goldText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 6. Floating Glass Cart Bar with Pulse Animation on Add
            AnimatedVisibility(
                visible = cartItems.isNotEmpty(),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(tween(220)),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(180)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                FloatingGlassCartBar(
                    cartLinesCount = cartLinesCount,
                    cartTotal = cartTotal,
                    cartUnitsSignature = cartUnitsSignature,
                    onOpenCheckout = { showCheckoutDialog = true }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StorefrontTopBar(
    kitchenOpen: Boolean,
    isDarkMode: Boolean,
    isCollapsed: Boolean,
    searchExpanded: Boolean,
    searchQuery: String,
    trackedOrdersCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    val surfaceBg by animateColorAsState(
        targetValue = if (isCollapsed) colors.floatingDock else colors.background,
        animationSpec = tween(220),
        label = "topbar_bg"
    )
    val shadowElev by animateDpAsState(
        targetValue = if (isCollapsed && !colors.isDark) 6.dp else 0.dp,
        animationSpec = tween(220),
        label = "topbar_elev"
    )

    Surface(
        color = surfaceBg,
        shadowElevation = shadowElev,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_top_banner_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start: App Logo + Brand Name + Live Kitchen Status Capsule
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onOpenAdmin()
                            }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = if (colors.isDark) 0.dp else 4.dp,
                                shape = CircleShape,
                                spotColor = colors.goldAccent.copy(alpha = 0.45f)
                            )
                            .clip(CircleShape)
                            .border(
                                width = 1.6.dp,
                                brush = Brush.linearGradient(
                                    listOf(colors.goldAccent, colors.primaryBrand, colors.goldAccent)
                                ),
                                shape = CircleShape
                            )
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

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "أكلة حلوة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        KitchenLiveStatusPill(kitchenOpen = kitchenOpen)
                    }
                }

                // End: Search + My Orders + Admin Lock + Theme Toggle
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleSearch,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (searchExpanded) colors.primaryContainer else colors.cardSurface)
                            .border(
                                1.dp,
                                if (searchExpanded) colors.primaryBrand.copy(alpha = 0.4f) else colors.cardBorder,
                                CircleShape
                            )
                            .testTag("customer_search_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchExpanded) "إغلاق البحث" else "بحث في المنيو",
                            tint = if (searchExpanded) colors.primaryBrand else colors.textPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Surface(
                        onClick = onOpenOrders,
                        color = colors.cardSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        shadowElevation = if (colors.isDark) 0.dp else 2.dp,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .bounceClick()
                            .testTag("my_orders_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "طلباتي",
                                tint = colors.primaryBrand,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (trackedOrdersCount > 0) "طلباتي ($trackedOrdersCount)" else "طلباتي",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenInfo,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.cardSurface)
                            .border(1.dp, colors.cardBorder, CircleShape)
                            .testTag("customer_info_updates_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "معلومات الحساب والتحديثات",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    ThemeToggleIconButton(
                        isDarkMode = isDarkMode,
                        onToggle = onToggleTheme,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Expandable Search Input Bar
            AnimatedVisibility(
                visible = searchExpanded,
                enter = expandVertically(spring(dampingRatio = 0.85f)) + fadeIn(tween(180)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(140))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.cardSurface,
                        border = BorderStroke(1.dp, colors.primaryBrand.copy(alpha = 0.35f)),
                        shadowElevation = if (colors.isDark) 0.dp else 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = "ابحث عن أكلة، طاجن، محشي، أو تتبيلة...",
                                    color = colors.textSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = colors.primaryBrand
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح البحث", tint = colors.textSecondary)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                    }
                }
            }

            if (isCollapsed) {
                HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.7f), thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun KitchenLiveStatusPill(kitchenOpen: Boolean) {
    val colors = AppTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "kitchen_pulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    val dotColor = if (kitchenOpen) colors.profitGreen else colors.errorRed
    val pillBg = if (kitchenOpen) colors.profitContainer else colors.errorContainer
    val textColor = if (kitchenOpen) colors.onProfitContainer else colors.onErrorContainer

    Surface(
        color = pillBg,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.4f)),
        modifier = Modifier.testTag("kitchen_live_status_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (kitchenOpen) "مفتوح الآن" else "مغلق",
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PromoHeroCard(
    dailyOfferText: String,
    kitchenOpen: Boolean,
    customerName: String,
    hasPreviousOrder: Boolean,
    onReorderLastOrder: () -> Unit,
    onExplorePopular: () -> Unit
) {
    val colors = AppTheme.colors
    val heroHeadlineColor = if (colors.isDark) colors.textPrimary else Color.White
    val heroSubColor = if (colors.isDark) colors.textSecondary else Color.White.copy(alpha = 0.88f)
    val heroBadgeBg = if (colors.isDark) colors.secondaryContainer else Color.White.copy(alpha = 0.18f)
    val heroBadgeBorder = if (colors.isDark) colors.goldAccent.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.35f)
    val heroBadgeFg = if (colors.isDark) colors.goldText else Color.White

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 0.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = colors.softShadow,
                spotColor = colors.softShadow
            )
            .clip(RoundedCornerShape(24.dp))
            .background(colors.heroGradient)
            .border(
                width = 1.dp,
                color = if (colors.isDark) colors.cardBorder else colors.primaryBrand,
                shape = RoundedCornerShape(24.dp)
            )
            .then(
                if (dailyOfferText.isNotBlank()) Modifier.testTag("daily_offer_banner") else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = heroBadgeBg,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, heroBadgeBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (dailyOfferText.isNotBlank()) Icons.Default.Campaign else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = heroBadgeFg,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dailyOfferText.isNotBlank()) "عرض اليوم الخاص" else "مطبخ أ/ نهى ماهر",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = heroBadgeFg
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(heroBadgeBg)
                        .border(1.dp, heroBadgeBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = heroBadgeFg,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = if (dailyOfferText.isNotBlank()) {
                        dailyOfferText
                    } else if (customerName.isNotBlank()) {
                        "أهلاً $customerName ✨ أكل بيتي فاخر بطعم زمان"
                    } else {
                        "أشهى المأكولات البيتي تُحضّر طازجة يومياً"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = heroHeadlineColor,
                    lineHeight = 28.sp
                )
                Text(
                    text = if (kitchenOpen) {
                        "اطلب الآن من منيو اليوم واستمتع بالتجهيز البيتي المضمون"
                    } else {
                        "المطبخ يستقبل طلبات الحجز المسبق الآن بأعلى نظافة وجودة"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = heroSubColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasPreviousOrder) {
                    Surface(
                        onClick = onReorderLastOrder,
                        color = colors.goldAccent,
                        shape = RoundedCornerShape(50),
                        shadowElevation = if (colors.isDark) 0.dp else 4.dp,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 42.dp)
                            .bounceClick()
                            .testTag("one_tap_reorder_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = Color(0xFF1E2022),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعادة طلبك السابق",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E2022),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = onExplorePopular,
                        color = heroBadgeBg,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, heroBadgeBorder),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = heroBadgeFg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "جودة بيتي 100%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = heroBadgeFg
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StickyCategoryBar(
    activeCategories: List<MenuCategoryEntity>,
    selectedFilterId: Long,
    favoritesCount: Int,
    onSelectFilter: (Long) -> Unit
) {
    val colors = AppTheme.colors
    Surface(
        color = colors.background.copy(alpha = 0.96f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .testTag("customer_category_filter_bar"),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item(key = "all_categories_chip") {
                    ExpandingCategoryPill(
                        label = "الكل",
                        selected = selectedFilterId == FILTER_ALL,
                        onClick = { onSelectFilter(FILTER_ALL) },
                        modifier = Modifier.testTag("customer_cat_all")
                    )
                }
                item(key = "favorites_chip") {
                    val favLabel = if (favoritesCount > 0) "المفضلة ❤️ ($favoritesCount)" else "المفضلة ❤️"
                    ExpandingCategoryPill(
                        label = favLabel,
                        selected = selectedFilterId == FILTER_FAVORITES,
                        selectedColor = colors.errorRed,
                        onClick = {
                            onSelectFilter(if (selectedFilterId == FILTER_FAVORITES) FILTER_ALL else FILTER_FAVORITES)
                        },
                        modifier = Modifier.testTag("customer_cat_favorites")
                    )
                }
                items(activeCategories, key = { it.id }) { cat ->
                    ExpandingCategoryPill(
                        label = cat.name,
                        selected = selectedFilterId == cat.id,
                        onClick = {
                            onSelectFilter(if (selectedFilterId == cat.id) FILTER_ALL else cat.id)
                        },
                        modifier = Modifier.testTag("customer_cat_${cat.id}")
                    )
                }
            }
            HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.55f), thickness = 1.dp)
        }
    }
}

@Composable
private fun ExpandingCategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = AppTheme.colors.primaryBrand
) {
    val colors = AppTheme.colors
    val horizontalPad by animateDpAsState(
        targetValue = if (selected) 22.dp else 15.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cat_pill_expand"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) selectedColor else colors.cardSurface,
        animationSpec = tween(220),
        label = "cat_pill_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) selectedColor else colors.cardBorder,
        animationSpec = tween(220),
        label = "cat_pill_border"
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 6.dp else if (colors.isDark) 0.dp else 1.5.dp,
        animationSpec = tween(220),
        label = "cat_pill_elev"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cat_pill_scale"
    )

    val selectedTextColor = if (selectedColor == colors.primaryBrand) colors.onAccentText else Color.White

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = elevation,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .minimumInteractiveComponentSize()
            .wrapContentHeight()
            .clip(RoundedCornerShape(50))
            .bounceClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPad, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(selectedTextColor.copy(alpha = 0.9f))
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (selected) selectedTextColor else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChefSignatureMiniCard(
    menuItem: MenuItemEntity,
    categoryName: String,
    photos: List<String>,
    quantityInCart: Double,
    onCardClick: () -> Unit,
    onQuickAdd: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val colors = AppTheme.colors
    Card(
        onClick = onCardClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 4.dp),
        modifier = Modifier
            .width(214.dp)
            .wrapContentHeight()
            .testTag("popular_card_${menuItem.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
            ) {
                DishPhotoCarousel(
                    photos = photos,
                    categoryName = categoryName,
                    itemName = menuItem.displayName,
                    cornerRadius = 18.dp,
                    fallbackIconSize = 36.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = colors.goldAccent,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "الأكثر طلباً 🔥",
                        color = Color(0xFF1E2022),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    color = colors.primaryBrand,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (menuItem.isVariablePrice && menuItem.publishedPrice <= 0.0) {
                            "حسب السعر"
                        } else {
                            DateUtils.formatCurrency(menuItem.publishedPrice)
                        },
                        color = colors.onAccentText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = menuItem.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AnimatedContent(
                targetState = quantityInCart > 0.0,
                transitionSpec = {
                    (scaleIn(initialScale = 0.88f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                        (scaleOut(targetScale = 0.88f) + fadeOut())
                },
                label = "sig_add_transition"
            ) { inCart ->
                if (!inCart) {
                    Button(
                        onClick = onQuickAdd,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 40.dp)
                            .bounceClick()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = colors.onAccentText, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("أضف سريعاً", color = colors.onAccentText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CulinaryQuantityStepper(
                            quantityText = WhatsAppUtils.formatQuantity(quantityInCart),
                            onDecrease = onDecrease,
                            onIncrease = onIncrease
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernFoodCard(
    menuItem: MenuItemEntity,
    categoryName: String,
    photos: List<String>,
    quantityInCart: Double,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAdd: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val colors = AppTheme.colors
    val qtyText = remember(quantityInCart) {
        WhatsAppUtils.formatQuantity(quantityInCart)
    }
    val favScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_fav_bounce"
    )
    val borderColor by animateColorAsState(
        targetValue = if (quantityInCart > 0.0) colors.goldAccent else colors.cardBorder,
        animationSpec = tween(220),
        label = "dish_card_border"
    )

    Card(
        onClick = onCardClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(
                elevation = if (colors.isDark) 0.dp else 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = colors.softShadow,
                spotColor = colors.softShadow
            )
            .animateContentSize(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
            .border(
                width = if (quantityInCart > 0.0) 1.6.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("customer_menu_item_${menuItem.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Large, Prominent Dish Image with 20dp Rounded Corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(206.dp)
                    .padding(10.dp)
            ) {
                DishPhotoCarousel(
                    photos = photos,
                    categoryName = categoryName,
                    itemName = menuItem.displayName,
                    cornerRadius = 20.dp,
                    fallbackIconSize = 48.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                )

                // Soft Gradient Scrim for Crisp Badge Contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.14f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.42f)
                                )
                            )
                        )
                )

                // Top Overlay Row: Featured / Prep Badge + Favorite Heart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (menuItem.isPopular) {
                        Surface(
                            color = colors.goldAccent,
                            shape = RoundedCornerShape(50),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFF1E2022),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "الأكثر طلباً",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E2022),
                                    maxLines = 1
                                )
                            }
                        }
                    } else if (menuItem.isPreOrder24h) {
                        Surface(
                            color = colors.cardSurface.copy(alpha = 0.94f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.65f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = colors.goldText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تجهيز 24 ساعة",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.goldText,
                                    maxLines = 1
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = colors.profitContainer.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "طازج اليوم",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.onProfitContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(38.dp)
                            .graphicsLayer {
                                scaleX = favScale
                                scaleY = favScale
                            }
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.48f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                            .testTag("fav_btn_${menuItem.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (isFavorite) colors.errorRed else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom Overlay Row: Prep 24h Badge (when Popular also true) & Rating Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (menuItem.isPopular && menuItem.isPreOrder24h) {
                        Surface(
                            color = colors.cardSurface.copy(alpha = 0.94f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.65f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = colors.goldText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تجهيز 24 ساعة",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.goldText
                                )
                            }
                        }
                    } else if (categoryName.isNotBlank()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = categoryName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Surface(
                        color = colors.cardSurface.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = colors.goldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", menuItem.ratingAvg),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Editorial Dish Title, Appetizing Description, Bold Price & Morphing Add -> (- 1 +) Control
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = menuItem.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = menuItem.description.ifBlank {
                        "تجهيز بيتي طازج يومياً بمكونات طبيعية مضمونة ونكهة أصيلة"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        if (menuItem.isVariablePrice && menuItem.publishedPrice <= 0.0) {
                            Text(
                                text = "حسب السعر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.goldText
                            )
                        } else {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = DateUtils.formatCurrency(menuItem.publishedPrice),
                                    style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.primaryBrand
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "/ ${menuItem.unitLabel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = quantityInCart > 0.0,
                        transitionSpec = {
                            (scaleIn(
                                initialScale = 0.78f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) + fadeIn(tween(180))) togetherWith
                                (scaleOut(targetScale = 0.78f) + fadeOut(tween(140)))
                        },
                        label = "morph_add_to_stepper"
                    ) { inCart ->
                        if (!inCart) {
                            Surface(
                                onClick = onAdd,
                                color = colors.primaryBrand,
                                shape = CircleShape,
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .size(48.dp)
                                    .bounceClick()
                                    .testTag("add_to_cart_${menuItem.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "إضافة للسلة",
                                        tint = colors.onAccentText,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        } else {
                            CulinaryQuantityStepper(
                                quantityText = qtyText,
                                onDecrease = onDecrease,
                                onIncrease = onIncrease
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingGlassCartBar(
    cartLinesCount: Int,
    cartTotal: Double,
    cartUnitsSignature: Double,
    onOpenCheckout: () -> Unit
) {
    val colors = AppTheme.colors
    var pulseTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(cartUnitsSignature) {
        if (cartUnitsSignature > 0.0) {
            pulseTrigger = true
            delay(180)
            pulseTrigger = false
        }
    }

    val barPulseScale by animateFloatAsState(
        targetValue = if (pulseTrigger) 1.035f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cart_bar_pulse"
    )

    val itemsCountLabel = when (cartLinesCount) {
        1 -> "صنف واحد"
        2 -> "صنفان"
        in 3..10 -> "$cartLinesCount أصناف"
        else -> "$cartLinesCount صنف"
    }

    Surface(
        onClick = onOpenCheckout,
        color = colors.floatingDock,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = if (colors.isDark) 20.dp else 14.dp,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier
            .widthIn(max = 680.dp)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = barPulseScale
                scaleY = barPulseScale
            }
            .testTag("floating_cart_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colors.warmAccentGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = colors.onAccentText,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Surface(
                        color = colors.goldAccent,
                        shape = CircleShape,
                        border = BorderStroke(1.2.dp, colors.cardSurface),
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AnimatedContent(
                                targetState = cartLinesCount,
                                transitionSpec = {
                                    (scaleIn(initialScale = 0.5f) + fadeIn()) togetherWith fadeOut()
                                },
                                label = "cart_badge_count"
                            ) { count ->
                                Text(
                                    text = count.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E2022)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "$itemsCountLabel في سلتك",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    AnimatedCurrencyCounter(
                        targetValue = cartTotal,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.goldText
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onOpenCheckout,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .bounceClick()
                    .testTag("open_checkout_btn")
            ) {
                Text(
                    text = "إتمام الطلب",
                    color = colors.onAccentText,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.onAccentText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerAccountInfoSheet(
    customerProfile: CustomerProfile,
    cloudSyncState: CloudSyncState,
    trackedOrdersCount: Int,
    onCheckForUpdates: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenAdmin: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardSurface,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("customer_info_updates_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "حسابك وتحديثات التطبيق ✨",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary
            )
            Surface(
                color = colors.secondarySurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "الاسم المسجل: ${customerProfile.username.ifBlank { "عميل أكلة حلوة" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (cloudSyncState.isCloudConnected) colors.profitGreen else colors.goldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cloudSyncState.statusMessage,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        text = "إصدار التطبيق الحالي: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                }
            }

            Button(
                onClick = onCheckForUpdates,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .bounceClick()
                    .testTag("customer_check_updates_btn")
            ) {
                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = colors.onAccentText)
                Spacer(modifier = Modifier.width(8.dp))
                Text("التحقق من التحديثات", color = colors.onAccentText, fontWeight = FontWeight.ExtraBold)
            }

            OutlinedButton(
                onClick = onOpenOrders,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .bounceClick()
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = colors.goldText)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (trackedOrdersCount > 0) "متابعة طلباتي السابقة ($trackedOrdersCount)" else "متابعة طلباتي السابقة",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onOpenAdmin,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_admin_lock_btn")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("دخول لوحة الإدارة", color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerCheckoutSheet(
    cartItems: List<CustomerCartItem>,
    itemsSubtotal: Double,
    initialName: String,
    initialPhone: String,
    initialAddress: String,
    deliveryZones: List<DeliveryZone>,
    onUpdateQuantity: (Long, Double) -> Unit,
    onClearCart: () -> Unit,
    onConfirmSubmit: (name: String, phone: String, zone: DeliveryZone, address: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(initialName) { mutableStateOf(initialName) }
    var phone by remember(initialPhone) { mutableStateOf(initialPhone) }
    var address by remember(initialAddress) { mutableStateOf(initialAddress) }
    var notes by remember { mutableStateOf("") }
    var selectedZone by remember(deliveryZones) {
        mutableStateOf(deliveryZones.firstOrNull() ?: DeliveryZone.DEFAULT_ZONES.first())
    }
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }

    val grandTotal = itemsSubtotal + selectedZone.fee

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardSurface,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("customer_checkout_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.warmAccentGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = colors.onAccentText, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تأكيد الطلب وإرساله",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "مطبخ أكلة حلوة — بإدارة نهى ماهر",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.goldText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                TextButton(onClick = onClearCart) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إفراغ السلة", color = colors.primaryBrand, fontWeight = FontWeight.Bold)
                }
            }

            // Itemized Order Summary with Thumbnails & Quantity Steppers
            Surface(
                color = colors.secondarySurface,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    cartItems.forEach { ci ->
                        val q = WhatsAppUtils.formatQuantity(ci.quantity)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductThumbnail(
                                imagePath = ci.imagePath,
                                contentDescription = ci.menuItem.displayName,
                                size = 46.dp,
                                cornerRadius = 14.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$q × ${ci.menuItem.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (ci.menuItem.isVariablePrice && ci.menuItem.publishedPrice <= 0.0) {
                                        "حسب السعر عند التأكيد"
                                    } else {
                                        DateUtils.formatCurrencyWithDecimal(ci.lineTotal)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.goldText,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onUpdateQuantity(ci.menuItem.id, -0.5) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colors.cardSurface)
                                        .border(1.dp, colors.cardBorder, CircleShape)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "تقليل", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onUpdateQuantity(ci.menuItem.id, 0.5) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colors.primaryBrand)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "زيادة", tint = colors.onAccentText, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = colors.cardBorder)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مجموع الأصناف:", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                        Text(DateUtils.formatCurrencyWithDecimal(itemsSubtotal), fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("رسوم التوصيل (${selectedZone.name}):", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                        Text(
                            text = if (selectedZone.fee > 0.0) DateUtils.formatCurrencyWithDecimal(selectedZone.fee) else "مجاناً",
                            fontWeight = FontWeight.Bold,
                            color = colors.goldText
                        )
                    }
                    HorizontalDivider(color = colors.cardBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الإجمالي النهائي:", fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        AnimatedCurrencyCounter(
                            targetValue = grandTotal,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = colors.profitGreen
                        )
                    }
                }
            }

            // Delivery Zone Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اختر منطقة التوصيل:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("checkout_delivery_zones_row")
                ) {
                    items(deliveryZones, key = { it.id }) { zone ->
                        val feeLabel = if (zone.fee > 0.0) " (${DateUtils.formatCurrency(zone.fee)})" else " (مجاناً)"
                        CulinaryPillChip(
                            label = "${zone.name}$feeLabel",
                            selected = selectedZone.id == zone.id,
                            onClick = { selectedZone = zone },
                            modifier = Modifier.testTag("zone_chip_${zone.id}")
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("الاسم بالكامل *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.primaryBrand) },
                isError = nameError,
                supportingText = if (nameError) ({ Text("يرجى كتابة الاسم بالكامل") }) else null,
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.secondarySurface,
                    unfocusedContainerColor = colors.secondarySurface,
                    focusedBorderColor = colors.primaryBrand,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    if (it.isNotBlank()) phoneError = false
                },
                label = { Text("رقم الموبايل *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = colors.goldAccent) },
                isError = phoneError,
                supportingText = if (phoneError) ({ Text("يرجى إدخال رقم الموبايل للتواصل") }) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_phone_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.secondarySurface,
                    unfocusedContainerColor = colors.secondarySurface,
                    focusedBorderColor = colors.primaryBrand,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            OutlinedTextField(
                value = address,
                onValueChange = {
                    address = it
                    if (it.isNotBlank()) addressError = false
                },
                label = { Text("عنوان التوصيل بالتفصيل *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.profitGreen) },
                isError = addressError,
                supportingText = if (addressError) ({ Text("يرجى كتابة عنوان التوصيل بالتفصيل") }) else null,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_address_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.secondarySurface,
                    unfocusedContainerColor = colors.secondarySurface,
                    focusedBorderColor = colors.primaryBrand,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("ملاحظات إضافية (ميعاد التسليم / بدون شطة ...)") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = colors.goldAccent) },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_notes_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.secondarySurface,
                    unfocusedContainerColor = colors.secondarySurface,
                    focusedBorderColor = colors.primaryBrand,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )

            Button(
                onClick = {
                    val validName = name.isNotBlank()
                    val validPhone = phone.isNotBlank()
                    val validAddress = address.isNotBlank()
                    nameError = !validName
                    phoneError = !validPhone
                    addressError = !validAddress
                    if (validName && validPhone && validAddress) {
                        onConfirmSubmit(name, phone, selectedZone, address, notes)
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.profitGreen),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 54.dp)
                    .shadow(8.dp, RoundedCornerShape(50), spotColor = colors.profitGreen.copy(alpha = 0.35f))
                    .bounceClick()
                    .testTag("submit_customer_order_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تأكيد الطلب وإرساله على الواتساب",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp)
            ) {
                Text("رجوع للمنيو", color = colors.textSecondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
