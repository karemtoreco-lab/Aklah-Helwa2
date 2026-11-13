package com.example.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.util.Base64ImageUtils

@Entity(
    tableName = "categories",
    indices = [Index(value = ["sortOrder"])]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"]), Index(value = ["sortOrder"])]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val unit: String = "كيلو",
    val description: String = "",
    val purchaseCost: Double = 0.0,
    val defaultSellingPrice: Double = 0.0,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "product_images",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class ProductImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val imagePath: String,
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "menu_categories",
    indices = [Index(value = ["sortOrder"])]
)
data class MenuCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val isVisible: Boolean = true
)

@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = MenuCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["menuCategoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["menuCategoryId"]), Index(value = ["productId"]), Index(value = ["sortOrder"])]
)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val menuCategoryId: Long,
    val productId: Long? = null,
    val displayName: String,
    val unitLabel: String = "كيلو",
    val description: String = "",
    val publishedPrice: Double = 0.0,
    val isVariablePrice: Boolean = false,
    val sortOrder: Int = 0,
    val isVisible: Boolean = true,
    val photosJson: String = "[]",
    val isPopular: Boolean = false,
    val isPreOrder24h: Boolean = false,
    val ratingAvg: Double = 4.9,
    val ratingCount: Int = 0
) {
    fun photoList(): List<String> = Base64ImageUtils.parsePhotosJson(photosJson)
}

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"]), Index(value = ["productId"])]
)
data class OrderLineItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long? = null,
    val productNameSnapshot: String,
    val productImagePathSnapshot: String? = null,
    val quantity: Double = 1.0,
    val unitPurchaseCost: Double = 0.0,
    val unitSellingPrice: Double = 0.0,
    val totalPurchaseCost: Double = 0.0,
    val totalSellingPrice: Double = 0.0,
    val itemGrossProfit: Double = 0.0
)

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["yearMonth"]), Index(value = ["expenseDate"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "تشغيل ومواصلات",
    val amount: Double,
    val expenseDate: Long = System.currentTimeMillis(),
    val yearMonth: String,
    val notes: String = ""
)

@Entity(
    tableName = "generated_menus",
    indices = [Index(value = ["createdAt"])]
)
data class GeneratedMenuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val pageIndex: Int = 1,
    val totalPages: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
