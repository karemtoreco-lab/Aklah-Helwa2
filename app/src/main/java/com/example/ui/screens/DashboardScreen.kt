package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.ui.components.CheckeredRibbon
import com.example.ui.components.QuickOrderEntryCard
import com.example.ui.theme.BrandRedPrimary
import com.example.ui.theme.CharcoalCardBorder
import com.example.ui.theme.CharcoalCardSurface
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitContainer
import com.example.ui.theme.EmeraldProfitOnContainer
import com.example.ui.theme.RedGradient
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.util.DateUtils
import com.example.viewmodel.MonthlyStats
import java.util.Calendar

@Composable
fun DashboardScreen(
    stats: MonthlyStats,
    selectedYearMonth: String,
    recentOrders: List<Order>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onOpenAddOrderDialog: () -> Unit,
    onOrderClick: (Order) -> Unit,
    onSaveQuickOrder: (
        customerName: String,
        orderValue: Double,
        profitEarned: Double,
        itemsDescription: String,
        customerPhone: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = DateUtils.formatYearMonthArabic(selectedYearMonth)
    val greeting = remember { getGreeting() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
            .testTag("dashboard_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Personalized Welcome Header Card with Checkered Accent
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                .border(1.dp, CharcoalCardBorder, RoundedCornerShape(24.dp))
                .testTag("dashboard_welcome_header"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface)
        ) {
            Column {
                CheckeredRibbon(height = 5.dp)

                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = greeting,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandRedPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = EmeraldProfitContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "المطبخ نشط 🍳",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldProfitOnContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "مشروع أكلة حلوة • لوحة التحكم",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = CleanWhite
                            )
                        }

                        // Month navigator compact pill
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalCardBorder)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = onNextMonth,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "التالي",
                                        tint = BrandRedPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = monthTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CleanWhite
                                )
                                IconButton(
                                    onClick = onPreviousMonth,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "السابق",
                                        tint = BrandRedPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Culinary Hero Banner Card with photography and red brush accent
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, BrandRedPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .testTag("dashboard_hero_banner"),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_food_banner),
                    contentDescription = "أشهى الأكلات المنزلية",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark & Rich Red Gradient overlay for contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.88f),
                                    Color(0xFF2E1010).copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Content inside Hero Banner
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = BrandRedPrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "أكلة حلوة 🍲",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "طعم بيتي أصيل • أرباح محسوبة بدقة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleanWhite
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "إدارة متكاملة للأوردرات والمبيعات والأرباح الشهرية",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanWhite.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Section Title: نظرة مالية عامة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ملخص $monthTitle المالي 💰",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleanWhite
            )

            TextButton(onClick = onNavigateToAnalytics) {
                Text("عرض التحليلات", style = MaterialTheme.typography.labelMedium, color = BrandRedPrimary)
                Spacer(modifier = Modifier.width(2.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BrandRedPrimary, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Financial Summary Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total Revenue Card (إجمالي المبيعات)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
                    .border(1.dp, CharcoalCardBorder, RoundedCornerShape(20.dp))
                    .testTag("dashboard_revenue_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إجمالي المبيعات",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhiteSecondary
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF381515), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = BrandRedPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = DateUtils.formatCurrency(stats.totalSales),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandRedPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${stats.totalOrdersCount} طلب هذا الشهر",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextWhiteSecondary
                    )
                }
            }

            // Net Profit Card (صافي الربح)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
                    .border(1.dp, EmeraldProfit.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .testTag("dashboard_profit_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142416))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "صافي الأرباح",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldProfit
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(EmeraldProfitContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = EmeraldProfit,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "+${DateUtils.formatCurrency(stats.netProfit)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldProfit
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = EmeraldProfitContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "هامش ${String.format(java.util.Locale.US, "%.1f", stats.profitMarginPercent)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldProfitOnContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Total Cost & Average Order Profit Strip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                .border(1.dp, CharcoalCardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF381515), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = BrandRedPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "إجمالي التكاليف",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhiteSecondary
                        )
                        Text(
                            text = DateUtils.formatCurrency(stats.totalCost),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CleanWhite
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(CharcoalCardBorder)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(EmeraldProfitContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = EmeraldProfit,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "متوسط ربح الطلب",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhiteSecondary
                        )
                        Text(
                            text = DateUtils.formatCurrency(stats.averageOrderProfit),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldProfit
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Quick Action Button Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // New Order Button with Red Gradient
            Button(
                onClick = onOpenAddOrderDialog,
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRedPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = CleanWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("أوردر جديد", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CleanWhite)
            }

            // View Menu
            Button(
                onClick = onNavigateToMenu,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = CleanWhite
                )
            ) {
                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = BrandRedPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("المنيو", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // View Orders
            Button(
                onClick = onNavigateToOrders,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = CleanWhite
                )
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CleanWhite, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("الطلبيات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Fast Order Entry Form Card
        QuickOrderEntryCard(
            onSaveOrder = onSaveQuickOrder,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 7. Recent Orders Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "أحدث طلبات المطبخ 🍽️",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleanWhite
            )

            TextButton(onClick = onNavigateToOrders) {
                Text("عرض الكل (${recentOrders.size})", style = MaterialTheme.typography.labelMedium, color = BrandRedPrimary)
                Spacer(modifier = Modifier.width(2.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BrandRedPrimary, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recentOrders.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, CharcoalCardBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_empty_kitchen),
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "لا توجد طلبات مسجلة في هذا الشهر حتى الآن",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleanWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سجل أول طلبية باستخدام النموذج أعلاه لتبدأ الأرباح!",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextWhiteSecondary
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recentOrders.take(4).forEach { order ->
                    RecentOrderItem(
                        order = order,
                        onClick = { onOrderClick(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentOrderItem(
    order: Order,
    onClick: () -> Unit
) {
    val isDelivered = order.status == OrderStatus.DELIVERED.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("recent_order_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Initial Avatar with red gradient
                Box(
                    modifier = Modifier
                        .size(42.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = order.customerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CleanWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Mini status dot
                        Surface(
                            color = if (isDelivered) EmeraldProfitContainer else Color(0xFF382500),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isDelivered) "تم التسليم" else "قيد التجهيز",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDelivered) EmeraldProfitOnContainer else Color(0xFFFFD54F),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = order.itemsDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhiteSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Pricing Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateUtils.formatCurrency(order.salePrice),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandRedPrimary
                )
                Text(
                    text = "+${DateUtils.formatCurrency(order.netProfit)} ربح",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldProfit
                )
            }
        }
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..11 -> "صباح الخير والبركة ☀️"
        in 12..16 -> "مساء النور والبهجة 🍲"
        else -> "مساء الخير والسعادة 🌙"
    }
}
