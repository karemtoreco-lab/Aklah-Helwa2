package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.CategoryEntity
import com.example.model.ExpenseEntity
import com.example.model.GeneratedMenuEntity
import com.example.model.MenuCategoryEntity
import com.example.model.MenuItemEntity
import com.example.model.Order
import com.example.model.OrderLineItemEntity
import com.example.model.ProductEntity
import com.example.model.ProductImageEntity
import com.example.model.TreasuryTransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Order::class,
        CategoryEntity::class,
        ProductEntity::class,
        ProductImageEntity::class,
        MenuCategoryEntity::class,
        MenuItemEntity::class,
        OrderLineItemEntity::class,
        ExpenseEntity::class,
        GeneratedMenuEntity::class,
        TreasuryTransactionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun catalogDao(): CatalogDao
    abstract fun menuDao(): MenuDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_sortOrder` ON `categories` (`sortOrder`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `products` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `unit` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `purchaseCost` REAL NOT NULL,
                        `defaultSellingPrice` REAL NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_categoryId` ON `products` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_sortOrder` ON `products` (`sortOrder`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `product_images` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `productId` INTEGER NOT NULL,
                        `imagePath` TEXT NOT NULL,
                        `isPrimary` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_product_images_productId` ON `product_images` (`productId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `menu_categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `isVisible` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_menu_categories_sortOrder` ON `menu_categories` (`sortOrder`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `menu_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `menuCategoryId` INTEGER NOT NULL,
                        `productId` INTEGER,
                        `displayName` TEXT NOT NULL,
                        `unitLabel` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `publishedPrice` REAL NOT NULL,
                        `isVariablePrice` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `isVisible` INTEGER NOT NULL,
                        FOREIGN KEY(`menuCategoryId`) REFERENCES `menu_categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_menu_items_menuCategoryId` ON `menu_items` (`menuCategoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_menu_items_productId` ON `menu_items` (`productId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_menu_items_sortOrder` ON `menu_items` (`sortOrder`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `order_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `orderId` INTEGER NOT NULL,
                        `productId` INTEGER,
                        `productNameSnapshot` TEXT NOT NULL,
                        `productImagePathSnapshot` TEXT,
                        `quantity` REAL NOT NULL,
                        `unitPurchaseCost` REAL NOT NULL,
                        `unitSellingPrice` REAL NOT NULL,
                        `totalPurchaseCost` REAL NOT NULL,
                        `totalSellingPrice` REAL NOT NULL,
                        `itemGrossProfit` REAL NOT NULL,
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_items_orderId` ON `order_items` (`orderId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_items_productId` ON `order_items` (`productId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `expenses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `expenseDate` INTEGER NOT NULL,
                        `yearMonth` TEXT NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_yearMonth` ON `expenses` (`yearMonth`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_expenseDate` ON `expenses` (`expenseDate`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `generated_menus` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `filePath` TEXT NOT NULL,
                        `pageIndex` INTEGER NOT NULL,
                        `totalPages` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_generated_menus_createdAt` ON `generated_menus` (`createdAt`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `deliveryAddress` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `orderSource` TEXT NOT NULL DEFAULT 'ADMIN'")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `cloudId` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `deliveryZone` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `shippingFee` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `isRated` INTEGER NOT NULL DEFAULT 0")

                db.execSQL("ALTER TABLE `menu_items` ADD COLUMN `photosJson` TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE `menu_items` ADD COLUMN `isPopular` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `menu_items` ADD COLUMN `isPreOrder24h` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `menu_items` ADD COLUMN `ratingAvg` REAL NOT NULL DEFAULT 4.9")
                db.execSQL("ALTER TABLE `menu_items` ADD COLUMN `ratingCount` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `deliveryDate` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `paidAmount` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `order_items` ADD COLUMN `isTotalCostForWholeQty` INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `treasury_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cloudId` TEXT NOT NULL DEFAULT '',
                        `type` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `title` TEXT NOT NULL,
                        `notes` TEXT NOT NULL DEFAULT '',
                        `createdAt` INTEGER NOT NULL,
                        `yearMonth` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_treasury_transactions_yearMonth` ON `treasury_transactions` (`yearMonth`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_treasury_transactions_createdAt` ON `treasury_transactions` (`createdAt`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `extraPrepCost` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `extraPrepNotes` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "akleh_helwa_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialCatalogAndMenu(database.catalogDao(), database.menuDao())
                    }
                }
            }
        }

        private data class InitialProductSeed(
            val name: String,
            val price: Double,
            val unit: String = "كيلو",
            val isVariable: Boolean = false
        )

        private data class InitialCategorySeed(
            val name: String,
            val items: List<InitialProductSeed>
        )

        suspend fun seedInitialCatalogAndMenu(catalogDao: CatalogDao, menuDao: MenuDao) {
            if (catalogDao.getCategoryCount() > 0) return

            val seedCategories = listOf(
                InitialCategorySeed(
                    name = "محاشي جاهزة على التسوية",
                    items = listOf(
                        InitialProductSeed("كيلو محشي كرنب", 120.0),
                        InitialProductSeed("كيلو ورق عنب على التسوية", 150.0),
                        InitialProductSeed("كيلو محشي بتنجان", 100.0),
                        InitialProductSeed("كيلو محشي كوسة أو فلفل", 100.0)
                    )
                ),
                InitialCategorySeed(
                    name = "تجهيزات قبل الحشو",
                    items = listOf(
                        InitialProductSeed("كيلو كرنب مسلوق ومتقطع", 60.0),
                        InitialProductSeed("كيلو بطاطس متقورة", 45.0),
                        InitialProductSeed("كيلو بتنجان مقور", 35.0),
                        InitialProductSeed("كيلو كوسة متقورة", 40.0),
                        InitialProductSeed("كيلو ورق عنب مسلوق وجاهز على الحشو", 200.0)
                    )
                ),
                InitialCategorySeed(
                    name = "خضار جاهز",
                    items = listOf(
                        InitialProductSeed("كيلو سبانخ بالخضرة", 55.0),
                        InitialProductSeed("كيلو فاصوليا", 55.0),
                        InitialProductSeed("كيلو بامية متقمعة", 65.0),
                        InitialProductSeed("كيلو شوربة خضار", 65.0),
                        InitialProductSeed("كيلو بسلة بالجزر", 90.0),
                        InitialProductSeed("كيلو بصل مفروم", 45.0)
                    )
                ),
                InitialCategorySeed(
                    name = "تتبيلات جاهزة",
                    items = listOf(
                        InitialProductSeed("كيلو بانيه متبل وجاهز على القلي", 270.0),
                        InitialProductSeed("كيلو استربس متبل وجاهز على القلي", 280.0),
                        InitialProductSeed("كيلو شيش طاووق", 280.0),
                        InitialProductSeed("كيلو شاورما فراخ", 275.0),
                        InitialProductSeed("كيلو كفتة حاتي", 420.0),
                        InitialProductSeed("كيلو لحمة حواوشي", 400.0)
                    )
                ),
                InitialCategorySeed(
                    name = "صلصات وتجهيزات",
                    items = listOf(
                        InitialProductSeed("كيلو صلصة متسبكة قبل التسبيك", 40.0),
                        InitialProductSeed("كيلو صلصة بالبصل قبل التسبيك", 50.0),
                        InitialProductSeed("كيلو صلصة بالثوم قبل التسبيك", 50.0),
                        InitialProductSeed("كيلو تسيكة محشي بعد التسبيك", 90.0),
                        InitialProductSeed("كيلو الصلصة بعد التسبيك صافي", 70.0)
                    )
                ),
                InitialCategorySeed(
                    name = "متاح فراخ",
                    items = listOf(
                        InitialProductSeed("فراخ كاملة", 0.0, unit = "كيلو", isVariable = true),
                        InitialProductSeed("دباس", 0.0, unit = "كيلو", isVariable = true),
                        InitialProductSeed("وراك", 0.0, unit = "كيلو", isVariable = true),
                        InitialProductSeed("أجنحة", 0.0, unit = "كيلو", isVariable = true)
                    )
                )
            )

            val shouldSeedMenu = menuDao.getMenuCategoryCount() == 0

            seedCategories.forEachIndexed { catIndex, catSeed ->
                val catId = catalogDao.insertCategory(
                    CategoryEntity(
                        name = catSeed.name,
                        sortOrder = catIndex
                    )
                )

                val menuCatId = if (shouldSeedMenu) {
                    menuDao.insertMenuCategory(
                        MenuCategoryEntity(
                            name = catSeed.name,
                            sortOrder = catIndex,
                            isVisible = true
                        )
                    )
                } else 0L

                catSeed.items.forEachIndexed { itemIndex, prodSeed ->
                    val prodId = catalogDao.insertProduct(
                        ProductEntity(
                            name = prodSeed.name,
                            categoryId = catId,
                            unit = prodSeed.unit,
                            description = "",
                            purchaseCost = 0.0,
                            defaultSellingPrice = prodSeed.price,
                            isActive = true,
                            sortOrder = itemIndex
                        )
                    )

                    if (shouldSeedMenu && menuCatId > 0L) {
                        menuDao.insertMenuItem(
                            MenuItemEntity(
                                menuCategoryId = menuCatId,
                                productId = prodId,
                                displayName = prodSeed.name,
                                unitLabel = prodSeed.unit,
                                description = "",
                                publishedPrice = prodSeed.price,
                                isVariablePrice = prodSeed.isVariable,
                                sortOrder = itemIndex,
                                isVisible = true
                            )
                        )
                    }
                }
            }
        }
    }
}
