package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OrderStatus(val titleAr: String) {
    PENDING("قيد التجهيز"),
    DELIVERED("تم التسليم"),
    CANCELLED("ملغي")
}

enum class PaymentStatus(val titleAr: String) {
    PAID("مدفوع"),
    PARTIAL("مدفوع جزئياً"),
    UNPAID("آجل / غير مدفوع")
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
    val yearMonth: String, // Format: YYYY-MM e.g. "2026-11"
    val status: String = OrderStatus.DELIVERED.name,
    val paymentStatus: String = PaymentStatus.PAID.name,
    val notes: String = ""
)
