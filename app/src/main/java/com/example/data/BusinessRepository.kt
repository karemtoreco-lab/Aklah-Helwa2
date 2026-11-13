package com.example.data

import com.example.model.CategoryEntity
import com.example.model.ExpenseEntity
import com.example.model.GeneratedMenuEntity
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import com.example.model.ProductEntity
import com.example.model.ProductImageEntity
import kotlinx.coroutines.flow.Flow

class BusinessRepository(
    private val catalogDao: CatalogDao,
    private val menuDao: MenuDao,
    private val expenseDao: ExpenseDao
) {
    // Catalog Flows
    val allCategories: Flow<List<CategoryEntity>> = catalogDao.getAllCategories()
    val allProducts: Flow<List<ProductEntity>> = catalogDao.getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = catalogDao.getActiveProducts()
    val allProductImages: Flow<List<ProductImageEntity>> = catalogDao.getAllProductImages()

    // Published Menu Flows
    val menuCategories: Flow<List<MenuCategoryEntity>> = menuDao.getAllMenuCategories()
    val menuItems: Flow<List<MenuItemEntity>> = menuDao.getAllMenuItems()
    val generatedMenus: Flow<List<GeneratedMenuEntity>> = menuDao.getGeneratedMenus()

    // Expenses Flows
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesByMonth(yearMonth: String): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByMonth(yearMonth)

    suspend fun ensureInitialCatalog() {
        AppDatabase.seedInitialCatalogAndMenu(catalogDao, menuDao)
    }

    // Categories
    suspend fun insertCategory(category: CategoryEntity): Long = catalogDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = catalogDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = catalogDao.deleteCategory(category)

    // Products
    suspend fun getAllProductsOnce(): List<ProductEntity> = catalogDao.getAllProductsOnce()
    suspend fun insertProduct(product: ProductEntity): Long = catalogDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = catalogDao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = catalogDao.deleteProduct(product)

    // Product Images
    suspend fun insertProductImage(image: ProductImageEntity): Long {
        val existing = catalogDao.getImagesForProductOnce(image.productId)
        val shouldBePrimary = existing.isEmpty() || image.isPrimary
        val newId = catalogDao.insertProductImage(image.copy(isPrimary = shouldBePrimary))
        if (shouldBePrimary) {
            catalogDao.setPrimaryImage(image.productId, newId)
        }
        return newId
    }

    suspend fun setPrimaryImage(productId: Long, imageId: Long) =
        catalogDao.setPrimaryImage(productId, imageId)

    suspend fun deleteProductImage(image: ProductImageEntity) {
        catalogDao.deleteProductImage(image)
        if (image.isPrimary) {
            val remaining = catalogDao.getImagesForProductOnce(image.productId)
            remaining.firstOrNull()?.let { nextPrimary ->
                catalogDao.setPrimaryImage(image.productId, nextPrimary.id)
            }
        }
    }

    // Independent / Cloud-Unified Menu
    suspend fun getAllMenuItemsOnce(): List<MenuItemEntity> = menuDao.getAllMenuItemsOnce()
    suspend fun getMenuItemByIdOnce(id: Long): MenuItemEntity? = menuDao.getMenuItemByIdOnce(id)
    suspend fun getMenuItemByProductIdOnce(productId: Long): MenuItemEntity? = menuDao.getMenuItemByProductIdOnce(productId)

    suspend fun insertMenuCategory(category: MenuCategoryEntity): Long =
        menuDao.insertMenuCategory(category)

    suspend fun updateMenuCategory(category: MenuCategoryEntity) =
        menuDao.updateMenuCategory(category)

    suspend fun updateMenuCategories(categories: List<MenuCategoryEntity>) =
        menuDao.updateMenuCategories(categories)

    suspend fun deleteMenuCategory(category: MenuCategoryEntity) =
        menuDao.deleteMenuCategory(category)

    suspend fun insertMenuItem(item: MenuItemEntity): Long =
        menuDao.insertMenuItem(item)

    suspend fun updateMenuItem(item: MenuItemEntity) =
        menuDao.updateMenuItem(item)

    suspend fun updateMenuItems(items: List<MenuItemEntity>) =
        menuDao.updateMenuItems(items)

    suspend fun deleteMenuItem(item: MenuItemEntity) =
        menuDao.deleteMenuItem(item)

    // Generated Menu Exports
    suspend fun insertGeneratedMenu(export: GeneratedMenuEntity): Long =
        menuDao.insertGeneratedMenu(export)

    suspend fun deleteGeneratedMenu(export: GeneratedMenuEntity) =
        menuDao.deleteGeneratedMenu(export)

    // Expenses
    suspend fun insertExpense(expense: ExpenseEntity): Long =
        expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) =
        expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) =
        expenseDao.deleteExpense(expense)
}
