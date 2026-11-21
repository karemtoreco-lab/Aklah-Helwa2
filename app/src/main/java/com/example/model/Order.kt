package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OrderStatus(val titleAr: String) {
    NEW_CUSTOMER("جديد (طلب عميل)"),
    PENDING_REVIEW("قيد المراجعة"),
    PENDING("قيد التجهيز"),
    PREPARING("جاري التجهيز"),
    OUT_FOR_DELIVERY("خرج للتوصيل"),
    DELIVERED("تم التسليم"),
    CANCELLED("ملغي");

    companion object {
        fun trackingTitle(rawStatus: String): String {
            return when (rawStatus) {
                NEW_CUSTOMER.name, PENDING_REVIEW.name -> "قيد المراجعة"
                PENDING.name, PREPARING.name -> "جاري التجهيز"
                OUT_FOR_DELIVERY.name -> "خرج للتوصيل"
                DELIVERED.name -> "تم التسليم"
                CANCELLED.name -> "ملغي"
                else -> "قيد المراجعة"
            }
        }

        fun trackingStepIndex(rawStatus: String): Int {
            return when (rawStatus) {
                NEW_CUSTOMER.name, PENDING_REVIEW.name -> 0
                PENDING.name, PREPARING.name -> 1
                OUT_FOR_DELIVERY.name -> 2
                DELIVERED.name -> 3
                else -> -1
            }
        }

        fun nextAdminStatus(current: String): OrderStatus {
            return when (current) {
                NEW_CUSTOMER.name, PENDING_REVIEW.name -> PREPARING
                PENDING.name, PREPARING.name -> OUT_FOR_DELIVERY
                OUT_FOR_DELIVERY.name -> DELIVERED
                DELIVERED.name -> PENDING_REVIEW
                else -> PENDING_REVIEW
            }
        }
    }
}

enum class PaymentStatus(val titleAr: String) {
    PAID("مدفوع بالكامل"),
    PARTIAL("عربون / مدفوع جزئياً"),
    UNPAID("آجل / غير مدفوع")
}

enum class OrderSource(val titleAr: String) {
    ADMIN("إدارة"),
    CUSTOMER("عميل")
}

@Entity(
    tableName = "orders",
    indices = [Index(value = ["yearMonth"]), Index(value = ["orderDate"])]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val customerPhone: String = "",
    val itemsDescription: String,
    val salePrice: Double,
    val costPrice: Double,
    val netProfit: Double = salePrice - costPrice,
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryDate: Long = 0L,
    val yearMonth: String,
    val status: String = OrderStatus.DELIVERED.name,
    val paymentStatus: String = PaymentStatus.PAID.name,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val deliveryAddress: String = "",
    val orderSource: String = OrderSource.ADMIN.name,
    val cloudId: String = "",
    val userId: String = "",
    val deliveryZone: String = "",
    val shippingFee: Double = 0.0,
    val isRated: Boolean = false
) {
    val isFromCustomer: Boolean get() = orderSource == OrderSource.CUSTOMER.name
    val totalWithShipping: Double get() = salePrice + shippingFee
    val effectivePaidAmount: Double
        get() = when (paymentStatus) {
            PaymentStatus.PAID.name -> totalWithShipping
            PaymentStatus.PARTIAL.name -> paidAmount.coerceIn(0.0, totalWithShipping)
            else -> 0.0
        }
    val remainingAmount: Double
        get() = (totalWithShipping - effectivePaidAmount).coerceAtLeast(0.0)
}
