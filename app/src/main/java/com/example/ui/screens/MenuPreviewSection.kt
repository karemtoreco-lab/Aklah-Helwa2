package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeneratedMenuEntity
import com.example.model.MenuCategoryEntity
import com.example.ui.components.CulinaryPillChip
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import com.example.util.ImageStorageUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MenuPreviewSection(
    menuCategories: List<MenuCategoryEntity>,
    previewBitmaps: List<Bitmap>,
    selectedPreviewCategoryId: Long?,
    isGenerating: Boolean,
    generatedMenus: List<GeneratedMenuEntity>,
    onSelectCategoryFilter: (Long?) -> Unit,
    onRefreshPreview: () -> Unit,
    onExportAllPages: ((List<String>) -> Unit) -> Unit,
    onExportSinglePage: (Int, (List<String>) -> Unit) -> Unit,
    onDeleteGeneratedMenu: (GeneratedMenuEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    var currentPageIndex by remember(previewBitmaps.size) { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (previewBitmaps.isEmpty()) {
            onRefreshPreview()
        }
    }

    val safePageIndex = currentPageIndex.coerceIn(0, (previewBitmaps.size - 1).coerceAtLeast(0))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 124.dp)
            .testTag("menu_preview_section")
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CulinaryPillChip(
                label = "كل المنيو",
                selected = selectedPreviewCategoryId == null,
                onClick = { onSelectCategoryFilter(null) }
            )
            menuCategories.filter { it.isVisible }.forEach { cat ->
                CulinaryPillChip(
                    label = cat.name,
                    selected = selectedPreviewCategoryId == cat.id,
                    onClick = { onSelectCategoryFilter(cat.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRefreshPreview,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .bounceClick()
                    .testTag("refresh_preview_btn"),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تحديث", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onExportAllPages {} },
                modifier = Modifier
                    .weight(1.3f)
                    .defaultMinSize(minHeight = 48.dp)
                    .shadow(6.dp, RoundedCornerShape(50), spotColor = colors.primaryBrand.copy(alpha = 0.35f))
                    .bounceClick()
                    .testTag("export_menu_png_btn"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = colors.onAccentText, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تصدير PNG", color = colors.onAccentText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    onExportAllPages { paths ->
                        if (paths.isNotEmpty()) {
                            ImageStorageUtils.shareMultiplePngFiles(context, paths)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1.2f)
                    .defaultMinSize(minHeight = 48.dp)
                    .shadow(6.dp, RoundedCornerShape(50), spotColor = colors.profitGreen.copy(alpha = 0.35f))
                    .bounceClick()
                    .testTag("share_menu_whatsapp_btn"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = colors.profitGreen)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("واتساب", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (previewBitmaps.size > 1) {
            Surface(
                color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder),
                shadowElevation = if (colors.isDark) 0.dp else 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (safePageIndex > 0) currentPageIndex = safePageIndex - 1 },
                        enabled = safePageIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "السابق", tint = colors.textPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "صفحة ${safePageIndex + 1} من ${previewBitmaps.size}",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تم تقسيم المنيو تلقائياً للحفاظ على وضوح التصميم",
                            color = colors.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                    IconButton(
                        onClick = { if (safePageIndex < previewBitmaps.lastIndex) currentPageIndex = safePageIndex + 1 },
                        enabled = safePageIndex < previewBitmaps.lastIndex
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "التالي", tint = colors.textPrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = colors.primaryBrand.copy(alpha = 0.25f))
                .border(1.5.dp, colors.primaryBrand.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("menu_poster_preview_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (colors.isDark) colors.glassSurface else colors.cardSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1440f / 2040f),
                contentAlignment = Alignment.Center
            ) {
                val bmp = previewBitmaps.getOrNull(safePageIndex)
                if (isGenerating) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = colors.primaryBrand)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("جاري تجهيز بوستر المنيو عالي الجودة...", color = colors.textPrimary, fontSize = 13.sp)
                    }
                } else if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "معاينة منيو أكلة حلوة",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    )
                }
            }
        }

        if (previewBitmaps.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onExportSinglePage(safePageIndex) {} },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصدير هذه الصفحة فقط (صفحة ${safePageIndex + 1})", color = colors.textPrimary, fontSize = 12.sp)
            }
        }

        if (generatedMenus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "سجل المنيوهات المصدرة (${generatedMenus.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                generatedMenus.take(6).forEach { export ->
                    Surface(
                        color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(export.title, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(DateUtils.formatDate(export.createdAt), color = colors.textSecondary, fontSize = 11.sp)
                            }
                            IconButton(
                                onClick = { ImageStorageUtils.sharePngFile(context, export.filePath) }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = colors.profitGreen)
                            }
                            IconButton(
                                onClick = { onDeleteGeneratedMenu(export) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = colors.primaryBrand)
                            }
                        }
                    }
                }
            }
        }
    }
}
