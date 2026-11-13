package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import org.json.JSONArray

/**
 * Compatibility facade preserving Base64ImageUtils JSON helpers while delegating
 * EXIF-aware Base64 Data URI compression & cached ImageBitmap decoding to [ImageUtils].
 */
object Base64ImageUtils {

    fun getCachedBitmap(base64OrPath: String): ImageBitmap? {
        return ImageUtils.getCachedImageBitmap(base64OrPath)
    }

    suspend fun decodeToImageBitmap(base64OrPath: String): ImageBitmap? {
        return ImageUtils.decodeToImageBitmap(base64OrPath)
    }

    suspend fun compressUriToBase64(context: Context, uri: Uri): String? {
        return ImageUtils.compressUriToBase64DataUri(context, uri)
    }

    fun scaleBitmapDown(source: Bitmap, maxDim: Int = ImageUtils.MAX_DIMENSION_PX): Bitmap {
        return ImageUtils.scaleBitmapPreservingAspectRatio(source, maxDim)
    }

    fun encodePhotosJson(photos: List<String>): String {
        val arr = JSONArray()
        photos
            .filter { it.isNotBlank() && !it.startsWith("content://", ignoreCase = true) }
            .take(3)
            .forEach { arr.put(it) }
        return arr.toString()
    }

    fun parsePhotosJson(json: String?): List<String> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i, "")
                    if (s.isNotBlank() && !s.startsWith("content://", ignoreCase = true)) {
                        add(s)
                    }
                }
            }.take(3)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
