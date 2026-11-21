package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.components.CulinaryPillChip
import com.example.ui.components.StaggeredAppearItem
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
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
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .imePadding()
                .testTag("orders_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (colors.isDark) 0.dp else 4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder)
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
                    shape = RoundedCornerShape(20.dp),
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

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CulinaryPillChip(
                    label = "كل الطلبات (${orders.size})",
                    selected = statusFilter == null,
                    onClick = { onStatusFilterChange(null) },
                    selectedColor = colors.primaryBrand,
                    modifier = Modifier.testTag("filter_all_orders")
                )
                CulinaryPillChip(
                    label = "طلبات العملاء 🛍️",
                    selected = statusFilter == "CUSTOMER",
                    onClick = {
                        onStatusFilterChange(if (statusFilter == "CUSTOMER") null else "CUSTOMER")
                    },
                    selectedColor = colors.profitGreen,
                    modifier = Modifier.testTag("filter_customer_orders")
                )
                CulinaryPillChip(
                    label = "قيد التجهيز ⏳",
                    selected = statusFilter == OrderStatus.PENDING.name,
                    onClick = {
                        onStatusFilterChange(if (statusFilter == OrderStatus.PENDING.name) null else OrderStatus.PENDING.name)
                    },
                    selectedColor = colors.secondaryAccent,
                    modifier = Modifier.testTag("filter_pending_orders")
                )
                CulinaryPillChip(
                    label = "تم التسليم ✅",
                    selected = statusFilter == OrderStatus.DELIVERED.name,
                    onClick = {
                        onStatusFilterChange(if (statusFilter == OrderStatus.DELIVERED.name) null else OrderStatus.DELIVERED.name)
                    },
                    selectedColor = colors.profitGreen,
                    modifier = Modifier.testTag("filter_delivered_orders")
                )
                CulinaryPillChip(
                    label = "ملغي",
                    selected = statusFilter == OrderStatus.CANCELLED.name,
                    onClick = {
                        onStatusFilterChange(if (statusFilter == OrderStatus.CANCELLED.name) null else OrderStatus.CANCELLED.name)
                    },
                    selectedColor = colors.primaryBrand,
                    modifier = Modifier.testTag("filter_cancelled_orders")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface)
                    ) {
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
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || statusFilter != null) {
                                    "لا توجد نتائج مطابقة لبحثك"
                                } else {
                                    "لا توجد طلبات مسجلة في هذا الشهر حتى الآن"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "سجل الآن أول طلبية لتبدأ في متابعة مبيعاتك وأرباحك بدقة!",
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
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تسجيل أوردر جديد", fontWeight = FontWeight.Bold, color = Color.White)
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
                    contentPadding = PaddingValues(bottom = 124.dp)
                ) {
                    itemsIndexed(orders, key = { _, o -> o.id }) { index, order ->
                        val itemsCount = orderItemsMap[order.id]?.size ?: 1
                        StaggeredAppearItem(index = index) {
                            PremiumOrderCard(
                                order = order,
                                itemsCount = itemsCount,
                                onClick = { onOrderClick(order) },
                                onToggleStatus = { onToggleStatus(order) },
                                onShare = {
                                    val text = generateReceiptText(order)
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة الأوردر"))
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
    onShare: () -> Unit
) {
    val colors = AppTheme.colors
    val isDelivered = order.status == OrderStatus.DELIVERED.name
    val isCancelled = order.status == OrderStatus.CANCELLED.name
    val isPaid = order.paymentStatus == PaymentStatus.PAID.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(if (colors.isDark) 0.dp else 4.dp, RoundedCornerShape(22.dp))
            .border(
                width = if (order.isFromCustomer) 1.5.dp else 1.dp,
                color = if (order.isFromCustomer) colors.profitGreen.copy(alpha = 0.55f) else if (colors.isDark) colors.glassBorder else colors.cardBorder,
                shape = RoundedCornerShape(22.dp)
            )
            .bounceClick(onClick = onClick)
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (order.isFromCustomer) {
                Surface(
                    color = colors.profitContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "طلب من عميل المنيو 🛍️",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onProfitContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(colors.warmAccentGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.customerName.take(1).ifBlank { "ع" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = order.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = order.customerPhone.ifBlank { DateUtils.formatDate(order.orderDate) },
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                val statusContainer = when {
                    isDelivered -> colors.profitContainer
                    isCancelled -> colors.primaryContainer
                    else -> colors.secondaryContainer
                }
                val statusTextCol = when {
                    isDelivered -> colors.onProfitContainer
                    isCancelled -> colors.primaryBrand
                    else -> colors.goldAccent
                }
                val statusLabel = OrderStatus.trackingTitle(order.status)

                Surface(
                    color = statusContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, statusTextCol.copy(alpha = 0.4f))
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
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusTextCol
                        )
                    }
                }
            }

            if (order.deliveryZone.isNotBlank() || order.deliveryAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    val zonePrefix = if (order.deliveryZone.isNotBlank()) "${order.deliveryZone} • " else ""
                    Text(
                        text = "$zonePrefix${order.deliveryAddress}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = colors.secondarySurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "[$itemsCount صنف] ${order.itemsDescription}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("إجمالي البيع", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        Text(
                            text = DateUtils.formatCurrency(order.salePrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.primaryBrand
                        )
                    }

                    Box(modifier = Modifier.height(26.dp).width(1.dp).background(colors.cardBorder))

                    Column {
                        Text("تكلفة الشراء", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        Text(
                            text = DateUtils.formatCurrency(order.costPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }

                Surface(
                    color = colors.profitContainer,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ربح الأوردر", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.onProfitContainer)
                            Text("+${DateUtils.formatCurrency(order.netProfit)}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = colors.profitGreen)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = if (isPaid) colors.profitGreen else colors.primaryBrand, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPaid) "مدفوع بالكامل" else "آجل / غير مدفوع",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) colors.onProfitContainer else colors.onPrimaryContainer
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = onToggleStatus,
                        color = colors.secondarySurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.defaultMinSize(minHeight = 40.dp).bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDelivered) Icons.Default.HourglassEmpty else Icons.Default.Check,
                                contentDescription = "المرحلة التالية",
                                tint = colors.goldAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "للمرحلة التالية: ${OrderStatus.trackingTitle(OrderStatus.nextAdminStatus(order.status).name)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.profitContainer, CircleShape)
                            .border(1.dp, colors.profitGreen.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة الفاتورة", tint = Color(0xFF25D366), modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}
