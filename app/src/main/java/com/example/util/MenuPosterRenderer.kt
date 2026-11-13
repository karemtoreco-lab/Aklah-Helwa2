package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

data class MenuPosterSection(
    val category: MenuCategoryEntity,
    val items: List<MenuItemEntity>
)

/**
 * High-resolution programmatic PNG poster generator for "أكلة حلوة".
 * Renders directly onto Android Canvas using Cairo Arabic font and RTL StaticLayout
 * to guarantee crisp text, zero overlapping, dynamic pricing, and multi-page pagination.
 */
object MenuPosterRenderer {

    private const val PAGE_WIDTH = 1440
    private const val PAGE_HEIGHT = 2040
    private const val SIDE_MARGIN = 64f
    private const val COLUMN_GAP = 44f
    private const val HEADER_HEIGHT = 350f
    private const val FOOTER_HEIGHT = 130f
    private const val CONTENT_TOP = HEADER_HEIGHT + 24f
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - FOOTER_HEIGHT - 20f

    private data class MeasuredItem(
        val item: MenuItemEntity,
        val nameLayout: StaticLayout,
        val rowHeight: Float
    )

    private data class MeasuredSection(
        val category: MenuCategoryEntity,
        val items: List<MeasuredItem>,
        val totalHeight: Float
    )

    private data class PlacedSection(
        val section: MeasuredSection,
        val colIndex: Int, // 0 = right column (RTL first), 1 = left column
        val topY: Float
    )

    fun renderMenuPages(
        context: Context,
        sections: List<MenuPosterSection>,
        singleCategoryId: Long? = null
    ): List<Bitmap> {
        val visibleSections = sections
            .filter { it.category.isVisible && (singleCategoryId == null || it.category.id == singleCategoryId) }
            .mapNotNull { sec ->
                val visibleItems = sec.items.filter { it.isVisible }
                if (visibleItems.isEmpty()) null else sec.copy(items = visibleItems)
            }

        val cairoTypeface = loadCairoTypeface(context)
        val boldTypeface = Typeface.create(cairoTypeface, Typeface.BOLD)

        if (visibleSections.isEmpty()) {
            return listOf(renderEmptyMenuPage(context, boldTypeface, cairoTypeface))
        }

        val colWidth = (PAGE_WIDTH - (SIDE_MARGIN * 2) - COLUMN_GAP) / 2f
        val nameMaxWidth = (colWidth - 230f).toInt().coerceAtLeast(240)

        val itemPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 31f
            typeface = boldTypeface
        }

        // Measure all sections cleanly
        val measuredSections = visibleSections.map { sec ->
            val measuredItems = sec.items.map { item ->
                val displayText = if (item.description.isNotBlank()) {
                    "${item.displayName}\n${item.description}"
                } else {
                    item.displayName
                }
                val layout = buildRtlStaticLayout(displayText, itemPaint, nameMaxWidth)
                val rowH = max(layout.height.toFloat() + 26f, 68f)
                MeasuredItem(item, layout, rowH)
            }
            val bodyHeight = measuredItems.sumOf { it.rowHeight.toDouble() }.toFloat()
            val cardHeaderSpace = 82f
            val bottomPadding = 24f
            MeasuredSection(
                category = sec.category,
                items = measuredItems,
                totalHeight = cardHeaderSpace + bodyHeight + bottomPadding
            )
        }

        // Split oversized sections if single category exceeds one column height
        val maxColHeight = CONTENT_BOTTOM - CONTENT_TOP
        val normalizedSections = mutableListOf<MeasuredSection>()
        for (ms in measuredSections) {
            if (ms.totalHeight <= maxColHeight) {
                normalizedSections.add(ms)
            } else {
                var remaining = ms.items
                var part = 1
                while (remaining.isNotEmpty()) {
                    var usedH = 82f + 24f
                    val chunk = mutableListOf<MeasuredItem>()
                    for (it in remaining) {
                        if (usedH + it.rowHeight <= maxColHeight || chunk.isEmpty()) {
                            chunk.add(it)
                            usedH += it.rowHeight
                        } else {
                            break
                        }
                    }
                    val cat = if (part == 1) ms.category else ms.category.copy(name = "${ms.category.name} (تابع)")
                    normalizedSections.add(MeasuredSection(cat, chunk, usedH))
                    remaining = remaining.drop(chunk.size)
                    part++
                }
            }
        }

        // Paginate into balanced 2-column pages
        val pages = mutableListOf<List<PlacedSection>>()
        var currentPlacements = mutableListOf<PlacedSection>()
        var rightY = CONTENT_TOP
        var leftY = CONTENT_TOP
        val sectionGap = 32f

        for (sec in normalizedSections) {
            val fitsRight = rightY + sec.totalHeight <= CONTENT_BOTTOM
            val fitsLeft = leftY + sec.totalHeight <= CONTENT_BOTTOM

            if (!fitsRight && !fitsLeft) {
                pages.add(currentPlacements)
                currentPlacements = mutableListOf()
                rightY = CONTENT_TOP
                leftY = CONTENT_TOP
            }

            // Pick shorter column that fits (preferring right column first in RTL)
            val useRight = when {
                rightY <= leftY && rightY + sec.totalHeight <= CONTENT_BOTTOM -> true
                leftY + sec.totalHeight <= CONTENT_BOTTOM -> false
                rightY + sec.totalHeight <= CONTENT_BOTTOM -> true
                else -> true
            }

            if (useRight) {
                currentPlacements.add(PlacedSection(sec, colIndex = 0, topY = rightY))
                rightY += sec.totalHeight + sectionGap
            } else {
                currentPlacements.add(PlacedSection(sec, colIndex = 1, topY = leftY))
                leftY += sec.totalHeight + sectionGap
            }
        }
        if (currentPlacements.isNotEmpty()) {
            pages.add(currentPlacements)
        }

        // Load reusable photography & logo assets once
        val logoBitmap = decodeSampledDrawable(context, R.drawable.app_logo, 260, 260)
        val tajineBitmap = decodeSampledDrawable(context, R.drawable.img_dish_tajine, 240, 240)
        val mahshiBitmap = decodeSampledDrawable(context, R.drawable.img_dish_mahshi, 240, 240)
        val bannerBitmap = decodeSampledDrawable(context, R.drawable.img_food_banner, 240, 240)

        val totalPages = pages.size
        val bitmaps = pages.mapIndexed { pageIdx, placements ->
            drawPosterPage(
                placements = placements,
                pageIndex = pageIdx + 1,
                totalPages = totalPages,
                colWidth = colWidth,
                cairoRegular = cairoTypeface,
                cairoBold = boldTypeface,
                logoBitmap = logoBitmap,
                foodPhotos = listOfNotNull(tajineBitmap, mahshiBitmap, bannerBitmap)
            )
        }

        logoBitmap?.recycle()
        tajineBitmap?.recycle()
        mahshiBitmap?.recycle()
        bannerBitmap?.recycle()

        return bitmaps
    }

    private fun drawPosterPage(
        placements: List<PlacedSection>,
        pageIndex: Int,
        totalPages: Int,
        colWidth: Float,
        cairoRegular: Typeface,
        cairoBold: Typeface,
        logoBitmap: Bitmap?,
        foodPhotos: List<Bitmap>
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Premium Charcoal Textured Background
        drawCharcoalTexturedBackground(canvas)

        // 2. Checkered Red & White Top & Bottom Borders
        drawCheckeredStrip(canvas, 0f, 18f, PAGE_WIDTH.toFloat(), 18f)
        drawCheckeredStrip(canvas, 0f, PAGE_HEIGHT - 18f, PAGE_WIDTH.toFloat(), 18f)

        // 3. Food Photography Accent Vignettes Around Edges
        if (foodPhotos.isNotEmpty()) {
            drawCircularPhotoBadge(canvas, foodPhotos[0], 130f, 175f, 92f)
            val rightPhoto = foodPhotos.getOrElse(1) { foodPhotos[0] }
            drawCircularPhotoBadge(canvas, rightPhoto, PAGE_WIDTH - 130f, 175f, 92f)
        }

        // 4. Header: Logo, Chef Hat Accent, Brand Name "أكلة حلوة" & Red Brush Underline
        drawBrandHeader(canvas, logoBitmap, cairoBold, cairoRegular)

        // 5. Draw Category Sections & Items
        for (placed in placements) {
            // RTL: colIndex 0 is Right Column, colIndex 1 is Left Column
            val colLeft = if (placed.colIndex == 0) {
                PAGE_WIDTH - SIDE_MARGIN - colWidth
            } else {
                SIDE_MARGIN
            }
            drawCategoryCard(
                canvas = canvas,
                section = placed.section,
                left = colLeft,
                top = placed.topY,
                width = colWidth,
                cairoBold = cairoBold,
                cairoRegular = cairoRegular
            )
        }

        // 6. Footer with brand tagline, food accents, and page indicator
        drawPosterFooter(canvas, pageIndex, totalPages, foodPhotos.lastOrNull(), cairoBold, cairoRegular)

        return bitmap
    }

    private fun drawCharcoalTexturedBackground(canvas: Canvas) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, PAGE_HEIGHT.toFloat(),
                intArrayOf(Color.parseColor("#161616"), Color.parseColor("#0F0F0F"), Color.parseColor("#181212")),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Subtle dark slate grain / charcoal texture
        val rng = Random(42)
        val grainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until 1800) {
            val x = rng.nextFloat() * PAGE_WIDTH
            val y = rng.nextFloat() * PAGE_HEIGHT
            val r = rng.nextFloat() * 2.5f + 0.5f
            grainPaint.color = if (i % 3 == 0) Color.argb(14, 255, 255, 255) else Color.argb(24, 0, 0, 0)
            canvas.drawCircle(x, y, r, grainPaint)
        }

        // Subtle outer dark-red vignette border frame
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.argb(90, 211, 47, 47)
        }
        canvas.drawRoundRect(RectF(28f, 34f, PAGE_WIDTH - 28f, PAGE_HEIGHT - 34f), 28f, 28f, framePaint)
    }

    private fun drawCheckeredStrip(canvas: Canvas, startX: Float, startY: Float, totalW: Float, squareSize: Float) {
        val redPaint = Paint().apply { color = Color.parseColor("#D32F2F") }
        val whitePaint = Paint().apply { color = Color.parseColor("#F5F5F5") }
        val cols = (totalW / squareSize).toInt() + 1
        for (c in 0 until cols) {
            val paint = if (c % 2 == 0) redPaint else whitePaint
            canvas.drawRect(
                startX + c * squareSize,
                startY,
                startX + (c + 1) * squareSize,
                startY + squareSize,
                paint
            )
        }
    }

    private fun drawCircularPhotoBadge(canvas: Canvas, photo: Bitmap, cx: Float, cy: Float, radius: Float) {
        // Outer shadow ring
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(170, 0, 0, 0)
        }
        canvas.drawCircle(cx, cy + 6f, radius + 6f, shadowPaint)

        // Cropped circular photo
        val diameter = (radius * 2).toInt()
        val scaled = Bitmap.createScaledBitmap(photo, diameter, diameter, true)
        val output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
        val c = Canvas(output)
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        c.drawCircle(radius, radius, radius, maskPaint)
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        c.drawBitmap(scaled, 0f, 0f, maskPaint)

        canvas.drawBitmap(output, cx - radius, cy - radius, null)
        if (scaled != photo) scaled.recycle()
        output.recycle()

        // Vibrant red & white double frame
        val whiteBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.WHITE
        }
        val redBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.parseColor("#D32F2F")
        }
        canvas.drawCircle(cx, cy, radius, whiteBorder)
        canvas.drawCircle(cx, cy, radius + 5f, redBorder)
    }

    private fun drawBrandHeader(canvas: Canvas, logoBitmap: Bitmap?, cairoBold: Typeface, cairoRegular: Typeface) {
        val centerX = PAGE_WIDTH / 2f

        // Logo circular badge
        if (logoBitmap != null) {
            drawCircularPhotoBadge(canvas, logoBitmap, centerX, 118f, 64f)
        } else {
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D32F2F") }
            canvas.drawCircle(centerX, 118f, 58f, badgePaint)
        }

        // Brand Name "أكلة حلوة"
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 74f
            typeface = cairoBold
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 4f, Color.BLACK)
        }
        canvas.drawText("أكلة حلوة", centerX, 256f, titlePaint)

        // Red brush stroke underline under brand name
        drawBrushStrokeBanner(
            canvas = canvas,
            centerX = centerX,
            centerY = 282f,
            width = 390f,
            height = 14f,
            colorStart = Color.parseColor("#FF3D00"),
            colorEnd = Color.parseColor("#B71C1C")
        )

        // Subtitle
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0")
            textSize = 30f
            typeface = cairoRegular
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("منيو الأكلات البيتي والتجهيزات الفاخرة", centerX, 326f, subtitlePaint)
    }

    private fun drawBrushStrokeBanner(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        colorStart: Int = Color.parseColor("#E53935"),
        colorEnd: Int = Color.parseColor("#B71C1C")
    ) {
        val halfW = width / 2f
        val halfH = height / 2f
        val path = Path().apply {
            moveTo(centerX - halfW + 14f, centerY - halfH + 3f)
            quadTo(centerX, centerY - halfH - 5f, centerX + halfW - 10f, centerY - halfH + 2f)
            lineTo(centerX + halfW + 6f, centerY - 2f)
            lineTo(centerX + halfW - 16f, centerY + halfH - 2f)
            quadTo(centerX, centerY + halfH + 5f, centerX - halfW + 10f, centerY + halfH - 3f)
            lineTo(centerX - halfW - 6f, centerY + 3f)
            close()
        }

        val brushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                centerX - halfW, centerY, centerX + halfW, centerY,
                colorStart, colorEnd, Shader.TileMode.CLAMP
            )
            setShadowLayer(8f, 0f, 4f, Color.argb(150, 0, 0, 0))
        }
        canvas.drawPath(path, brushPaint)
    }

    private fun drawCategoryCard(
        canvas: Canvas,
        section: MeasuredSection,
        left: Float,
        top: Float,
        width: Float,
        cairoBold: Typeface,
        cairoRegular: Typeface
    ) {
        val cardTop = top + 24f
        val cardRect = RectF(left, cardTop, left + width, top + section.totalHeight)

        // Card dark textured fill
        val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1D1D1D")
        }
        canvas.drawRoundRect(cardRect, 22f, 22f, cardFillPaint)

        // Thin clean white outline border
        val cardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.2f
            color = Color.argb(200, 255, 255, 255)
        }
        canvas.drawRoundRect(cardRect, 22f, 22f, cardStrokePaint)

        // Red brush-stroke category header overlapping top edge
        val headerCenterX = left + width / 2f
        val headerCenterY = cardTop + 8f
        drawBrushStrokeBanner(
            canvas = canvas,
            centerX = headerCenterX,
            centerY = headerCenterY,
            width = (width - 70f).coerceAtLeast(300f),
            height = 56f
        )

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 33f
            typeface = cairoBold
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, Color.argb(140, 0, 0, 0))
        }
        val textBounds = Rect()
        headerTextPaint.getTextBounds(section.category.name, 0, section.category.name.length, textBounds)
        canvas.drawText(
            section.category.name,
            headerCenterX,
            headerCenterY - textBounds.exactCenterY(),
            headerTextPaint
        )

        // Draw each product row
        var rowY = cardTop + 58f
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = Color.argb(55, 255, 255, 255)
            pathEffect = DashPathEffect(floatArrayOf(8f, 8f), 0f)
        }

        val priceBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                left + 20f, 0f, left + 185f, 0f,
                Color.parseColor("#E53935"), Color.parseColor("#B71C1C"),
                Shader.TileMode.CLAMP
            )
        }
        val priceBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = Color.argb(180, 255, 255, 255)
        }
        val priceTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 27f
            typeface = cairoBold
            textAlign = Paint.Align.CENTER
        }

        section.items.forEachIndexed { idx, measuredItem ->
            val item = measuredItem.item
            val layout = measuredItem.nameLayout

            // Product name on the RIGHT side (RTL)
            val nameRightX = left + width - 24f
            val nameStartX = nameRightX - layout.width
            val nameY = rowY + (measuredItem.rowHeight - layout.height) / 2f

            canvas.save()
            canvas.translate(nameStartX, nameY)
            layout.draw(canvas)
            canvas.restore()

            // Red Price Badge on the LEFT side
            val badgeW = 166f
            val badgeH = 44f
            val badgeLeft = left + 22f
            val badgeTop = rowY + (measuredItem.rowHeight - badgeH) / 2f
            val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeW, badgeTop + badgeH)

            canvas.drawRoundRect(badgeRect, 22f, 22f, priceBadgePaint)
            canvas.drawRoundRect(badgeRect, 22f, 22f, priceBorderPaint)

            val priceLabel = if (item.isVariablePrice && item.publishedPrice <= 0.0) {
                "حسب السعر"
            } else if (item.publishedPrice % 1.0 == 0.0) {
                String.format(Locale.US, "%.0f ج.م", item.publishedPrice)
            } else {
                String.format(Locale.US, "%.2f ج.م", item.publishedPrice)
            }

            val pBounds = Rect()
            priceTextPaint.getTextBounds(priceLabel, 0, priceLabel.length, pBounds)
            canvas.drawText(
                priceLabel,
                badgeRect.centerX(),
                badgeRect.centerY() - pBounds.exactCenterY(),
                priceTextPaint
            )

            rowY += measuredItem.rowHeight
            if (idx < section.items.lastIndex) {
                canvas.drawLine(left + 26f, rowY, left + width - 26f, rowY, dividerPaint)
            }
        }
    }

    private fun drawPosterFooter(
        canvas: Canvas,
        pageIndex: Int,
        totalPages: Int,
        photo: Bitmap?,
        cairoBold: Typeface,
        cairoRegular: Typeface
    ) {
        val footerTop = PAGE_HEIGHT - FOOTER_HEIGHT
        val centerX = PAGE_WIDTH / 2f

        // Accent line
        val linePaint = Paint().apply {
            color = Color.argb(120, 211, 47, 47)
            strokeWidth = 2.5f
        }
        canvas.drawLine(80f, footerTop + 8f, PAGE_WIDTH - 80f, footerTop + 8f, linePaint)

        // Small decorative food photo on bottom-left
        if (photo != null) {
            drawCircularPhotoBadge(canvas, photo, 112f, footerTop + 62f, 40f)
        }

        val footerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
            typeface = cairoBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("أكلة حلوة • أكل بيتي بنظافة وجودة مضمونة ❤️", centerX, footerTop + 58f, footerTitlePaint)

        val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#BDBDBD")
            textSize = 24f
            typeface = cairoRegular
            textAlign = Paint.Align.CENTER
        }
        val pageLabel = if (totalPages > 1) "صفحة $pageIndex من $totalPages" else "الأسعار بالجنيه المصري"
        canvas.drawText(pageLabel, centerX, footerTop + 96f, pagePaint)
    }

    private fun renderEmptyMenuPage(context: Context, cairoBold: Typeface, cairoRegular: Typeface): Bitmap {
        val bitmap = Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawCharcoalTexturedBackground(canvas)
        drawCheckeredStrip(canvas, 0f, 18f, PAGE_WIDTH.toFloat(), 18f)
        drawCheckeredStrip(canvas, 0f, PAGE_HEIGHT - 18f, PAGE_WIDTH.toFloat(), 18f)
        val logoBitmap = decodeSampledDrawable(context, R.drawable.app_logo, 260, 260)
        drawBrandHeader(canvas, logoBitmap, cairoBold, cairoRegular)
        logoBitmap?.recycle()

        val msgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 40f
            typeface = cairoBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("لا توجد أصناف منشورة في المنيو حالياً", PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f, msgPaint)
        return bitmap
    }

    private fun buildRtlStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setLineSpacing(4f, 1.08f)
            .setIncludePad(false)
            .build()
    }

    private fun loadCairoTypeface(context: Context): Typeface {
        return try {
            ResourcesCompat.getFont(context, R.font.cairo) ?: Typeface.DEFAULT
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
    }

    private fun decodeSampledDrawable(context: Context, resId: Int, reqW: Int, reqH: Int): Bitmap? {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, resId, opts)
            var inSample = 1
            if (opts.outHeight > reqH || opts.outWidth > reqW) {
                val halfH = opts.outHeight / 2
                val halfW = opts.outWidth / 2
                while ((halfH / inSample) >= reqH && (halfW / inSample) >= reqW) {
                    inSample *= 2
                }
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = inSample }
            BitmapFactory.decodeResource(context.resources, resId, decodeOpts)
        } catch (_: Exception) {
            null
        }
    }
}
