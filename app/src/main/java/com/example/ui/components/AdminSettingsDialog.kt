package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CloudConfig
import com.example.model.CloudSyncState
import com.example.model.DeliveryZone
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import java.util.UUID

@Composable
fun AdminSettingsDialog(
    isDarkMode: Boolean,
    currentPin: String,
    adminWhatsAppNumber: String,
    dailyOfferText: String,
    deliveryZones: List<DeliveryZone>,
    cloudConfig: CloudConfig,
    cloudSyncState: CloudSyncState,
    onToggleTheme: () -> Unit,
    onUpdatePin: (String) -> Unit,
    onUpdateWhatsAppNumber: (String) -> Unit,
    onUpdateDailyOffer: (String) -> Unit,
    onSaveDeliveryZones: (List<DeliveryZone>) -> Unit,
    onSyncMenuNow: () -> Unit,
    onReturnToCustomerStorefront: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var pinInput by remember(currentPin) { mutableStateOf(currentPin) }
    var whatsAppInput by remember(adminWhatsAppNumber) { mutableStateOf(adminWhatsAppNumber) }
    var offerInput by remember(dailyOfferText) { mutableStateOf(dailyOfferText) }
    var editableZones by remember(deliveryZones) { mutableStateOf(deliveryZones) }
    var newZoneName by remember { mutableStateOf("") }
    var newZoneFee by remember { mutableStateOf("") }

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
                .border(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(26.dp))
                .testTag("admin_settings_dialog"),
            color = colors.cardSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إعدادات الإدارة والمزامنة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "أكلة حلوة — بإدارة نهى ماهر",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.secondarySurface)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = colors.textPrimary)
                    }
                }

                // Theme Switch Card
                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("المظهر الليلي (Dark Mode)", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("تبديل بين الوضع الفاتح الدافئ والوضع الليلي الفاخر", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        Switch(checked = isDarkMode, onCheckedChange = { onToggleTheme() })
                    }
                }

                // Daily Offer Banner Editor Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.goldAccent.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                        .testTag("daily_offer_settings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("عرض اليوم للعملاء (Daily Offer)", fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        }
                        Text(
                            text = "اكتبي عرضاً مميزاً يظهر فوراً في بانر متوهج أعلى منيو العملاء (أو اتركيه فارغاً لإخفائه).",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        OutlinedTextField(
                            value = offerInput,
                            onValueChange = { offerInput = it },
                            label = { Text("نص عرض اليوم (مثال: خصم 15% على صواني المحشي)") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_daily_offer_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardSurface,
                                unfocusedContainerColor = colors.cardSurface,
                                focusedBorderColor = colors.goldAccent,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onUpdateDailyOffer(offerInput.trim()) },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .bounceClick()
                                    .testTag("save_daily_offer_btn")
                            ) {
                                Text("حفظ عرض اليوم", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            if (offerInput.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        offerInput = ""
                                        onUpdateDailyOffer("")
                                    },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text("إخفاء", color = colors.textSecondary)
                                }
                            }
                        }
                    }
                }

                // Delivery Zones & Shipping Fees CRUD Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.secondaryAccent.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                        .testTag("delivery_zones_settings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colors.secondaryAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مناطق التوصيل ورسوم الشحن", fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        }
                        editableZones.forEachIndexed { idx, zone ->
                            Surface(
                                color = colors.cardSurface,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, colors.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(zone.name, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = if (zone.fee <= 0.0) "مجاني" else DateUtils.formatCurrency(zone.fee),
                                            color = colors.goldAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (editableZones.size > 1) {
                                        IconButton(
                                            onClick = {
                                                editableZones = editableZones.filterIndexed { i, _ -> i != idx }
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف المنطقة", tint = colors.primaryBrand)
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newZoneName,
                                onValueChange = { newZoneName = it },
                                label = { Text("اسم المنطقة") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1.4f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.cardSurface,
                                    unfocusedContainerColor = colors.cardSurface,
                                    focusedBorderColor = colors.primaryBrand,
                                    unfocusedBorderColor = colors.cardBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )
                            OutlinedTextField(
                                value = newZoneFee,
                                onValueChange = { newZoneFee = it },
                                label = { Text("الشحن") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(0.8f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.cardSurface,
                                    unfocusedContainerColor = colors.cardSurface,
                                    focusedBorderColor = colors.primaryBrand,
                                    unfocusedBorderColor = colors.cardBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (newZoneName.isNotBlank()) {
                                        val fee = (newZoneFee.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                                        editableZones = editableZones + DeliveryZone(
                                            id = UUID.randomUUID().toString(),
                                            name = newZoneName.trim(),
                                            fee = fee
                                        )
                                        newZoneName = ""
                                        newZoneFee = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryBrand)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "إضافة منطقة", tint = Color.White)
                            }
                        }

                        Button(
                            onClick = { onSaveDeliveryZones(editableZones) },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick()
                                .testTag("save_delivery_zones_btn")
                        ) {
                            Text("حفظ مناطق التوصيل", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // PIN Management Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تغيير الرقم السري للإدارة", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("الرقم السري (4+ أرقام)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_pin_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.cardSurface,
                                    unfocusedContainerColor = colors.cardSurface,
                                    focusedBorderColor = colors.primaryBrand,
                                    unfocusedBorderColor = colors.cardBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )
                            Button(
                                onClick = { onUpdatePin(pinInput) },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .bounceClick()
                            ) {
                                Text("حفظ", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // WhatsApp Order Reception Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.profitGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .testTag("whatsapp_settings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعدادات استقبال الطلبات عبر واتساب",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                        }
                        Text(
                            text = "أدخلي رقم واتساب مطبخ أ/ نهى ماهر بكود الدولة (مثال: 2010xxxxxxxxx أو 010xxxxxxxx) لتوجيه طلبات العملاء مباشرة إلى واتسابك.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = whatsAppInput,
                                onValueChange = { whatsAppInput = it },
                                label = { Text("رقم واتساب الإدارة (مثال: 2010xxxxxxxxx)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF25D366))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_whatsapp_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.cardSurface,
                                    unfocusedContainerColor = colors.cardSurface,
                                    focusedBorderColor = Color(0xFF25D366),
                                    unfocusedBorderColor = colors.cardBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )
                            Button(
                                onClick = { onUpdateWhatsAppNumber(whatsAppInput) },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .bounceClick()
                                    .testTag("save_whatsapp_btn")
                            ) {
                                Text("حفظ", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Read-Only Automatic Firebase Cloud Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                        .testTag("cloud_settings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (cloudSyncState is CloudSyncState.Error) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = if (cloudSyncState is CloudSyncState.Error) colors.primaryBrand else colors.profitGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعدادات الربط السحابي",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                        }

                        Surface(
                            color = if (cloudSyncState is CloudSyncState.Error) colors.primaryContainer else colors.profitContainer,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (cloudSyncState is CloudSyncState.Error) colors.primaryBrand.copy(alpha = 0.35f) else colors.profitGreen.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cloud_readonly_status_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (cloudSyncState is CloudSyncState.Error) Icons.Default.CloudOff else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (cloudSyncState is CloudSyncState.Error) colors.primaryBrand else colors.profitGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (cloudSyncState) {
                                            is CloudSyncState.Connected -> "متصل بالسحابة تلقائياً (${cloudSyncState.projectId})"
                                            is CloudSyncState.Error -> cloudSyncState.statusMessage
                                            else -> "متصل بالسحابة تلقائياً (${cloudConfig.projectId})"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (cloudSyncState is CloudSyncState.Error) colors.onPrimaryContainer else colors.onProfitContainer
                                    )
                                    Text(
                                        text = cloudSyncState.lastError
                                            ?: "تتم مزامنة المنيو والصور وطلبات العملاء وإعدادات الواتساب تلقائياً بين جميع الأجهزة.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (cloudSyncState is CloudSyncState.Error) colors.primaryBrand else colors.textSecondary
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onSyncMenuNow,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.profitGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick()
                                .testTag("sync_menu_now_btn")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مزامنة البيانات الآن", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                HorizontalDivider(color = colors.cardBorder)

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onReturnToCustomerStorefront()
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colors.primaryBrand.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .bounceClick()
                        .testTag("switch_role_btn")
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = colors.primaryBrand, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("العودة لواجهة العملاء", color = colors.primaryBrand, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "أكلة حلوة — بإدارة نهى ماهر",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
