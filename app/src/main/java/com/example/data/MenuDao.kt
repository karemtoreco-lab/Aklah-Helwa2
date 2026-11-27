package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.GeneratedMenuEntity
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    // Menu Categories
    @Query("SELECT * FROM menu_categories ORDER BY sortOrder ASC, id ASC")
    fun getAllMenuCategories(): Flow<List<MenuCategoryEntity>>

    @Query("SELECT * FROM menu_categories ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllMenuCategoriesOnce(): List<MenuCategoryEntity>

    @Query("SELECT * FROM menu_categories WHERE id = :id LIMIT 1")
    suspend fun getMenuCategoryByIdOnce(id: Long): MenuCategoryEntity?

    @Query("SELECT COUNT(*) FROM menu_categories")
    suspend fun getMenuCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuCategory(category: MenuCategoryEntity): Long

    @Update
    suspend fun updateMenuCategory(category: MenuCategoryEntity)

    @Update
    suspend fun updateMenuCategories(categories: List<MenuCategoryEntity>)

    @Delete
    suspend fun deleteMenuCategory(category: MenuCategoryEntity)

    @Query("DELETE FROM menu_categories WHERE id = :categoryId")
    suspend fun deleteMenuCategoryById(categoryId: Long)

    // Menu Items
    @Query("SELECT * FROM menu_items ORDER BY menuCategoryId ASC, sortOrder ASC, id ASC")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items ORDER BY menuCategoryId ASC, sortOrder ASC, id ASC")
    suspend fun getAllMenuItemsOnce(): List<MenuItemEntity>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun getMenuItemByIdOnce(id: Long): MenuItemEntity?

    @Query("SELECT * FROM menu_items WHERE productId = :productId LIMIT 1")
    suspend fun getMenuItemByProductIdOnce(productId: Long): MenuItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Update
    suspend fun updateMenuItems(items: List<MenuItemEntity>)

    @Delete
    suspend fun deleteMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItemById(id: Long)

    // Generated Menu Exports
    @Query("SELECT * FROM generated_menus ORDER BY createdAt DESC")
    fun getGeneratedMenus(): Flow<List<GeneratedMenuEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedMenu(export: GeneratedMenuEntity): Long

    @Delete
    suspend fun deleteGeneratedMenu(export: GeneratedMenuEntity)
}
