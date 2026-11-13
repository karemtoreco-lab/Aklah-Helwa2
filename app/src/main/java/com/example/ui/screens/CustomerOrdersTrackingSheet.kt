package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderStatus
import com.example.model.TrackedCloudOrder
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrdersTrackingSheet(
    orders: List<TrackedCloudOrder>,
    onSubmitRating: (TrackedCloudOrder, Int) -> Unit,
    onReorder: (TrackedCloudOrder) -> Unit,
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
        modifier = Modifier.testTag("customer_orders_tracking_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.warmAccentGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "طلباتي ومتابعة التجهيز",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "تحديثات لحظية مباشرة من مطبخ أ/ نهى ماهر",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            if (orders.isEmpty()) {
                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا توجد طلبات مسجلة لحسابك حتى الآن",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أول طلب هيتسجل هنا وهتقدري تتابعي حالة تجهيزه وتوصيله خطوة بخطوة!",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(orders, key = { it.cloudId }) { order ->
                        TrackedOrderCard(
                            order = order,
                            onSubmitRating = { stars -> onSubmitRating(order, stars) },
                            onReorder = {
                                onReorder(order)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackedOrderCard(
    order: TrackedCloudOrder,
    onSubmitRating: (Int) -> Unit,
    onReorder: () -> Unit
) {
    val colors = AppTheme.colors
    val stepIndex = OrderStatus.trackingStepIndex(order.status)
    val isDelivered = order.status == OrderStatus.DELIVERED.name
    val isCancelled = order.status == OrderStatus.CANCELLED.name
    var selectedStars by remember(order.cloudId) { mutableIntStateOf(5) }

    Surface(
        color = colors.secondarySurface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (isDelivered) colors.profitGreen.copy(alpha = 0.45f) else colors.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tracked_order_card_${order.cloudId}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDate(order.orderDate),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Surface(
                    color = if (isCancelled) colors.primaryContainer else if (isDelivered) colors.profitContainer else colors.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = OrderStatus.trackingTitle(order.status),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCancelled) colors.primaryBrand else if (isDelivered) colors.onProfitContainer else colors.goldAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (!isCancelled) {
                OrderPipelineSteps(currentStep = stepIndex)
            }

            Text(
                text = order.itemsDescription,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (order.deliveryZone.isNotBlank()) {
                        Text(
                            text = "التوصيل: ${order.deliveryZone} (${DateUtils.formatCurrency(order.shippingFee)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        text = "الإجمالي: ${DateUtils.formatCurrencyWithDecimal(order.salePrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primaryBrand
                    )
                }

                if (order.lineItems.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onReorder,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.primaryBrand.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إعادة الطلب", color = colors.primaryBrand, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isDelivered && !order.isRated) {
                HorizontalDivider(color = colors.cardBorder)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "قيّمي جودة الأكل والتجهيز:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { selectedStars = star },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$star نجوم",
                                        tint = colors.goldAccent,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onSubmitRating(selectedStars) },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.profitGreen),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 42.dp)
                                .bounceClick()
                                .testTag("submit_order_rating_btn_${order.cloudId}")
                        ) {
                            Text("إرسال التقييم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else if (order.isRated) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("تم إرسال تقييمك بنجاح — شكراً لك ❤️", style = MaterialTheme.typography.labelSmall, color = colors.profitGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OrderPipelineSteps(currentStep: Int) {
    val colors = AppTheme.colors
    val steps = listOf("قيد المراجعة", "جاري التجهيز", "خرج للتوصيل", "تم التسليم")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, label ->
            val reached = idx <= currentStep
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (reached) colors.profitGreen else colors.cardBorder
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = if (reached) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (reached) colors.profitGreen else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
