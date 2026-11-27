package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.model.CategoryEntity
import com.example.model.ProductEntity
import com.example.model.ProductImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    // Categories
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryByIdOnce(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    // Products
    @Query("SELECT * FROM products ORDER BY categoryId ASC, sortOrder ASC, id ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY categoryId ASC, sortOrder ASC, id ASC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY categoryId ASC, sortOrder ASC, id ASC")
    suspend fun getAllProductsOnce(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdOnce(id: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)

    // Product Images
    @Query("SELECT * FROM product_images ORDER BY isPrimary DESC, createdAt DESC")
    fun getAllProductImages(): Flow<List<ProductImageEntity>>

    @Query("SELECT * FROM product_images WHERE productId = :productId ORDER BY isPrimary DESC, createdAt DESC")
    suspend fun getImagesForProductOnce(productId: Long): List<ProductImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductImage(image: ProductImageEntity): Long

    @Query("UPDATE product_images SET isPrimary = 0 WHERE productId = :productId")
    suspend fun clearPrimaryForProduct(productId: Long)

    @Query("UPDATE product_images SET isPrimary = 1 WHERE id = :imageId")
    suspend fun markImagePrimary(imageId: Long)

    @Transaction
    suspend fun setPrimaryImage(productId: Long, imageId: Long) {
        clearPrimaryForProduct(productId)
        markImagePrimary(imageId)
    }

    @Delete
    suspend fun deleteProductImage(image: ProductImageEntity)
}
