package com.example.data

import com.example.model.Order
import com.example.model.OrderLineItemEntity
import com.example.model.TreasuryTransactionEntity
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {

    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    val distinctMonths: Flow<List<String>> = orderDao.getDistinctMonths()

    val allLineItems: Flow<List<OrderLineItemEntity>> = orderDao.getAllLineItems()

    val allTreasuryTransactions: Flow<List<TreasuryTransactionEntity>> = orderDao.getAllTreasuryTransactions()

    fun getOrdersByMonth(yearMonth: String): Flow<List<Order>> {
        return orderDao.getOrdersByMonth(yearMonth)
    }

    fun getLineItemsForMonth(yearMonth: String): Flow<List<OrderLineItemEntity>> {
        return orderDao.getLineItemsForMonth(yearMonth)
    }

    suspend fun getLineItemsForOrder(orderId: Long): List<OrderLineItemEntity> {
        return orderDao.getLineItemsForOrder(orderId)
    }

    suspend fun getOrderByCloudId(cloudId: String): Order? {
        return orderDao.getOrderByCloudId(cloudId)
    }

    suspend fun insertOrder(order: Order): Long {
        return orderDao.insertOrder(order)
    }

    suspend fun saveOrderWithItems(order: Order, items: List<OrderLineItemEntity>): Long {
        return orderDao.saveOrderWithItems(order, items)
    }

    suspend fun updateOrder(order: Order) {
        orderDao.updateOrder(order)
    }

    suspend fun deleteOrder(order: Order) {
        orderDao.deleteOrder(order)
    }

    suspend fun deleteOrderById(id: Long) {
        orderDao.deleteOrderById(id)
    }

    // Treasury & Capital
    suspend fun getTreasuryTxByCloudId(cloudId: String): TreasuryTransactionEntity? {
        return orderDao.getTreasuryTxByCloudId(cloudId)
    }

    suspend fun insertTreasuryTransaction(tx: TreasuryTransactionEntity): Long {
        return orderDao.insertTreasuryTransaction(tx)
    }

    suspend fun deleteTreasuryTransaction(tx: TreasuryTransactionEntity) {
        orderDao.deleteTreasuryTransaction(tx)
    }
}
