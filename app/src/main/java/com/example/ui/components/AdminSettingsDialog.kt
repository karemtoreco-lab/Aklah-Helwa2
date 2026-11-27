package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.model.AppUser
import com.example.model.AppVersionConfig
import com.example.model.CloudConfig
import com.example.model.CloudSyncState
import com.example.model.DeliveryZone
import com.example.ui.theme.AppTheme
import com.example.ui.theme.LightTextPrimary
import com.example.util.DateUtils
import java.util.UUID

@Composable
private fun SettingsSectionBadge(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.16f))
            .border(1.dp, tint.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun AdminSettingsDialog(
    isDarkMode: Boolean,
    currentPin: String,
    adminWhatsAppNumber: String,
    dailyOfferText: String,
    deliveryZones: List<DeliveryZone>,
    appVersionConfig: AppVersionConfig?,
    cloudConfig: CloudConfig,
    cloudSyncState: CloudSyncState,
    appUsers: List<AppUser> = emptyList(),
    isForceResyncing: Boolean = false,
    forceResyncFeedback: Pair<Boolean, String>? = null,
    onToggleTheme: () -> Unit,
    onUpdatePin: (String) -> Unit,
    onUpdateWhatsAppNumber: (String) -> Unit,
    onUpdateDailyOffer: (String) -> Unit,
    onSaveDeliveryZones: (List<DeliveryZone>) -> Unit,
    onPublishAppVersion: (Int, String, String, String, Boolean) -> Unit,
    onSyncMenuNow: () -> Unit,
    onForceResyncNow: () -> Unit = {},
    onDeleteAppUser: (AppUser) -> Unit = {},
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
    var usersSectionExpanded by remember { mutableStateOf(false) }
    var userPendingDeletion by remember { mutableStateOf<AppUser?>(null) }
    var versionCodeInput by remember(appVersionConfig) {
        mutableStateOf((appVersionConfig?.latestVersionCode ?: (BuildConfig.VERSION_CODE + 1)).toString())
    }
    var versionNameInput by remember(appVersionConfig) {
        mutableStateOf(appVersionConfig?.latestVersionName?.ifBlank { "1.0.1" } ?: "1.0.1")
    }
    var downloadUrlInput by remember(appVersionConfig) {
        mutableStateOf(appVersionConfig?.downloadUrl.orEmpty())
    }
    var releaseNotesInput by remember(appVersionConfig) {
        mutableStateOf(appVersionConfig?.releaseNotes.orEmpty())
    }
    var isMandatoryUpdate by remember(appVersionConfig) {
        mutableStateOf(appVersionConfig?.isMandatory ?: false)
    }

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
                .clip(RoundedCornerShape(28.dp))
                .border(1.2.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder, RoundedCornerShape(28.dp))
                .testTag("admin_settings_dialog"),
            color = colors.cardSurface,
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(20.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(colors.warmAccentGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = colors.onAccentText, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إعدادات الإدارة والمزامنة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "أكلة حلوة — بإدارة نهى ماهر",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.goldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.cardSurface)
                                .border(1.dp, colors.cardBorder, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = colors.textPrimary)
                        }
                    }
                }

                // Theme Switch Card
                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SettingsSectionBadge(Icons.Default.DarkMode, colors.primaryBrand)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("المظهر الليلي (Dark Mode)", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text("تبديل بين الوضع الفاتح الدافئ والوضع الليلي الفاخر", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                        }
                        Switch(checked = isDarkMode, onCheckedChange = { onToggleTheme() })
                    }
                }

                // Daily Offer Banner Editor Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.goldAccent.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                        .testTag("daily_offer_settings_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsSectionBadge(Icons.Default.Campaign, colors.goldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
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
                            shape = RoundedCornerShape(16.dp),
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
                                Text("حفظ عرض اليوم", color = colors.onAccentText, fontWeight = FontWeight.Bold)
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
                        .border(1.dp, colors.secondaryAccent.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                        .testTag("delivery_zones_settings_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsSectionBadge(Icons.Default.LocalShipping, colors.secondaryAccent)
                            Spacer(modifier = Modifier.width(8.dp))
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
                                Icon(Icons.Default.Add, contentDescription = "إضافة منطقة", tint = colors.onAccentText)
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
                            Text("حفظ مناطق التوصيل", color = LightTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // PIN Management Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsSectionBadge(Icons.Default.Lock, colors.primaryBrand)
                            Spacer(modifier = Modifier.width(8.dp))
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
                                Text("حفظ", color = colors.onAccentText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // WhatsApp Order Reception Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.profitGreen.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                        .testTag("whatsapp_settings_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsSectionBadge(Icons.Default.Chat, colors.profitGreen)
                            Spacer(modifier = Modifier.width(8.dp))
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
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = colors.profitGreen)
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
                                    focusedBorderColor = colors.profitGreen,
                                    unfocusedBorderColor = colors.cardBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )
                            Button(
                                onClick = { onUpdateWhatsAppNumber(whatsAppInput) },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.profitGreen),
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
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .testTag("cloud_settings_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SettingsSectionBadge(
                                icon = if (cloudSyncState is CloudSyncState.Error) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                tint = if (cloudSyncState is CloudSyncState.Error) colors.errorRed else colors.profitGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إعدادات الربط السحابي",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                        }

                        Surface(
                            color = if (cloudSyncState is CloudSyncState.Error) colors.errorContainer else colors.profitContainer,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (cloudSyncState is CloudSyncState.Error) colors.errorRed.copy(alpha = 0.35f) else colors.profitGreen.copy(alpha = 0.4f)
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
                                    tint = if (cloudSyncState is CloudSyncState.Error) colors.errorRed else colors.profitGreen,
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
                                        color = if (cloudSyncState is CloudSyncState.Error) colors.onErrorContainer else colors.onProfitContainer
                                    )
                                    Text(
                                        text = cloudSyncState.lastError
                                            ?: "تتم مزامنة المنيو والصور وطلبات العملاء وإعدادات الواتساب تلقائياً بين جميع الأجهزة.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (cloudSyncState is CloudSyncState.Error) colors.onErrorContainer else colors.textSecondary
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onSyncMenuNow,
                            enabled = !isForceResyncing,
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
                            Text("رفع ومزامنة البيانات الآن", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }

                        OutlinedButton(
                            onClick = onForceResyncNow,
                            enabled = !isForceResyncing,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.2.dp, colors.primaryBrand),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick()
                                .testTag("force_resync_btn")
                        ) {
                            if (isForceResyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.2.dp,
                                    color = colors.primaryBrand
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "جاري سحب وتحديث البيانات...",
                                    color = colors.primaryBrand,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = colors.primaryBrand,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "إعادة مزامنة شاملة (طوارئ)",
                                    color = colors.primaryBrand,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = forceResyncFeedback != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            forceResyncFeedback?.let { (isSuccess, message) ->
                                Surface(
                                    color = if (isSuccess) colors.profitContainer else colors.errorContainer,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSuccess) colors.profitGreen.copy(alpha = 0.4f) else colors.errorRed.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("force_resync_feedback_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = if (isSuccess) colors.profitGreen else colors.errorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = message,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSuccess) colors.onProfitContainer else colors.onErrorContainer,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Registered App Accounts Card (Delete Test / Registered Accounts)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .testTag("settings_registered_users_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick { usersSectionExpanded = !usersSectionExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                SettingsSectionBadge(Icons.Default.Group, colors.primaryBrand)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "إدارة الحسابات المسجلة (${appUsers.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "اضغطي لحذف أي حساب تجريبي نهائياً من السحابة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (usersSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "عرض أو إخفاء الحسابات",
                                tint = colors.textSecondary
                            )
                        }

                        AnimatedVisibility(visible = usersSectionExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                HorizontalDivider(color = colors.cardBorder)
                                if (appUsers.isEmpty()) {
                                    Text(
                                        text = "لا توجد حسابات مسجلة على السحابة حالياً",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    val now = System.currentTimeMillis()
                                    appUsers.forEach { user ->
                                        val isLive = user.isLiveOnline(now)
                                        Surface(
                                            color = colors.cardSurface,
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, if (isLive) colors.profitGreen.copy(alpha = 0.4f) else colors.cardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = user.username,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.textPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    val joined = if (user.joinedAt > 0L) DateUtils.formatDate(user.joinedAt) else "—"
                                                    Text(
                                                        text = "${if (isLive) "متصل الآن" else "غير متصل"} • انضم: $joined • طلبات: ${user.ordersCount}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isLive) colors.profitGreen else colors.textSecondary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    onClick = { userPendingDeletion = user },
                                                    color = colors.errorContainer,
                                                    shape = RoundedCornerShape(50),
                                                    border = BorderStroke(1.dp, colors.errorRed.copy(alpha = 0.35f)),
                                                    modifier = Modifier
                                                        .minimumInteractiveComponentSize()
                                                        .defaultMinSize(minHeight = 36.dp)
                                                        .bounceClick()
                                                        .testTag("settings_delete_user_btn_${user.userId}")
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.DeleteOutline,
                                                            contentDescription = "حذف الحساب",
                                                            tint = colors.errorRed,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "حذف الحساب",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = colors.onErrorContainer
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // In-App Remote Update Management Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.goldAccent.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                        .testTag("app_updates_settings_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.secondarySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsSectionBadge(Icons.Default.SystemUpdate, colors.goldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("إدارة تحديثات التطبيق", fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                                Text(
                                    text = "الإصدار المثبت حالياً: ${BuildConfig.VERSION_NAME} (كود ${BuildConfig.VERSION_CODE})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = versionCodeInput,
                                onValueChange = { versionCodeInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("كود الإصدار") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_update_version_code"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.cardSurface,
                                    unfocusedContainerColor = colors.cardSurface,
                                    focusedBorderColor = colors.goldAccent,
                                    unfocusedBorderColor = colors.cardBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )
                            OutlinedTextField(
                                value = versionNameInput,
                                onValueChange = { versionNameInput = it },
                                label = { Text("رقم الإصدار (مثال: 1.0.1)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("settings_update_version_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.cardSurface,
                                    unfocusedContainerColor = colors.cardSurface,
                                    focusedBorderColor = colors.goldAccent,
                                    unfocusedBorderColor = colors.cardBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )
                        }

                        OutlinedTextField(
                            value = downloadUrlInput,
                            onValueChange = { downloadUrlInput = it },
                            label = { Text("رابط تحميل التحديث المباشر (APK URL)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_update_download_url"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardSurface,
                                unfocusedContainerColor = colors.cardSurface,
                                focusedBorderColor = colors.goldAccent,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        OutlinedTextField(
                            value = releaseNotesInput,
                            onValueChange = { releaseNotesInput = it },
                            label = { Text("ملاحظات الإصدار (ما الجديد؟)") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_update_release_notes"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardSurface,
                                unfocusedContainerColor = colors.cardSurface,
                                focusedBorderColor = colors.goldAccent,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تحديث إجباري للعملاء", color = colors.textPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Switch(checked = isMandatoryUpdate, onCheckedChange = { isMandatoryUpdate = it })
                        }

                        Button(
                            onClick = {
                                val parsedCode = versionCodeInput.toIntOrNull() ?: 0
                                onPublishAppVersion(
                                    parsedCode,
                                    versionNameInput,
                                    downloadUrlInput,
                                    releaseNotesInput,
                                    isMandatoryUpdate
                                )
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .bounceClick()
                                .testTag("publish_app_update_btn")
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = colors.onAccentText, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نشر التحديث للعملاء", color = colors.onAccentText, fontWeight = FontWeight.ExtraBold)
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

    userPendingDeletion?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { userPendingDeletion = null },
            containerColor = colors.cardSurface,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = colors.errorRed
                )
            },
            title = {
                Text(
                    text = "تأكيد حذف الحساب",
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "هل أنتِ متأكدة من حذف حساب (${targetUser.username}) نهائياً؟ سيتم حذفه من السحابة ومن القائمة فوراً.",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAppUser(targetUser)
                        userPendingDeletion = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.errorRed),
                    modifier = Modifier.testTag("settings_confirm_delete_user_btn")
                ) {
                    Text("حذف نهائياً", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userPendingDeletion = null }) {
                    Text("إلغاء", color = colors.textSecondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
