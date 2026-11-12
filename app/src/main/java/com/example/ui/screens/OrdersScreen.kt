package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.ui.components.CheckeredRibbon
import com.example.ui.theme.BrandRedPrimary
import com.example.ui.theme.CharcoalCardBorder
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CharcoalCardSurface
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitContainer
import com.example.ui.theme.EmeraldProfitOnContainer
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.util.DateUtils

@Composable
fun OrdersScreen(
    orders: List<Order>,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("orders_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar Card in Charcoal
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = CharcoalCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalCardBorder)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "ابحث باسم العميل، الصنف، أو الهاتف...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF777777)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = BrandRedPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextWhiteSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_orders_input"),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    focusedTextColor = CleanWhite,
                    unfocusedTextColor = CleanWhite
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = statusFilter == null,
                onClick = { onStatusFilterChange(null) },
                label = { Text("كل الطلبات (${orders.size})", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandRedPrimary,
                    selectedLabelColor = CleanWhite,
                    containerColor = CharcoalCardSurface,
                    labelColor = TextWhiteSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("filter_all_orders")
            )

            FilterChip(
                selected = statusFilter == OrderStatus.PENDING.name,
                onClick = {
                    onStatusFilterChange(
                        if (statusFilter == OrderStatus.PENDING.name) null else OrderStatus.PENDING.name
                    )
                },
                label = { Text("قيد التجهيز ⏳") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFA000),
                    selectedLabelColor = Color.Black,
                    containerColor = CharcoalCardSurface,
                    labelColor = TextWhiteSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("filter_pending_orders")
            )

            FilterChip(
                selected = statusFilter == OrderStatus.DELIVERED.name,
                onClick = {
                    onStatusFilterChange(
                        if (statusFilter == OrderStatus.DELIVERED.name) null else OrderStatus.DELIVERED.name
                    )
                },
                label = { Text("تم التسليم ✅") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldProfit,
                    selectedLabelColor = CleanWhite,
                    containerColor = CharcoalCardSurface,
                    labelColor = TextWhiteSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("filter_delivered_orders")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Orders List or Empty State
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
                        .border(1.dp, CharcoalCardBorder, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_empty_kitchen),
                            contentDescription = null,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isNotBlank() || statusFilter != null) {
                                "لا توجد نتائج مطابقة لبحثك"
                            } else {
                                "لا توجد طلبات مسجلة في هذا الشهر حتى الآن"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleanWhite
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "سجل الآن أول طلبية لتبدأ في متابعة مبيعاتك وأرباحك بدقة!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhiteSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onAddOrderClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary),
                            modifier = Modifier.testTag("empty_state_add_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = CleanWhite, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل أوردر جديد", fontWeight = FontWeight.Bold, color = CleanWhite)
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
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    PremiumOrderCard(
                        order = order,
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

@Composable
fun PremiumOrderCard(
    order: Order,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onShare: () -> Unit
) {
    val isDelivered = order.status == OrderStatus.DELIVERED.name
    val isPaid = order.paymentStatus == PaymentStatus.PAID.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp))
            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar with initial, Customer Name, and Delivery Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFF3D00), BrandRedPrimary)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.customerName.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleanWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = order.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleanWhite
                        )
                        if (order.customerPhone.isNotBlank()) {
                            Text(
                                text = order.customerPhone,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhiteSecondary
                            )
                        } else {
                            Text(
                                text = DateUtils.formatDate(order.orderDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextWhiteSecondary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Status Badge
                Surface(
                    color = if (isDelivered) EmeraldProfitContainer else Color(0xFF382500),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDelivered) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (isDelivered) EmeraldProfit else Color(0xFFFFD54F),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isDelivered) "تم التسليم" else "قيد التجهيز",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDelivered) EmeraldProfitOnContainer else Color(0xFFFFD54F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meals Description bubble
            Surface(
                color = CharcoalCardElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = BrandRedPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.itemsDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                        color = CleanWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing & Profit Highlight Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = "قيمة الطلب",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhiteSecondary
                        )
                        Text(
                            text = DateUtils.formatCurrency(order.salePrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandRedPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .width(1.dp)
                            .background(CharcoalCardBorder)
                            .align(Alignment.CenterVertically)
                    )

                    Column {
                        Text(
                            text = "التكلفة",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhiteSecondary
                        )
                        Text(
                            text = DateUtils.formatCurrency(order.costPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = CleanWhite
                        )
                    }
                }

                // Profit Earned Pill
                Surface(
                    color = EmeraldProfitContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = EmeraldProfit,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "المكسب المحقق",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldProfitOnContainer
                            )
                            Text(
                                text = "+${DateUtils.formatCurrency(order.netProfit)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldProfit
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer with Payment badge & quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment indicator
                Surface(
                    color = if (isPaid) Color(0xFF143016) else Color(0xFF381212),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            tint = if (isPaid) EmeraldProfit else BrandRedPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPaid) "مدفوع بالكامل" else "غير مدفوع (آجل)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) EmeraldProfit else BrandRedPrimary
                        )
                    }
                }

                // Action buttons: Status toggle & WhatsApp share
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onToggleStatus,
                        modifier = Modifier
                            .size(34.dp)
                            .background(CharcoalCardElevated, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isDelivered) Icons.Default.HourglassEmpty else Icons.Default.Check,
                            contentDescription = "تغيير حالة التسليم",
                            tint = BrandRedPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF143016), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة الفاتورة",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
