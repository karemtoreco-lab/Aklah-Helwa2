package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Order
import com.example.model.OrderLineItemEntity
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.ui.components.AnimatedCurrencyCounter
import com.example.ui.components.AnimatedIntCounter
import com.example.ui.components.CulinaryPillChip
import com.example.ui.components.LuxuryCard
import com.example.ui.components.StaggeredAppearItem
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import com.example.util.WhatsAppUtils

@Composable
fun OrdersScreen(
    orders: List<Order>,
    orderItemsMap: Map<Long, List<OrderLineItemEntity>>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: String?,
    onStatusFilterChange: (String?) -> Unit,
    onOrderClick: (Order) -> Unit,
    onToggleStatus: (Order) -> Unit,
    onAddOrderClick: () -> Unit,
    generateReceiptText: (Order) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundGradient),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .imePadding()
                .testTag("orders_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Clean Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (colors.isDark) 0.dp else 3.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = colors.softShadow,
                        spotColor = colors.softShadow
                    ),
                shape = RoundedCornerShape(22.dp),
                color = colors.cardSurface,
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            "ابحث باسم العميل، الصنف، المنطقة، أو الهاتف...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = colors.primaryBrand, modifier = Modifier.size(21.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_orders_input"),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smooth Horizontal Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item(key = "filter_all") {
                    CulinaryPillChip(
                        label = "كل الطلبات (${orders.size})",
                        selected = statusFilter == null,
                        onClick = { onStatusFilterChange(null) },
                        selectedColor = colors.primaryBrand,
                        modifier = Modifier.testTag("filter_all_orders")
                    )
                }
                item(key = "filter_customer") {
                    CulinaryPillChip(
                        label = "طلبات العملاء",
                        selected = statusFilter == "CUSTOMER",
                        onClick = {
                            onStatusFilterChange(if (statusFilter == "CUSTOMER") null else "CUSTOMER")
                        },
                        selectedColor = colors.profitGreen,
                        modifier = Modifier.testTag("filter_customer_orders")
                    )
                }
                item(key = "filter_pending") {
                    CulinaryPillChip(
                        label = "قيد التجهيز",
                        selected = statusFilter == OrderStatus.PENDING.name,
                        onClick = {
                            onStatusFilterChange(if (statusFilter == OrderStatus.PENDING.name) null else OrderStatus.PENDING.name)
                        },
                        selectedColor = colors.goldAccent,
                        modifier = Modifier.testTag("filter_pending_orders")
                    )
                }
                item(key = "filter_delivered") {
                    CulinaryPillChip(
                        label = "تم التسليم",
                        selected = statusFilter == OrderStatus.DELIVERED.name,
                        onClick = {
                            onStatusFilterChange(if (statusFilter == OrderStatus.DELIVERED.name) null else OrderStatus.DELIVERED.name)
                        },
                        selectedColor = colors.profitGreen,
                        modifier = Modifier.testTag("filter_delivered_orders")
                    )
                }
                item(key = "filter_cancelled") {
                    CulinaryPillChip(
                        label = "ملغي",
                        selected = statusFilter == OrderStatus.CANCELLED.name,
                        onClick = {
                            onStatusFilterChange(if (statusFilter == OrderStatus.CANCELLED.name) null else OrderStatus.CANCELLED.name)
                        },
                        selectedColor = colors.errorRed,
                        modifier = Modifier.testTag("filter_cancelled_orders")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (orders.isNotEmpty()) {
                val filteredSales by remember(orders) {
                    derivedStateOf { orders.sumOf { it.salePrice } }
                }
                val filteredProfit by remember(orders) {
                    derivedStateOf { orders.sumOf { it.netProfit } }
                }
                LuxuryCard(
                    cornerRadius = 20.dp,
                    elevation = 2.dp,
                    modifier = Modifier.testTag("orders_summary_strip")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("عدد الأوردرات", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            AnimatedIntCounter(
                                targetValue = orders.size,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = colors.textPrimary,
                                suffix = " أوردر"
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .width(1.dp)
                                .background(colors.cardBorder)
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("إجمالي المبيعات", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            AnimatedCurrencyCounter(
                                targetValue = filteredSales,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = colors.goldText
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .width(1.dp)
                                .background(colors.cardBorder)
                        )
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("صافي الربح", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            AnimatedCurrencyCounter(
                                targetValue = filteredProfit,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = colors.profitGreen,
                                prefix = if (filteredProfit > 0) "+" else ""
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LuxuryCard(cornerRadius = 24.dp, elevation = 3.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_empty_kitchen),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || statusFilter != null) {
                                    "لا توجد نتائج مطابقة لبحثك"
                                } else {
                                    "لا توجد طلبات مسجلة في هذا الشهر حتى الآن"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "سجّلي الآن أول طلبية لمتابعة المبيعات والأرباح بدقة",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = onAddOrderClick,
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .bounceClick()
                                    .testTag("empty_state_add_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = colors.onAccentText, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تسجيل أوردر جديد", fontWeight = FontWeight.Bold, color = colors.onAccentText)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("orders_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 126.dp)
                ) {
                    itemsIndexed(orders, key = { _, o -> o.id }) { index, order ->
                        val itemsCount = orderItemsMap[order.id]?.size ?: 1
                        StaggeredAppearItem(index = index) {
                            PremiumOrderCard(
                                order = order,
                                itemsCount = itemsCount,
                                onClick = { onOrderClick(order) },
                                onToggleStatus = { onToggleStatus(order) },
                                onCallCustomer = {
                                    val phone = order.customerPhone.trim()
                                    if (phone.isNotBlank()) {
                                        try {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                            context.startActivity(dialIntent)
                                        } catch (_: Exception) {
                                        }
                                    }
                                },
                                onWhatsAppCustomer = {
                                    val sanitized = WhatsAppUtils.sanitizeWhatsAppNumber(order.customerPhone)
                                    if (sanitized.isNotBlank()) {
                                        val msg = "أهلاً ${order.customerName}، بخصوص طلبك من مطبخ أكلة حلوة (${OrderStatus.trackingTitle(order.status)})"
                                        WhatsAppUtils.launchWhatsAppOrderIntent(
                                            context = context,
                                            rawAdminPhone = sanitized,
                                            message = msg
                                        )
                                    }
                                },
                                onShare = {
                                    val text = generateReceiptText(order)
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة الفاتورة"))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PremiumOrderCard(
    order: Order,
    itemsCount: Int,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onShare: () -> Unit,
    onCallCustomer: () -> Unit = {},
    onWhatsAppCustomer: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    val isDelivered = order.status == OrderStatus.DELIVERED.name
    val isCancelled = order.status == OrderStatus.CANCELLED.name
    val isNew = order.status == OrderStatus.NEW_CUSTOMER.name || order.status == OrderStatus.PENDING_REVIEW.name
    val isPaid = order.paymentStatus == PaymentStatus.PAID.name

    val sideStripColor by animateColorAsState(
        targetValue = when {
            isDelivered -> colors.profitGreen
            isCancelled -> colors.errorRed
            isNew -> colors.primaryBrand
            else -> colors.goldAccent
        },
        animationSpec = tween(250),
        label = "order_side_strip"
    )
    val statusContainer by animateColorAsState(
        targetValue = when {
            isDelivered -> colors.profitContainer
            isCancelled -> colors.errorContainer
            isNew -> colors.primaryContainer
            else -> colors.secondaryContainer
        },
        animationSpec = tween(240),
        label = "order_status_bg"
    )
    val statusTextCol by animateColorAsState(
        targetValue = when {
            isDelivered -> colors.onProfitContainer
            isCancelled -> colors.onErrorContainer
            isNew -> colors.onPrimaryContainer
            else -> colors.goldText
        },
        animationSpec = tween(240),
        label = "order_status_fg"
    )
    val statusLabel = OrderStatus.trackingTitle(order.status)

    LuxuryCard(
        cornerRadius = 24.dp,
        borderWidth = 1.dp,
        borderColor = colors.cardBorder,
        elevation = 4.dp,
        onClick = onClick,
        modifier = Modifier.testTag("order_card_${order.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Colored Status Side Accent Bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(sideStripColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: Customer Avatar, Name, Phone + Quick Call & WhatsApp + Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer)
                                .border(1.dp, colors.primaryBrand.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = order.customerName.take(1).ifBlank { "ع" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primaryBrand
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = order.customerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (order.isFromCustomer) {
                                    Surface(
                                        color = colors.profitContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "عميل",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = colors.onProfitContainer,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (order.customerPhone.isNotBlank()) {
                                    "${order.customerPhone} • ${DateUtils.formatDate(order.orderDate)}"
                                } else {
                                    DateUtils.formatDate(order.orderDate)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = statusContainer,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, sideStripColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    isDelivered -> Icons.Default.CheckCircle
                                    order.status == OrderStatus.OUT_FOR_DELIVERY.name -> Icons.Default.LocalShipping
                                    else -> Icons.Default.HourglassEmpty
                                },
                                contentDescription = null,
                                tint = statusTextCol,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusTextCol,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Quick Contact Row (Call + WhatsApp) if phone is present
                if (order.customerPhone.isNotBlank() || order.deliveryZone.isNotBlank() || order.deliveryAddress.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (order.deliveryZone.isNotBlank() || order.deliveryAddress.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = colors.profitGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val zonePrefix = if (order.deliveryZone.isNotBlank()) "${order.deliveryZone} • " else ""
                                Text(
                                    text = "$zonePrefix${order.deliveryAddress}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        if (order.customerPhone.isNotBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    onClick = onCallCustomer,
                                    color = colors.secondarySurface,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, colors.cardBorder),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .defaultMinSize(minHeight = 38.dp)
                                        .bounceClick()
                                        .testTag("order_call_btn_${order.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "اتصال بالعميل",
                                            tint = colors.primaryBrand,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "اتصال",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                    }
                                }

                                Surface(
                                    onClick = onWhatsAppCustomer,
                                    color = colors.profitContainer,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .defaultMinSize(minHeight = 38.dp)
                                        .bounceClick()
                                        .testTag("order_whatsapp_btn_${order.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = "واتساب العميل",
                                            tint = colors.profitGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "واتساب",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onProfitContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Items Summary Container
                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = colors.primaryBrand,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "[$itemsCount صنف] ${order.itemsDescription}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Financial Summary + Clear Net Profit Badge
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("إجمالي البيع", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            Text(
                                text = DateUtils.formatCurrency(order.salePrice),
                                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.goldText
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(colors.cardBorder)
                        )

                        Column {
                            Text("التكلفة", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            Text(
                                text = DateUtils.formatCurrency(order.costPrice),
                                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            if (order.extraPrepCost > 0.0) {
                                Text(
                                    text = "شامل ${DateUtils.formatCurrency(order.extraPrepCost)} تجهيز",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.goldText
                                )
                            }
                        }
                    }

                    // Prominent Net Profit Pill
                    Surface(
                        color = colors.profitContainer,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = colors.profitGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "صافي الربح",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onProfitContainer
                                )
                                Text(
                                    text = "+${DateUtils.formatCurrency(order.netProfit)}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.profitGreen
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.7f))

                // Footer Actions: Payment Pill + 1-Tap Next Stage + Share Receipt
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isPaid) colors.profitContainer else colors.primaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = if (isPaid) colors.profitGreen else colors.primaryBrand,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPaid) "مدفوع" else "آجل / غير مدفوع",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaid) colors.onProfitContainer else colors.onPrimaryContainer
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleStatus()
                            },
                            color = colors.primaryBrand,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .defaultMinSize(minHeight = 42.dp)
                                .bounceClick()
                                .testTag("toggle_order_status_${order.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isDelivered) Icons.Default.HourglassEmpty else Icons.Default.Check,
                                    contentDescription = "تغيير الحالة",
                                    tint = colors.onAccentText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = OrderStatus.trackingTitle(OrderStatus.nextAdminStatus(order.status).name),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.onAccentText,
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.secondarySurface)
                                .border(1.dp, colors.cardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة الفاتورة",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
