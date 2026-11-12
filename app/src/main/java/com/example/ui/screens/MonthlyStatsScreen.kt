package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.viewmodel.DailyStatPoint
import com.example.viewmodel.MonthlyStats

@Composable
fun MonthlyStatsScreen(
    stats: MonthlyStats,
    selectedYearMonth: String,
    availableMonths: List<String>,
    onSelectMonth: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onShareReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitleArabic = DateUtils.formatYearMonthArabic(selectedYearMonth)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
            .testTag("monthly_stats_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Month Selector Bar with Checkered Ribbon
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
                .border(1.dp, CharcoalCardBorder, RoundedCornerShape(20.dp))
                .testTag("month_selector_card"),
            colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column {
                CheckeredRibbon(height = 5.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Next Month
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .size(38.dp)
                            .background(CharcoalCardElevated, CircleShape)
                            .testTag("next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "الشهر القادم",
                            tint = BrandRedPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = BrandRedPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "تقرير أرباح $monthTitleArabic",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CleanWhite
                            )
                            Text(
                                text = "مشروع أكلة حلوة",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextWhiteSecondary
                            )
                        }
                    }

                    // Previous Month
                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier
                            .size(38.dp)
                            .background(CharcoalCardElevated, CircleShape)
                            .testTag("prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الشهر السابق",
                            tint = BrandRedPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Executive Financial Highlight Cards (Sales & Net Profit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Total Sales
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp))
                    .border(1.dp, CharcoalCardBorder, RoundedCornerShape(22.dp))
                    .testTag("total_sales_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إجمالي المبيعات",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = DateUtils.formatCurrency(stats.totalSales),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandRedPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "مبيعات $monthTitleArabic",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextWhiteSecondary
                    )
                }
            }

            // Card 2: Net Profit
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp))
                    .border(1.dp, EmeraldProfit.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                    .testTag("net_profit_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142416))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "+${DateUtils.formatCurrency(stats.netProfit)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldProfit
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = EmeraldProfitContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "هامش ${String.format(java.util.Locale.US, "%.1f", stats.profitMarginPercent)}% ربح",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldProfitOnContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Costs and Profit Margin Gauge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                .border(1.dp, CharcoalCardBorder, RoundedCornerShape(20.dp))
                .testTag("costs_summary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي التكاليف والمصاريف:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhiteSecondary
                        )
                        Text(
                            text = DateUtils.formatCurrency(stats.totalCost),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleanWhite
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "متوسط ربح الأوردر:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhiteSecondary
                        )
                        Text(
                            text = DateUtils.formatCurrency(stats.averageOrderProfit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldProfit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profit Ratio Progress Bar
                val progressRatio = if (stats.totalSales > 0) {
                    (stats.netProfit / stats.totalSales).toFloat().coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldProfit,
                    trackColor = Color(0xFF381515)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "التكاليف: ${DateUtils.formatCurrency(stats.totalCost)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextWhiteSecondary
                    )
                    Text(
                        text = "الربح الصافي: ${DateUtils.formatCurrency(stats.netProfit)} (${String.format(java.util.Locale.US, "%.0f", stats.profitMarginPercent)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldProfit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order Count Breakdown Section
        Text(
            text = "📦 تفاصيل الطلبيات خلال الشهر:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = CleanWhite
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total
            StatCountChip(
                title = "إجمالي الطلبات",
                count = "${stats.totalOrdersCount}",
                color = CharcoalCardElevated,
                textColor = CleanWhite,
                icon = Icons.Default.ShoppingBag,
                modifier = Modifier.weight(1f)
            )

            // Delivered
            StatCountChip(
                title = "تم تسليمها",
                count = "${stats.deliveredCount}",
                color = EmeraldProfitContainer,
                textColor = EmeraldProfitOnContainer,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )

            // Pending
            StatCountChip(
                title = "قيد التجهيز",
                count = "${stats.pendingCount}",
                color = Color(0xFF382500),
                textColor = Color(0xFFFFD54F),
                icon = Icons.Default.HourglassTop,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Daily Activity Chart (Sales & Profit by Day)
        if (stats.dailyPoints.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                    .border(1.dp, CharcoalCardBorder, RoundedCornerShape(20.dp))
                    .testTag("daily_activity_chart"),
                colors = CardDefaults.cardColors(containerColor = CharcoalCardSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = BrandRedPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حركة المبيعات والأرباح اليومية",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CleanWhite
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(BrandRedPrimary, RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مبيعات", style = MaterialTheme.typography.labelSmall, color = TextWhiteSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(EmeraldProfit, RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("أرباح", style = MaterialTheme.typography.labelSmall, color = TextWhiteSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DailyBarChart(points = stats.dailyPoints)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // WhatsApp Share Monthly Report Button
        Button(
            onClick = onShareReport,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .testTag("share_monthly_report_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "مشاركة تقرير شهر $monthTitleArabic عبر واتساب",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun StatCountChip(
    title: String,
    count: String,
    color: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun DailyBarChart(
    points: List<DailyStatPoint>,
    modifier: Modifier = Modifier
) {
    val maxSales = points.maxOfOrNull { it.sales } ?: 1.0
    val effectiveMax = if (maxSales > 0) maxSales else 1.0

    var animateBars by remember { mutableStateOf(false) }
    LaunchedEffect(points) {
        animateBars = true
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            val salesRatio = (point.sales / effectiveMax).toFloat().coerceIn(0.08f, 1f)
            val profitRatio = (point.profit / effectiveMax).toFloat().coerceIn(0.05f, 1f)

            val animatedSalesHeight by animateDpAsState(
                targetValue = if (animateBars) (100 * salesRatio).dp else 8.dp,
                animationSpec = tween(durationMillis = 600),
                label = "bar_sales"
            )

            val animatedProfitHeight by animateDpAsState(
                targetValue = if (animateBars) (100 * profitRatio).dp else 6.dp,
                animationSpec = tween(durationMillis = 700),
                label = "bar_profit"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.width(38.dp)
            ) {
                // Bar representation
                Row(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Sales bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(animatedSalesHeight)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFF5252), BrandRedPrimary)
                                ),
                                RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                            )
                    )

                    // Profit bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(animatedProfitHeight)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF66BB6A), EmeraldProfit)
                                ),
                                RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Day Label
                Text(
                    text = "${point.dayNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextWhiteSecondary
                )
            }
        }
    }
}
