package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.model.Order
import com.example.model.OrderLineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE yearMonth = :yearMonth ORDER BY orderDate DESC")
    fun getOrdersByMonth(yearMonth: String): Flow<List<Order>>

    @Query("SELECT DISTINCT yearMonth FROM orders ORDER BY yearMonth DESC")
    fun getDistinctMonths(): Flow<List<String>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): Order?

    @Query("SELECT * FROM orders WHERE cloudId = :cloudId LIMIT 1")
    suspend fun getOrderByCloudId(cloudId: String): Order?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id ASC")
    suspend fun getLineItemsForOrder(orderId: Long): List<OrderLineItemEntity>

    @Query("SELECT * FROM order_items")
    fun getAllLineItems(): Flow<List<OrderLineItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId IN (SELECT id FROM orders WHERE yearMonth = :yearMonth AND status != 'CANCELLED')")
    fun getLineItemsForMonth(yearMonth: String): Flow<List<OrderLineItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<Order>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineItems(items: List<OrderLineItemEntity>)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteLineItemsForOrder(orderId: Long)

    @Transaction
    suspend fun saveOrderWithItems(order: Order, items: List<OrderLineItemEntity>): Long {
        val orderId = if (order.id == 0L) {
            insertOrder(order)
        } else {
            updateOrder(order)
            deleteLineItemsForOrder(order.id)
            order.id
        }
        if (items.isNotEmpty()) {
            insertLineItems(items.map { it.copy(orderId = orderId) })
        }
        return orderId
    }

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getCount(): Int
}
