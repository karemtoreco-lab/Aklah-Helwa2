package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.model.AppVersionConfig
import com.example.ui.theme.AppTheme

@Composable
fun AppUpdateDialog(
    versionConfig: AppVersionConfig,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    Dialog(
        onDismissRequest = { if (!versionConfig.isMandatory) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !versionConfig.isMandatory,
            dismissOnClickOutside = !versionConfig.isMandatory
        )
    ) {
        Surface(
            modifier = Modifier
                .padding(20.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .border(
                    width = 1.5.dp,
                    color = if (colors.isDark) colors.glassBorder else colors.goldAccent.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(30.dp)
                )
                .testTag("app_update_dialog"),
            color = colors.cardSurface,
            shadowElevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .background(colors.cardHighlightGradient)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(colors.primaryBrand.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(14.dp, CircleShape, spotColor = colors.primaryBrand.copy(alpha = 0.55f))
                            .clip(CircleShape)
                            .background(colors.warmAccentGradient)
                            .border(2.dp, colors.onAccentText.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = colors.onAccentText,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Text(
                    text = "تحديث جديد متاح ✨",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = colors.profitContainer,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.profitGreen.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "الإصدار الجديد: ${versionConfig.latestVersionName}",
                            color = colors.onProfitContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        color = colors.secondarySurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Text(
                            text = "الحالي: ${BuildConfig.VERSION_NAME}",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Surface(
                    color = colors.secondarySurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ما الجديد في أكلة حلوة:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.goldAccent
                        )
                        Text(
                            text = versionConfig.releaseNotes.ifBlank {
                                "تحسينات شاملة في السرعة وتجربة الطلب وإضافة مميزات جديدة لمطبخ أ/ نهى ماهر."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    }
                }

                Text(
                    text = "يتم تثبيت التحديث مباشرة فوق التطبيق الحالي مع الحفاظ الكامل على حسابك وطلباتك.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        val rawUrl = versionConfig.downloadUrl.trim()
                        val normalized = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                            rawUrl
                        } else {
                            "https://$rawUrl"
                        }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .shadow(10.dp, RoundedCornerShape(50), spotColor = colors.primaryBrand.copy(alpha = 0.45f))
                        .bounceClick()
                        .testTag("update_now_btn")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = colors.onAccentText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تحديث الآن",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onAccentText
                    )
                }

                if (!versionConfig.isMandatory) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .bounceClick()
                            .testTag("dismiss_update_btn")
                    ) {
                        Text("لاحقاً", color = colors.textSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
