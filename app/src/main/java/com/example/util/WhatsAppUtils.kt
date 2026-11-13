package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.model.CustomerCartItem

enum class WhatsAppLaunchResult {
    LAUNCHED_DIRECT,
    NO_NUMBER_CONFIGURED,
    WHATSAPP_NOT_INSTALLED_FALLBACK_SHARE
}

object WhatsAppUtils {

    /**
     * Normalizes an admin WhatsApp phone number to digits suitable for https://wa.me/<number>.
     */
    fun sanitizeWhatsAppNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isBlank()) return ""
        return when {
            digits.startsWith("00") -> digits.drop(2)
            digits.length == 11 && digits.startsWith("01") -> "2$digits"
            else -> digits
        }
    }

    fun formatQuantity(qty: Double): String {
        return if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
    }

    /**
     * Builds a neatly formatted Arabic WhatsApp order confirmation message including
     * delivery zone & shipping fee breakdown.
     */
    fun buildCustomerOrderWhatsAppMessage(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryZone: String = "",
        shippingFee: Double = 0.0,
        notes: String,
        cartItems: List<CustomerCartItem>,
        totalAmount: Double
    ): String {
        val subtotal = totalAmount
        val grandTotal = subtotal + shippingFee
        return buildString {
            appendLine("🍲 *طلب جديد - أكلة حلوة (مطبخ أ/ نهى ماهر)* 🍲")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("👤 *الاسم:* ${customerName.trim()}")
            appendLine("📱 *رقم الموبايل:* ${customerPhone.trim()}")
            if (deliveryZone.isNotBlank()) {
                appendLine("🗺️ *منطقة التوصيل:* ${deliveryZone.trim()}")
            }
            appendLine("📍 *عنوان التوصيل:* ${deliveryAddress.trim()}")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("🍽️ *الأصناف المطلوبة:*")
            cartItems.forEach { ci ->
                val qty = formatQuantity(ci.quantity)
                val lineSubtotal = if (ci.menuItem.isVariablePrice && ci.menuItem.publishedPrice <= 0.0) {
                    "حسب السعر عند التأكيد"
                } else {
                    DateUtils.formatCurrencyWithDecimal(ci.lineTotal)
                }
                appendLine("• $qty × ${ci.menuItem.displayName} = $lineSubtotal")
            }
            appendLine("━━━━━━━━━━━━━━━━━━")
            if (shippingFee > 0.0) {
                appendLine("🛍️ *مجموع الأصناف:* ${DateUtils.formatCurrencyWithDecimal(subtotal)}")
                appendLine("🛵 *رسوم التوصيل:* ${DateUtils.formatCurrencyWithDecimal(shippingFee)}")
            }
            appendLine("💵 *الإجمالي:* ${DateUtils.formatCurrencyWithDecimal(grandTotal)}")
            if (notes.isNotBlank()) {
                appendLine("📝 *ملاحظات إضافية:* ${notes.trim()}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━")
            append("شكراً لاختياركم مطبخ أكلة حلوة ❤️")
        }
    }

    /**
     * Launches WhatsApp directly to the saved admin phone number via https://wa.me/,
     * with graceful fallback if no number is configured or WhatsApp/browser cannot handle the URI.
     */
    fun launchWhatsAppOrderIntent(
        context: Context,
        rawAdminPhone: String,
        message: String
    ): WhatsAppLaunchResult {
        val sanitized = sanitizeWhatsAppNumber(rawAdminPhone)
        if (sanitized.isBlank()) {
            return WhatsAppLaunchResult.NO_NUMBER_CONFIGURED
        }

        val waUri = Uri.parse("https://wa.me/$sanitized?text=${Uri.encode(message)}")
        val viewIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(viewIntent)
            WhatsAppLaunchResult.LAUNCHED_DIRECT
        } catch (_: ActivityNotFoundException) {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                val chooser = Intent.createChooser(shareIntent, "إرسال تفاصيل الطلب").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (_: Exception) {
                // Keep confirmed state gracefully
            }
            WhatsAppLaunchResult.WHATSAPP_NOT_INSTALLED_FALLBACK_SHARE
        } catch (_: Exception) {
            WhatsAppLaunchResult.WHATSAPP_NOT_INSTALLED_FALLBACK_SHARE
        }
    }
}
