package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.CloudSyncState
import com.example.model.CustomerCartItem
import com.example.model.CustomerProfile
import com.example.model.DeliveryZone
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import com.example.model.TrackedCloudOrder
import com.example.ui.components.AdminPinDialog
import com.example.ui.components.CheckeredRibbon
import com.example.ui.components.CulinaryPillChip
import com.example.ui.components.CulinaryQuantityStepper
import com.example.ui.components.DishPhotoCarousel
import com.example.ui.components.StaggeredAppearItem
import com.example.ui.components.ThemeToggleIconButton
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import com.example.util.WhatsAppUtils
import java.util.Locale

private const val FILTER_ALL = -1L
private const val FILTER_FAVORITES = -2L

@OptIn(ExperimentalFoundationApi::class)
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterId by remember { mutableStateOf(FILTER_ALL) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var selectedDishForSheet by remember { mutableStateOf<MenuItemEntity?>(null) }
    var showMyOrdersSheet by remember { mutableStateOf(false) }

    // Deduplicated active categories with items
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

    if (showCheckoutDialog) {
        CustomerCheckoutDialog(
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
        topBar = {
            Column(
                modifier = Modifier
                    .background(colors.background)
                    .statusBarsPadding()
            ) {
                CheckeredRibbon(height = 4.dp, squareSize = 4.dp)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundGradient),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("customer_menu_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 10.dp,
                    bottom = innerPadding.calculateBottomPadding() + 120.dp
                )
            ) {
                // 1. Gourmet Top Hero Banner Card with discreet Admin Lock + Long-press title
                item(key = "header_banner") {
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(26.dp),
                                spotColor = colors.primaryBrand.copy(alpha = 0.4f)
                            )
                            .clip(RoundedCornerShape(26.dp))
                            .background(colors.heroGradient)
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        colors.goldAccent.copy(alpha = 0.65f),
                                        Color.White.copy(alpha = 0.14f)
                                    )
                                ),
                                shape = RoundedCornerShape(26.dp)
                            )
                            .testTag("customer_top_banner_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Discreet Admin Lock Trigger
                                    IconButton(
                                        onClick = { showAdminPinDialog = true },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.16f))
                                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                            .testTag("customer_admin_lock_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "دخول الإدارة",
                                            tint = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    KitchenLiveStatusPill(kitchenOpen = kitchenOpen)
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // "طلباتي" Order Tracking Button
                                    Surface(
                                        onClick = { showMyOrdersSheet = true },
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 40.dp)
                                            .testTag("my_orders_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val activeCount = trackedOrders.size
                                            Text(
                                                text = if (activeCount > 0) "طلباتي ($activeCount)" else "طلباتي",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    ThemeToggleIconButton(
                                        isDarkMode = isDarkMode,
                                        onToggle = onToggleTheme
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "شعار أكلة حلوة",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .shadow(6.dp, CircleShape)
                                        .clip(CircleShape)
                                        .border(2.dp, colors.goldAccent, CircleShape)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .combinedClickable(
                                            onClick = {},
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showAdminPinDialog = true
                                            }
                                        )
                                ) {
                                    Text(
                                        text = "أكلة حلوة — مطبخ أ/ نهى ماهر",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    val welcomeName = customerProfile.username.takeIf { it.isNotBlank() }
                                    Text(
                                        text = if (welcomeName != null) {
                                            "أهلاً $welcomeName ✨ تجهيزات بيتي طازجة يومياً"
                                        } else {
                                            "تجهيزات بيتي طازجة يومياً بأعلى جودة"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.92f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Daily Offer Banner (editable by Admin)
                if (dailyOfferText.isNotBlank()) {
                    item(key = "daily_offer_banner") {
                        Surface(
                            color = if (colors.isDark) colors.glassSurface else colors.secondaryContainer,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("daily_offer_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colors.warmAccentGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "عرض اليوم ✨",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.goldAccent
                                    )
                                    Text(
                                        text = dailyOfferText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 1-Tap Reorder Banner if user has a previous order
                if (hasPreviousOrder) {
                    item(key = "one_tap_reorder") {
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onReorderLastOrder()
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, colors.primaryBrand.copy(alpha = 0.55f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick()
                                .testTag("one_tap_reorder_btn")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اطلب أوردرك السابق تاني بضغطة واحدة",
                                color = colors.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Popular / Most Ordered Carousel
                if (popularItems.isNotEmpty() && searchQuery.isBlank() && selectedFilterId == FILTER_ALL) {
                    item(key = "popular_showcase") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "الأكثر طلباً 🔥",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 2.dp),
                                modifier = Modifier.testTag("popular_dishes_carousel")
                            ) {
                                items(popularItems, key = { "pop_${it.id}" }) { popItem ->
                                    val photos = resolveItemPhotos(popItem)
                                    val catName = categoryMap[popItem.menuCategoryId]?.name.orEmpty()
                                    PopularDishMiniCard(
                                        menuItem = popItem,
                                        categoryName = catName,
                                        photos = photos,
                                        onCardClick = { selectedDishForSheet = popItem },
                                        onQuickAdd = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onAddToCart(popItem, photos.firstOrNull(), 1.0)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item(key = "search_bar") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث عن أكلة أو تتبيلة...", color = colors.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primaryBrand) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح", tint = colors.textSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                            unfocusedContainerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                            focusedBorderColor = colors.primaryBrand,
                            unfocusedBorderColor = if (colors.isDark) colors.glassBorder else colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }

                // Single Deduplicated Horizontally Scrollable Category Filter Bar + Favorites
                item(key = "categories_lazy_row") {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_category_filter_bar"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item(key = "all_categories_chip") {
                            CulinaryPillChip(
                                label = "الكل",
                                selected = selectedFilterId == FILTER_ALL,
                                onClick = { selectedFilterId = FILTER_ALL },
                                modifier = Modifier.testTag("customer_cat_all")
                            )
                        }
                        item(key = "favorites_chip") {
                            CulinaryPillChip(
                                label = "المفضلة ❤️",
                                selected = selectedFilterId == FILTER_FAVORITES,
                                selectedColor = Color(0xFFE63962),
                                onClick = {
                                    selectedFilterId = if (selectedFilterId == FILTER_FAVORITES) FILTER_ALL else FILTER_FAVORITES
                                },
                                modifier = Modifier.testTag("customer_cat_favorites")
                            )
                        }
                        items(activeCategories, key = { it.id }) { cat ->
                            CulinaryPillChip(
                                label = cat.name,
                                selected = selectedFilterId == cat.id,
                                onClick = {
                                    selectedFilterId = if (selectedFilterId == cat.id) FILTER_ALL else cat.id
                                },
                                modifier = Modifier.testTag("customer_cat_${cat.id}")
                            )
                        }
                    }
                }

                if (filteredItems.isEmpty()) {
                    item(key = "empty_menu") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (colors.isDark) colors.glassBorder else colors.cardBorder,
                                    RoundedCornerShape(22.dp)
                                ),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
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
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (selectedFilterId == FILTER_FAVORITES) {
                                        "لم تقم بإضافة أصناف للمفضلة بعد"
                                    } else {
                                        "لا توجد أصناف مطابقة للبحث حالياً"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(filteredItems, key = { _, item -> item.id }) { idx, menuItem ->
                        val catName = categoryMap[menuItem.menuCategoryId]?.name.orEmpty()
                        val photos = resolveItemPhotos(menuItem)
                        val qtyInCart = cartQuantityMap[menuItem.id] ?: 0.0
                        val isFav = menuItem.id in favoriteIds

                        StaggeredAppearItem(index = idx) {
                            GourmetCustomerMenuItemCard(
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

                item(key = "footer_branding") {
                    Text(
                        text = if (cloudSyncState.isCloudConnected) {
                            "أكلة حلوة — بإدارة نهى ماهر • متصل لحظياً"
                        } else {
                            "أكلة حلوة — بإدارة نهى ماهر"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Floating Animated Bottom Pill Cart Bar
            AnimatedVisibility(
                visible = cartItems.isNotEmpty(),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                val itemsCountLabel = when (cartLinesCount) {
                    1 -> "صنف واحد"
                    2 -> "صنفان"
                    in 3..10 -> "$cartLinesCount أصناف"
                    else -> "$cartLinesCount صنف"
                }

                Surface(
                    color = colors.cardSurface,
                    shape = RoundedCornerShape(50),
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.5.dp, colors.primaryBrand.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxWidth()
                        .testTag("floating_cart_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(colors.warmAccentGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$itemsCountLabel في السلة",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = DateUtils.formatCurrency(cartTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.primaryBrand,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showCheckoutDialog = true },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick()
                                .testTag("open_checkout_btn")
                        ) {
                            Text(
                                text = "مراجعة وإرسال",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KitchenLiveStatusPill(kitchenOpen: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "kitchen_pulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    val dotColor = if (kitchenOpen) Color(0xFF2EC4B6) else Color(0xFFFFB84D)

    Surface(
        color = Color.Black.copy(alpha = 0.28f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.65f)),
        modifier = Modifier.testTag("customer_kitchen_status_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (kitchenOpen) "مفتوح لاستقبال الطلبات" else "مشغول حالياً",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PopularDishMiniCard(
    menuItem: MenuItemEntity,
    categoryName: String,
    photos: List<String>,
    onCardClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    val colors = AppTheme.colors
    Surface(
        onClick = onCardClick,
        color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.45f)),
        shadowElevation = if (colors.isDark) 0.dp else 4.dp,
        modifier = Modifier
            .width(174.dp)
            .testTag("popular_dish_card_${menuItem.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                DishPhotoCarousel(
                    photos = photos,
                    categoryName = categoryName,
                    itemName = menuItem.displayName,
                    cornerRadius = 14.dp,
                    fallbackIconSize = 28.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = colors.primaryBrand,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (menuItem.isVariablePrice && menuItem.publishedPrice <= 0.0) "حسب السعر" else DateUtils.formatCurrency(menuItem.publishedPrice),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = menuItem.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = onQuickAdd,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("أضف", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GourmetCustomerMenuItemCard(
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

    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(
                width = if (quantityInCart > 0.0) 1.5.dp else 1.dp,
                color = when {
                    quantityInCart > 0.0 -> colors.primaryBrand
                    colors.isDark -> colors.glassBorder
                    else -> colors.cardBorder
                },
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("customer_menu_item_${menuItem.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.size(96.dp)) {
                    DishPhotoCarousel(
                        photos = photos,
                        categoryName = categoryName,
                        itemName = menuItem.displayName,
                        cornerRadius = 18.dp,
                        fallbackIconSize = 30.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(18.dp))
                    )

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("fav_btn_${menuItem.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (isFavorite) Color(0xFFFF4D6D) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = menuItem.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", menuItem.ratingAvg),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.goldAccent
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (categoryName.isNotBlank()) {
                            Text(
                                text = "$categoryName • ${menuItem.unitLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                        Surface(
                            color = if (menuItem.isPreOrder24h) colors.secondaryContainer else colors.profitContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (menuItem.isPreOrder24h) colors.goldAccent else colors.profitGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (menuItem.isPreOrder24h) "حجز 24 س" else "جاهز",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (menuItem.isPreOrder24h) colors.goldAccent else colors.onProfitContainer,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (menuItem.description.isNotBlank()) {
                        Text(
                            text = menuItem.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = colors.primaryContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colors.primaryBrand.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (menuItem.isVariablePrice && menuItem.publishedPrice <= 0.0) {
                            "حسب السعر"
                        } else {
                            "${DateUtils.formatCurrency(menuItem.publishedPrice)} / ${menuItem.unitLabel}"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                AnimatedContent(
                    targetState = quantityInCart > 0.0,
                    transitionSpec = {
                        (scaleIn(initialScale = 0.85f) + fadeIn()) togetherWith
                            (scaleOut(targetScale = 0.85f) + fadeOut())
                    },
                    label = "cart_stepper_transition"
                ) { inCart ->
                    if (!inCart) {
                        Button(
                            onClick = onAdd,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick()
                                .testTag("add_to_cart_${menuItem.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("أضف للسلة", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
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

@Composable
private fun CustomerCheckoutDialog(
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(14.dp)
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .imePadding()
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                .testTag("customer_checkout_dialog"),
            color = colors.cardSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مراجعة الطلب وإرساله 🛍️",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "مطبخ أكلة حلوة — بإدارة نهى ماهر",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }

                    TextButton(onClick = onClearCart) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إفراغ السلة", color = colors.primaryBrand, fontWeight = FontWeight.Bold)
                    }
                }

                // Itemized Order Summary
                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cartItems.forEach { ci ->
                            val q = WhatsAppUtils.formatQuantity(ci.quantity)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$q × ${ci.menuItem.displayName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (ci.menuItem.isVariablePrice && ci.menuItem.publishedPrice <= 0.0) {
                                            "حسب السعر عند التأكيد"
                                        } else {
                                            DateUtils.formatCurrencyWithDecimal(ci.lineTotal)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.primaryBrand,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onUpdateQuantity(ci.menuItem.id, -0.5) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "تقليل", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { onUpdateQuantity(ci.menuItem.id, 0.5) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "زيادة", tint = colors.primaryBrand, modifier = Modifier.size(16.dp))
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
                                color = colors.goldAccent
                            )
                        }
                        HorizontalDivider(color = colors.cardBorder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الإجمالي النهائي:", fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                            Text(
                                text = DateUtils.formatCurrencyWithDecimal(grandTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.profitGreen
                            )
                        }
                    }
                }

                // Delivery Zone Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اختر منطقة التوصيل:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
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
                    shape = RoundedCornerShape(16.dp),
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
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = colors.secondaryAccent) },
                    isError = phoneError,
                    supportingText = if (phoneError) ({ Text("يرجى إدخال رقم الموبايل للتواصل") }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
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
                    shape = RoundedCornerShape(16.dp),
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
                    shape = RoundedCornerShape(16.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
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
}
