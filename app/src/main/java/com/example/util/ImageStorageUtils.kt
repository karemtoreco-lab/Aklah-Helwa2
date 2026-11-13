package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageUtils {

    private const val PRODUCT_IMAGES_DIR = "product_images"
    private const val EXPORTED_MENUS_DIR = "exported_menus"

    /**
     * Copies a picked gallery image URI into persistent app-internal storage,
     * scaling down oversized images to max 1200px to ensure fast rendering and low memory usage.
     */
    fun copyProductImageToInternalStorage(context: Context, sourceUri: Uri, productId: Long): String? {
        return try {
            val dir = File(context.filesDir, PRODUCT_IMAGES_DIR).apply { mkdirs() }
            val destFile = File(dir, "prod_${productId}_${UUID.randomUUID()}.jpg")

            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val originalBitmap = inputStream.use { BitmapFactory.decodeStream(it) } ?: return null

            val maxDim = 1200
            val scaledBitmap = if (originalBitmap.width > maxDim || originalBitmap.height > maxDim) {
                val ratio = minOf(maxDim.toFloat() / originalBitmap.width, maxDim.toFloat() / originalBitmap.height)
                val targetW = (originalBitmap.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (originalBitmap.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true).also {
                    if (it != originalBitmap) originalBitmap.recycle()
                }
            } else {
                originalBitmap
            }

            FileOutputStream(destFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            if (!scaledBitmap.isRecycled) {
                scaledBitmap.recycle()
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteStoredFile(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Saves a generated high-res menu poster bitmap as PNG to internal app storage (for FileProvider sharing & history)
     * and exports a public copy to device Pictures/AklehHelwa via MediaStore on API 29+.
     */
    fun saveExportedMenuPng(context: Context, bitmap: Bitmap, fileBaseName: String): String? {
        return try {
            val dir = File(context.filesDir, EXPORTED_MENUS_DIR).apply { mkdirs() }
            val safeName = fileBaseName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            val internalFile = File(dir, "${safeName}_${System.currentTimeMillis()}.png")

            FileOutputStream(internalFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Also save to device Pictures gallery on Android Q+ (zero permissions required)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "${internalFile.name}")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AklehHelwa")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { mediaOut ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, mediaOut)
                        }
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                    }
                } catch (_: Exception) {
                    // Internal file copy still succeeded cleanly
                }
            }

            internalFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sharePngFile(context: Context, filePath: String, chooserTitle: String = "مشاركة منيو أكلة حلوة") {
        val file = File(filePath)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "منيو أكلات مطبخ أكلة حلوة 🍲❤️")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }

    fun shareMultiplePngFiles(context: Context, filePaths: List<String>, chooserTitle: String = "مشاركة صفحات منيو أكلة حلوة") {
        val uris = ArrayList<Uri>()
        filePaths.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                uris.add(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
            }
        }
        if (uris.isEmpty()) return
        if (uris.size == 1) {
            sharePngFile(context, filePaths.first(), chooserTitle)
            return
        }
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/png"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            putExtra(Intent.EXTRA_TEXT, "منيو أكلات مطبخ أكلة حلوة 🍲❤️")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }
}
