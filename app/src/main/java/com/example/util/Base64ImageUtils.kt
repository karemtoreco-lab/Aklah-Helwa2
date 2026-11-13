package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * High-performance Base64 JPEG compression (max 400x400px, 60% quality)
 * paired with an in-memory LruCache<String, ImageBitmap> decoded on Dispatchers.Default
 * to guarantee smooth 60fps LazyColumn/HorizontalPager scrolling.
 */
object Base64ImageUtils {

    private const val MAX_DIMENSION = 400
    private const val JPEG_QUALITY = 60

    private val bitmapCache = object : LruCache<String, ImageBitmap>(48) {}

    fun getCachedBitmap(base64OrPath: String): ImageBitmap? {
        if (base64OrPath.isBlank()) return null
        return bitmapCache.get(cacheKey(base64OrPath))
    }

    suspend fun decodeToImageBitmap(base64OrPath: String): ImageBitmap? {
        if (base64OrPath.isBlank()) return null
        val key = cacheKey(base64OrPath)
        bitmapCache.get(key)?.let { return it }

        return withContext(Dispatchers.Default) {
            bitmapCache.get(key)?.let { return@withContext it }
            try {
                val bmp = if (base64OrPath.startsWith("/")) {
                    BitmapFactory.decodeFile(base64OrPath)
                } else {
                    val clean = base64OrPath.substringAfter("base64,", base64OrPath).trim()
                    val bytes = Base64.decode(clean, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                bmp?.asImageBitmap()?.also { img ->
                    bitmapCache.put(key, img)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun compressUriToBase64(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }
                val rawW = boundsOpts.outWidth
                val rawH = boundsOpts.outHeight
                if (rawW <= 0 || rawH <= 0) return@withContext null

                var sampleSize = 1
                while ((rawW / sampleSize) > MAX_DIMENSION * 2 || (rawH / sampleSize) > MAX_DIMENSION * 2) {
                    sampleSize *= 2
                }

                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                } ?: return@withContext null

                val scaled = scaleBitmapDown(decoded, MAX_DIMENSION)
                if (scaled !== decoded) decoded.recycle()

                val baos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
                scaled.recycle()

                Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun scaleBitmapDown(source: Bitmap, maxDim: Int = MAX_DIMENSION): Bitmap {
        val maxSide = max(source.width, source.height)
        if (maxSide <= maxDim) return source
        val ratio = maxDim.toFloat() / maxSide.toFloat()
        val targetW = (source.width * ratio).roundToInt().coerceAtLeast(1)
        val targetH = (source.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    fun encodePhotosJson(photos: List<String>): String {
        val arr = JSONArray()
        photos.take(3).forEach { p ->
            if (p.isNotBlank()) arr.put(p)
        }
        return arr.toString()
    }

    fun parsePhotosJson(json: String?): List<String> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i, "")
                    if (s.isNotBlank()) add(s)
                }
            }.take(3)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun cacheKey(raw: String): String {
        return "${raw.hashCode()}_${raw.length}"
    }
}
