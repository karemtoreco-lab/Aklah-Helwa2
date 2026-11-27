package com.example

import com.example.model.DraftOrderLineItem
import com.example.model.Order
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun draftLineItem_defaultsToBatchTotalCost_withoutMultiplyingByQuantity() {
        val draft = DraftOrderLineItem(
            productNameSnapshot = "كيلو محشي كرنب",
            quantity = 3.0,
            unitPurchaseCost = 25.0,
            unitSellingPrice = 120.0
        )

        assertTrue(draft.isTotalCostForWholeQty)
        assertEquals(360.0, draft.totalSellingPrice, 0.001)
        assertEquals(25.0, draft.totalPurchaseCost, 0.001)
        assertEquals(335.0, draft.itemGrossProfit, 0.001)
    }

    @Test
    fun draftLineItem_perUnitCostToggle_multipliesPurchaseCostByQuantity() {
        val draft = DraftOrderLineItem(
            productNameSnapshot = "كيلو بانيه",
            quantity = 3.0,
            unitPurchaseCost = 25.0,
            unitSellingPrice = 120.0,
            isTotalCostForWholeQty = false
        )

        assertEquals(360.0, draft.totalSellingPrice, 0.001)
        assertEquals(75.0, draft.totalPurchaseCost, 0.001)
        assertEquals(285.0, draft.itemGrossProfit, 0.001)
    }

    @Test
    fun order_combinesItemsCostAndExtraPrepCost_intoTotalCostAndNetProfit() {
        val itemsCost = 25.0
        val extraPrepCost = 30.0
        val totalSale = 360.0
        val totalCost = itemsCost + extraPrepCost

        val order = Order(
            customerName = "أم كريم",
            itemsDescription = "3 × كيلو محشي كرنب",
            salePrice = totalSale,
            costPrice = totalCost,
            extraPrepCost = extraPrepCost,
            extraPrepNotes = "خضرة وصلصة وأرز",
            yearMonth = "2026-11"
        )

        assertEquals(55.0, order.costPrice, 0.001)
        assertEquals(25.0, order.itemsCostPrice, 0.001)
        assertEquals(30.0, order.extraPrepCost, 0.001)
        assertEquals(305.0, order.netProfit, 0.001)
    }
}

