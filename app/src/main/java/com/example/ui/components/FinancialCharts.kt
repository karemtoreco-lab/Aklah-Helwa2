package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyStatPoint
import com.example.model.TopProductStat
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalesAndProfitChartCard(
    points: List<DailyStatPoint>,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
            .testTag("sales_profit_chart_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 3.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "منحنى المبيعات والأرباح",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendDot("المبيعات", colors.primaryBrand)
                    LegendDot("الربح", colors.profitGreen)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (points.isEmpty()) {
                EmptyChartHint("لا توجد مبيعات مسجلة لعرض المنحنى البياني بعد")
            } else {
                var animateTrigger by remember { mutableStateOf(false) }
                LaunchedEffect(points) { animateTrigger = true }
                val animProgress by animateFloatAsState(
                    targetValue = if (animateTrigger) 1f else 0f,
                    animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
                    label = "chart_curve_progress"
                )
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                val orderedPoints = remember(points, isRtl) {
                    if (isRtl) points.sortedByDescending { it.dayNumber } else points.sortedBy { it.dayNumber }
                }

                CurvedDualAreaChart(
                    valuesA = orderedPoints.map { it.sales.toFloat() },
                    valuesB = orderedPoints.map { it.grossProfit.toFloat() },
                    colorA = colors.primaryBrand,
                    colorB = colors.profitGreen,
                    gridColor = colors.cardBorder,
                    dotCenterColor = colors.cardSurface,
                    progress = animProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    orderedPoints.take(7).forEach { pt ->
                        Surface(
                            color = colors.secondarySurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, colors.cardBorder)
                        ) {
                            Text(
                                text = "يوم ${pt.dayNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
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
fun OrdersVolumeChartCard(
    points: List<DailyStatPoint>,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
            .testTag("orders_over_time_chart_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 3.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = colors.secondaryAccent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تدفق الأوردرات خلال الشهر",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                }
                LegendDot("عدد الأوردرات", colors.secondaryAccent)
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (points.isEmpty()) {
                EmptyChartHint("لا توجد أوردرات مسجلة بعد")
            } else {
                var animateTrigger by remember { mutableStateOf(false) }
                LaunchedEffect(points) { animateTrigger = true }
                val animProgress by animateFloatAsState(
                    targetValue = if (animateTrigger) 1f else 0f,
                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                    label = "orders_curve_progress"
                )
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                val orderedPoints = remember(points, isRtl) {
                    if (isRtl) points.sortedByDescending { it.dayNumber } else points.sortedBy { it.dayNumber }
                }

                CurvedSingleAreaChart(
                    values = orderedPoints.map { it.ordersCount.toFloat() },
                    strokeColor = colors.secondaryAccent,
                    dotColor = colors.goldAccent,
                    dotCenterColor = colors.cardSurface,
                    progress = animProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                )
            }
        }
    }
}

@Composable
private fun CurvedDualAreaChart(
    valuesA: List<Float>,
    valuesB: List<Float>,
    colorA: Color,
    colorB: Color,
    gridColor: Color,
    dotCenterColor: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padTop = 14f
        val padBottom = 12f
        val chartH = (h - padTop - padBottom).coerceAtLeast(10f)

        val gridPaintColor = gridColor.copy(alpha = 0.85f)
        for (i in 0..3) {
            val y = padTop + chartH * (i / 3f)
            drawLine(
                color = gridPaintColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
        }

        val maxVal = (valuesA + valuesB).maxOrNull()?.coerceAtLeast(1f) ?: 1f

        fun buildCoords(vals: List<Float>): List<Offset> {
            if (vals.size == 1) {
                val y = padTop + chartH * (1f - (vals[0] / maxVal) * progress)
                return listOf(Offset(0f, y), Offset(w, y))
            }
            val stepX = w / (vals.size - 1).coerceAtLeast(1)
            return vals.mapIndexed { idx, v ->
                val x = idx * stepX
                val ratio = ((v / maxVal).coerceIn(0f, 1f)) * progress
                val y = padTop + chartH * (1f - ratio)
                Offset(x, y)
            }
        }

        val coordsA = buildCoords(valuesA)
        val coordsB = buildCoords(valuesB)

        drawSmoothAreaAndLine(coordsA, colorA, h - padBottom)
        drawSmoothAreaAndLine(coordsB, colorB, h - padBottom)

        coordsA.forEach { pt ->
            drawCircle(color = dotCenterColor, radius = 6f, center = pt)
            drawCircle(color = colorA, radius = 4f, center = pt)
        }
        coordsB.forEach { pt ->
            drawCircle(color = dotCenterColor, radius = 6f, center = pt)
            drawCircle(color = colorB, radius = 4f, center = pt)
        }
    }
}

@Composable
private fun CurvedSingleAreaChart(
    values: List<Float>,
    strokeColor: Color,
    dotColor: Color,
    dotCenterColor: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padTop = 14f
        val padBottom = 10f
        val chartH = (h - padTop - padBottom).coerceAtLeast(10f)
        val maxVal = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f

        val coords = if (values.size == 1) {
            val y = padTop + chartH * (1f - (values[0] / maxVal) * progress)
            listOf(Offset(0f, y), Offset(w, y))
        } else {
            val stepX = w / (values.size - 1).coerceAtLeast(1)
            values.mapIndexed { idx, v ->
                val ratio = (v / maxVal).coerceIn(0f, 1f) * progress
                Offset(idx * stepX, padTop + chartH * (1f - ratio))
            }
        }

        drawSmoothAreaAndLine(coords, strokeColor, h - padBottom)
        coords.forEach { pt ->
            drawCircle(color = dotCenterColor, radius = 6.5f, center = pt)
            drawCircle(color = dotColor, radius = 4.5f, center = pt)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmoothAreaAndLine(
    coords: List<Offset>,
    lineColor: Color,
    baselineY: Float
) {
    if (coords.size < 2) return
    val linePath = Path().apply {
        moveTo(coords.first().x, coords.first().y)
        for (i in 0 until coords.lastIndex) {
            val p0 = coords[i]
            val p1 = coords[i + 1]
            val cx = (p0.x + p1.x) / 2f
            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
        }
    }

    val areaPath = Path().apply {
        addPath(linePath)
        lineTo(coords.last().x, baselineY)
        lineTo(coords.first().x, baselineY)
        close()
    }

    drawPath(
        path = areaPath,
        brush = Brush.verticalGradient(
            colors = listOf(lineColor.copy(alpha = 0.32f), lineColor.copy(alpha = 0.02f)),
            startY = 0f,
            endY = baselineY
        )
    )

    drawPath(
        path = linePath,
        color = lineColor,
        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

@Composable
fun ProductPerformanceCard(
    topProducts: List<TopProductStat>,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
            .testTag("product_performance_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 3.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الأصناف الأكثر مبيعاً وأداءً",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            if (topProducts.isEmpty()) {
                EmptyChartHint("لم يتم بيع أصناف خلال هذا الشهر بعد")
            } else {
                val maxQty = topProducts.maxOfOrNull { it.totalQuantity }?.coerceAtLeast(1.0) ?: 1.0
                var animateBars by remember { mutableStateOf(false) }
                LaunchedEffect(topProducts) { animateBars = true }

                Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    topProducts.take(6).forEachIndexed { index, prod ->
                        val targetRatio = (prod.totalQuantity / maxQty).toFloat().coerceIn(0.08f, 1f)
                        val ratio by animateFloatAsState(
                            targetValue = if (animateBars) targetRatio else 0.05f,
                            animationSpec = tween(600, easing = FastOutSlowInEasing),
                            label = "qty_bar"
                        )
                        val qtyText = if (prod.totalQuantity % 1.0 == 0.0) {
                            prod.totalQuantity.toInt().toString()
                        } else {
                            prod.totalQuantity.toString()
                        }
                        val rankColor = when (index) {
                            0 -> colors.goldAccent
                            1 -> colors.primaryBrand
                            2 -> colors.profitGreen
                            else -> colors.textSecondary
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(rankColor.copy(alpha = 0.16f))
                                            .border(1.dp, rankColor.copy(alpha = 0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = rankColor,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = prod.productName,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = colors.profitContainer,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "$qtyText وحدة • +${DateUtils.formatCurrency(prod.totalGrossProfit)}",
                                        color = colors.onProfitContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(colors.secondarySurface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .height(9.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(colors.primaryBrand, colors.secondaryAccent, colors.goldAccent)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChartHint(message: String) {
    val colors = AppTheme.colors
    Surface(
        color = colors.secondarySurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
    }
}
