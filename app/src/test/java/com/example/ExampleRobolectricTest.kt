package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("أكلة حلوة", appName)
  }

  @Test
  fun `order profit calculation is correct`() {
    val order = Order(
      customerName = "أم محمد",
      itemsDescription = "طاجن مكرونة بشاميل",
      salePrice = 450.0,
      costPrice = 250.0,
      netProfit = 450.0 - 250.0,
      yearMonth = "2026-11",
      status = OrderStatus.DELIVERED.name,
      paymentStatus = PaymentStatus.PAID.name
    )

    assertEquals(200.0, order.netProfit, 0.001)
    assertEquals(450.0, order.salePrice, 0.001)
  }

  @Test
  fun `month formatting in arabic`() {
    val formatted = DateUtils.formatYearMonthArabic("2026-11")
    assertEquals("نوفمبر 2026", formatted)
  }
}
