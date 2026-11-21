package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Order
import com.example.model.OrderLineItemEntity
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderDetailDialog(
    order: Order,
    lineItems: List<OrderLineItemEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelectStatus: (String) -> Unit,
    onTogglePayment: () -> Unit,
    receiptText: String
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = colors.cardSurface,
            title = { Text("حذف الأوردر", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = {
                Text(
                    "هل أنت متأكد من رغبتك في حذف أوردر ${order.customerName}؟ لا يمكن التراجع عن هذا الإجراء.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).bounceClick()
                ) {
                    Text("نعم، احذف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("إلغاء", color = colors.textSecondary)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(14.dp)
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .border(1.2.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(28.dp))
                .testTag("order_detail_dialog"),
            color = colors.cardSurface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header Card
                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .shadow(6.dp, CircleShape, spotColor = colors.primaryBrand.copy(alpha = 0.35f))
                                    .background(colors.warmAccentGradient, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = order.customerName.take(1).ifBlank { "ع" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = order.customerName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = DateUtils.formatDate(order.orderDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.cardSurface)
                                .border(1.dp, colors.cardBorder, CircleShape)
                                .bounceClick(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (order.isFromCustomer || order.deliveryAddress.isNotBlank() || order.deliveryZone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = colors.profitContainer,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (order.isFromCustomer) {
                                Text(
                                    text = "طلب مرسل من قسم العملاء 🛍️",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.onProfitContainer
                                )
                            }
                            if (order.deliveryZone.isNotBlank()) {
                                Text(
                                    text = "منطقة التوصيل: ${order.deliveryZone} (شحن: ${DateUtils.formatCurrency(order.shippingFee)})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                            if (order.deliveryAddress.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "عنوان التوصيل: ${order.deliveryAddress}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current status & payment summary chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isDelivered = order.status == OrderStatus.DELIVERED.name
                    val statusLabel = OrderStatus.trackingTitle(order.status)
                    Surface(
                        color = if (isDelivered) colors.profitContainer else colors.secondaryContainer,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (isDelivered) colors.profitGreen.copy(alpha = 0.4f) else colors.goldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isDelivered) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (isDelivered) colors.profitGreen else colors.goldAccent,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isDelivered) colors.onProfitContainer else colors.goldAccent
                            )
                        }
                    }

                    val isPaid = order.paymentStatus == PaymentStatus.PAID.name
                    val paymentLabel = PaymentStatus.entries.find { it.name == order.paymentStatus }?.titleAr ?: order.paymentStatus
                    Surface(
                        color = if (isPaid) colors.profitContainer else colors.primaryContainer,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (isPaid) colors.profitGreen.copy(alpha = 0.4f) else colors.primaryBrand.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = if (isPaid) colors.profitGreen else colors.primaryBrand,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = paymentLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaid) colors.onProfitContainer else colors.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تحديث حالة التجهيز والتوصيل للعميل:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val trackingOptions = listOf(
                        OrderStatus.PENDING_REVIEW to "قيد المراجعة",
                        OrderStatus.PREPARING to "جاري التجهيز",
                        OrderStatus.OUT_FOR_DELIVERY to "خرج للتوصيل",
                        OrderStatus.DELIVERED to "تم التسليم",
                        OrderStatus.CANCELLED to "ملغي"
                    )
                    trackingOptions.forEach { (st, title) ->
                        val isSelected = OrderStatus.trackingTitle(order.status) == title
                        CulinaryPillChip(
                            label = title,
                            selected = isSelected,
                            selectedColor = if (st == OrderStatus.DELIVERED) colors.profitGreen else colors.primaryBrand,
                            onClick = { onSelectStatus(st.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي البيع للعميل:", color = colors.textSecondary)
                            Text(
                                text = DateUtils.formatCurrencyWithDecimal(order.salePrice),
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primaryBrand,
                                fontSize = 16.sp
                            )
                        }
                        if (order.shippingFee > 0.0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("رسوم التوصيل (${order.deliveryZone}):", color = colors.textSecondary)
                                Text(DateUtils.formatCurrencyWithDecimal(order.shippingFee), fontWeight = FontWeight.SemiBold, color = colors.goldAccent)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي تكلفة الشراء:", color = colors.textSecondary)
                            Text(
                                text = DateUtils.formatCurrencyWithDecimal(order.costPrice),
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = colors.cardBorder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(colors.profitContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = colors.profitGreen, modifier = Modifier.size(17.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إجمالي ربح الأوردر:", fontWeight = FontWeight.ExtraBold, color = colors.onProfitContainer)
                            }
                            Surface(
                                color = colors.profitContainer,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = DateUtils.formatCurrencyWithDecimal(order.netProfit),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = colors.profitGreen,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "الأصناف المسجلة بالأوردر",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (lineItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lineItems.forEachIndexed { idx, li ->
                            val qtyStr = if (li.quantity % 1.0 == 0.0) li.quantity.toInt().toString() else li.quantity.toString()
                            StaggeredAppearItem(index = idx) {
                                Surface(
                                    color = colors.background,
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.dp, colors.cardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ProductThumbnail(
                                            imagePath = li.productImagePathSnapshot,
                                            contentDescription = li.productNameSnapshot,
                                            size = 48.dp,
                                            cornerRadius = 14.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "$qtyStr × ${li.productNameSnapshot}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "شراء/وحدة: ${DateUtils.formatCurrencyWithDecimal(li.unitPurchaseCost)} • بيع/وحدة: ${DateUtils.formatCurrencyWithDecimal(li.unitSellingPrice)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.textSecondary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Surface(color = colors.primaryContainer, shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = DateUtils.formatCurrencyWithDecimal(li.totalSellingPrice),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "+${DateUtils.formatCurrencyWithDecimal(li.itemGrossProfit)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.profitGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        color = colors.background,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = order.itemsDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (order.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ملاحظات:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(order.notes, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onTogglePayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .bounceClick(),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Text(
                        text = if (order.paymentStatus == PaymentStatus.PAID.name) "تغيير الدفع إلى (غير مدفوع)" else "تأكيد الدفع بالكامل",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (order.customerPhone.isNotBlank()) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${order.customerPhone}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اتصال", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة الفاتورة"))
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .defaultMinSize(minHeight = 48.dp)
                            .bounceClick(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.profitGreen)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة واتساب", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.primaryBrand)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حذف الأوردر", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondarySurface, contentColor = colors.textPrimary),
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp).bounceClick()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تعديل الأوردر", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
