package com.example.data

import com.example.model.Order
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {

    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    val distinctMonths: Flow<List<String>> = orderDao.getDistinctMonths()

    fun getOrdersByMonth(yearMonth: String): Flow<List<Order>> {
        return orderDao.getOrdersByMonth(yearMonth)
    }

    suspend fun insertOrder(order: Order): Long {
        return orderDao.insertOrder(order)
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

    suspend fun ensureDataExists() {
        if (orderDao.getCount() == 0) {
            AppDatabase.populateInitialOrders(orderDao)
        }
    }
}
