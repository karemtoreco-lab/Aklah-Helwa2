package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val ARABIC_MONTHS = arrayOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    fun getCurrentYearMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date())
    }

    fun formatYearMonthArabic(yearMonth: String): String {
        return try {
            val parts = yearMonth.split("-")
            if (parts.size == 2) {
                val year = parts[0]
                val monthIndex = parts[1].toInt() - 1
                if (monthIndex in 0..11) {
                    "${ARABIC_MONTHS[monthIndex]} $year"
                } else yearMonth
            } else yearMonth
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        return sdf.format(Date(timestamp))
    }

    fun formatDayMonth(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMMM", Locale("ar"))
        return sdf.format(Date(timestamp))
    }

    fun getYearMonthFromTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun getPreviousMonth(yearMonth: String): String {
        return try {
            val parts = yearMonth.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            cal.add(Calendar.MONTH, -1)
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            sdf.format(cal.time)
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun getNextMonth(yearMonth: String): String {
        return try {
            val parts = yearMonth.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            cal.add(Calendar.MONTH, 1)
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            sdf.format(cal.time)
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun formatCurrency(amount: Double): String {
        return String.format(Locale.US, "%,.0f ج.م", amount)
    }

    fun formatCurrencyWithDecimal(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%,.0f ج.م", amount)
        } else {
            String.format(Locale.US, "%,.2f ج.م", amount)
        }
    }
}
