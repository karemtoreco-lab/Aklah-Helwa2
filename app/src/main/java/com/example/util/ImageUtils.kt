package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Cloud-ready image compression & cached Base64 Data URI utility.
 * Reads streams on Dispatchers.IO, normalizes EXIF orientation, scales down to max 640px
 * preserving aspect ratio, compresses JPEG at quality 72 (~60KB per image), and caches
 * decoded ImageBitmaps in memory on Dispatchers.Default for smooth 60fps scrolling.
 */
object ImageUtils {

    const val MAX_DIMENSION_PX = 640
    const val JPEG_QUALITY = 72
    const val DATA_URI_PREFIX = "data:image/jpeg;base64,"

    private val decodedBitmapCache = object : LruCache<String, ImageBitmap>(64) {}

    suspend fun compressUriToBase64DataUri(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val exifOrientation = readExifOrientation(context, uri)

                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }
                val rawW = boundsOpts.outWidth
                val rawH = boundsOpts.outHeight
                if (rawW <= 0 || rawH <= 0) return@withContext null

                var sampleSize = 1
                while ((rawW / sampleSize) > MAX_DIMENSION_PX * 2 || (rawH / sampleSize) > MAX_DIMENSION_PX * 2) {
                    sampleSize *= 2
                }

                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                } ?: return@withContext null

                val oriented = applyExifOrientation(decoded, exifOrientation)
                if (oriented !== decoded && !decoded.isRecycled) {
                    decoded.recycle()
                }

                val scaled = scaleBitmapPreservingAspectRatio(oriented, MAX_DIMENSION_PX)
                if (scaled !== oriented && !oriented.isRecycled) {
                    oriented.recycle()
                }

                val baos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
                if (!scaled.isRecycled) {
                    scaled.recycle()
                }

                val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                DATA_URI_PREFIX + base64
            } catch (_: Exception) {
                null
            }
        }
    }

    fun getCachedImageBitmap(source: String?): ImageBitmap? {
        if (source.isNullOrBlank()) return null
        return decodedBitmapCache.get(cacheKey(source))
    }

    suspend fun decodeToImageBitmap(source: String?): ImageBitmap? {
        if (source.isNullOrBlank()) return null
        val trimmed = source.trim()
        if (trimmed.startsWith("content://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return null
        }

        val key = cacheKey(trimmed)
        decodedBitmapCache.get(key)?.let { return it }

        return withContext(Dispatchers.Default) {
            decodedBitmapCache.get(key)?.let { return@withContext it }
            try {
                val bmp = if (trimmed.startsWith("/")) {
                    BitmapFactory.decodeFile(trimmed)
                } else {
                    val rawBase64 = trimmed.substringAfter("base64,", trimmed).trim()
                    val bytes = Base64.decode(rawBase64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                bmp?.asImageBitmap()?.also { img ->
                    decodedBitmapCache.put(key, img)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    fun calculateTargetDimensions(width: Int, height: Int, maxDim: Int = MAX_DIMENSION_PX): Pair<Int, Int> {
        if (width <= 0 || height <= 0) return 1 to 1
        val maxSide = max(width, height)
        if (maxSide <= maxDim) return width to height
        val ratio = maxDim.toFloat() / maxSide.toFloat()
        val targetW = (width * ratio).roundToInt().coerceAtLeast(1)
        val targetH = (height * ratio).roundToInt().coerceAtLeast(1)
        return targetW to targetH
    }

    fun scaleBitmapPreservingAspectRatio(source: Bitmap, maxDim: Int = MAX_DIMENSION_PX): Bitmap {
        val (targetW, targetH) = calculateTargetDimensions(source.width, source.height, maxDim)
        if (targetW == source.width && targetH == source.height) return source
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    fun isBase64OrDataUri(source: String?): Boolean {
        if (source.isNullOrBlank()) return false
        val trimmed = source.trim()
        return trimmed.startsWith("data:image/", ignoreCase = true) ||
            (!trimmed.startsWith("/") &&
                !trimmed.startsWith("http://", ignoreCase = true) &&
                !trimmed.startsWith("https://", ignoreCase = true) &&
                !trimmed.startsWith("content://", ignoreCase = true))
    }

    fun isRemoteUrlOrFile(source: String?): Boolean {
        if (source.isNullOrBlank()) return false
        val trimmed = source.trim()
        return trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("/")
    }

    private fun readExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }

    private fun cacheKey(raw: String): String = "${raw.hashCode()}_${raw.length}"
}
