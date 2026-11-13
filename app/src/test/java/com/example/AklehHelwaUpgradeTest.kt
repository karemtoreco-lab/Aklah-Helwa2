package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.BusinessRepository
import com.example.data.OrderRepository
import com.example.model.AppUser
import com.example.model.Order
import com.example.model.OrderLineItemEntity
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.model.ProductEntity
import com.example.model.ProductImageEntity
import com.example.util.Base64ImageUtils
import com.example.util.MenuPosterRenderer
import com.example.util.MenuPosterSection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AklehHelwaUpgradeTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var orderRepo: OrderRepository
    private lateinit var businessRepo: BusinessRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        orderRepo = OrderRepository(db.orderDao())
        businessRepo = BusinessRepository(db.catalogDao(), db.menuDao(), db.expenseDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `initial seed has exact 6 categories 30 products and zero demo orders expenses or images`() = runBlocking {
        businessRepo.ensureInitialCatalog()

        val categories = businessRepo.allCategories.first()
        val products = businessRepo.allProducts.first()
        val menuCategories = businessRepo.menuCategories.first()
        val menuItems = businessRepo.menuItems.first()
        val orders = orderRepo.allOrders.first()
        val expenses = businessRepo.allExpenses.first()
        val images = businessRepo.allProductImages.first()

        assertEquals(6, categories.size)
        assertEquals(30, products.size)
        assertEquals(6, menuCategories.size)
        assertEquals(30, menuItems.size)

        // Zero demo data
        assertEquals(0, orders.size)
        assertEquals(0, expenses.size)
        assertEquals(0, images.size)

        // Verify exact chicken variable-price products exist
        val chickenNames = products.filter { it.defaultSellingPrice == 0.0 }.map { it.name }
        assertTrue(chickenNames.containsAll(listOf("فراخ كاملة", "دباس", "وراك", "أجنحة")))
        assertEquals(120.0, products.first { it.name == "كيلو محشي كرنب" }.defaultSellingPrice, 0.001)
    }

    @Test
    fun `adding product to catalog does not automatically add to published menu`() = runBlocking {
        businessRepo.ensureInitialCatalog()
        val catId = businessRepo.allCategories.first().first().id
        val initialMenuCount = businessRepo.menuItems.first().size

        val newProdId = businessRepo.insertProduct(
            ProductEntity(
                name = "طاجن ورق عنب بالكوارع",
                categoryId = catId,
                unit = "طاجن",
                purchaseCost = 180.0,
                defaultSellingPrice = 320.0
            )
        )

        assertTrue(newProdId > 0)
        assertEquals(31, businessRepo.allProducts.first().size)
        assertEquals(initialMenuCount, businessRepo.menuItems.first().size)
    }

    @Test
    fun `editing menu price does not affect product cost or price and removing menu item preserves product`() = runBlocking {
        businessRepo.ensureInitialCatalog()
        val product = businessRepo.allProducts.first().first { it.name == "كيلو محشي كرنب" }
        businessRepo.updateProduct(product.copy(purchaseCost = 70.0))
        val menuItem = businessRepo.menuItems.first().first { it.productId == product.id }

        // Edit published menu price independently
        businessRepo.updateMenuItem(menuItem.copy(publishedPrice = 145.0))
        val updatedProduct = businessRepo.allProducts.first().first { it.id == product.id }
        assertEquals(120.0, updatedProduct.defaultSellingPrice, 0.001)
        assertEquals(70.0, updatedProduct.purchaseCost, 0.001)

        // Remove from published menu without deleting product
        businessRepo.deleteMenuItem(menuItem)
        assertFalse(businessRepo.menuItems.first().any { it.id == menuItem.id })
        assertTrue(businessRepo.allProducts.first().any { it.id == product.id })
    }

    @Test
    fun `multi product order computes totals accurately and preserves historical snapshots`() = runBlocking {
        businessRepo.ensureInitialCatalog()
        val p1 = businessRepo.allProducts.first().first { it.name == "كيلو محشي كرنب" }
        val p2 = businessRepo.allProducts.first().first { it.name == "كيلو شيش طاووق" }

        val line1 = OrderLineItemEntity(
            orderId = 0L,
            productId = p1.id,
            productNameSnapshot = p1.name,
            quantity = 2.0,
            unitPurchaseCost = 75.0,
            unitSellingPrice = 120.0,
            totalPurchaseCost = 150.0,
            totalSellingPrice = 240.0,
            itemGrossProfit = 90.0
        )
        val line2 = OrderLineItemEntity(
            orderId = 0L,
            productId = p2.id,
            productNameSnapshot = p2.name,
            quantity = 1.0,
            unitPurchaseCost = 200.0,
            unitSellingPrice = 280.0,
            totalPurchaseCost = 200.0,
            totalSellingPrice = 280.0,
            itemGrossProfit = 80.0
        )

        val order = Order(
            customerName = "أم كريم",
            itemsDescription = "2 × كيلو محشي كرنب، 1 × كيلو شيش طاووق",
            salePrice = 520.0,
            costPrice = 350.0,
            netProfit = 170.0,
            yearMonth = "2026-11",
            status = OrderStatus.DELIVERED.name,
            paymentStatus = PaymentStatus.PAID.name
        )

        val savedId = orderRepo.saveOrderWithItems(order, listOf(line1, line2))
        assertTrue(savedId > 0)

        // Now modify and delete original products from catalog
        businessRepo.updateProduct(p1.copy(name = "اسم معدل", defaultSellingPrice = 999.0))
        businessRepo.deleteProduct(p2)

        // Verify historical order snapshots are completely untouched!
        val savedOrder = orderRepo.allOrders.first().first { it.id == savedId }
        val savedItems = orderRepo.getLineItemsForOrder(savedId)
        assertEquals(2, savedItems.size)
        assertEquals("كيلو محشي كرنب", savedItems[0].productNameSnapshot)
        assertEquals(120.0, savedItems[0].unitSellingPrice, 0.001)
        assertEquals("كيلو شيش طاووق", savedItems[1].productNameSnapshot)
        assertEquals(170.0, savedOrder.netProfit, 0.001)
    }

    @Test
    fun `image gallery auto promotes primary and reassigns primary on deletion`() = runBlocking {
        businessRepo.ensureInitialCatalog()
        val prodId = businessRepo.allProducts.first().first().id

        val img1Id = businessRepo.insertProductImage(ProductImageEntity(productId = prodId, imagePath = "/tmp/a.jpg"))
        val img2Id = businessRepo.insertProductImage(ProductImageEntity(productId = prodId, imagePath = "/tmp/b.jpg"))

        var imgs = businessRepo.allProductImages.first()
        assertEquals(2, imgs.size)
        assertTrue(imgs.first { it.id == img1Id }.isPrimary)
        assertFalse(imgs.first { it.id == img2Id }.isPrimary)

        businessRepo.setPrimaryImage(prodId, img2Id)
        imgs = businessRepo.allProductImages.first()
        assertTrue(imgs.first { it.id == img2Id }.isPrimary)

        businessRepo.deleteProductImage(imgs.first { it.id == img2Id })
        imgs = businessRepo.allProductImages.first()
        assertEquals(1, imgs.size)
        assertTrue(imgs.first().isPrimary)
    }

    @Test
    fun `migration 1 to 4 preserves existing v1 order records and initializes v4 defaults`() {
        val dbName = "migration_test_db"
        context.deleteDatabase(dbName)

        // 1. Create a v1 schema database with 1 order row
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `orders` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `customerName` TEXT NOT NULL,
                            `customerPhone` TEXT NOT NULL,
                            `itemsDescription` TEXT NOT NULL,
                            `salePrice` REAL NOT NULL,
                            `costPrice` REAL NOT NULL,
                            `netProfit` REAL NOT NULL,
                            `orderDate` INTEGER NOT NULL,
                            `yearMonth` TEXT NOT NULL,
                            `status` TEXT NOT NULL,
                            `paymentStatus` TEXT NOT NULL,
                            `notes` TEXT NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_yearMonth` ON `orders` (`yearMonth`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_orderDate` ON `orders` (`orderDate`)")
                    db.execSQL(
                        """
                        INSERT INTO orders (customerName, customerPhone, itemsDescription, salePrice, costPrice, netProfit, orderDate, yearMonth, status, paymentStatus, notes)
                        VALUES ('عميل قديم', '0100000000', 'محشي مشكل', 300.0, 180.0, 120.0, 1700000000000, '2026-11', 'DELIVERED', 'PAID', 'محفوظ')
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        helper.writableDatabase.close()

        // 2. Open with Room v4 + MIGRATION_1_2 + MIGRATION_2_3 + MIGRATION_3_4 and verify old order survived
        val migratedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4
            )
            .allowMainThreadQueries()
            .build()

        runBlocking {
            val orders = migratedDb.orderDao().getAllOrders().first()
            assertEquals(1, orders.size)
            assertEquals("عميل قديم", orders.first().customerName)
            assertEquals(120.0, orders.first().netProfit, 0.001)
            assertEquals("ADMIN", orders.first().orderSource)
            assertEquals("", orders.first().deliveryAddress)
            assertEquals("", orders.first().deliveryZone)
            assertEquals(0.0, orders.first().shippingFee, 0.001)
            assertFalse(orders.first().isRated)
        }
        migratedDb.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `customer order cloudId deduplication and source helper work accurately`() = runBlocking {
        val customerOrder = Order(
            customerName = "هدى محمود",
            customerPhone = "01012345678",
            itemsDescription = "1 × كيلو محشي كرنب",
            salePrice = 120.0,
            costPrice = 70.0,
            netProfit = 50.0,
            yearMonth = "2026-11",
            status = OrderStatus.PENDING.name,
            paymentStatus = PaymentStatus.UNPAID.name,
            deliveryAddress = "المعادي - شارع 9",
            orderSource = com.example.model.OrderSource.CUSTOMER.name,
            cloudId = "cloud-order-xyz",
            deliveryZone = "المعادي والمقطم",
            shippingFee = 45.0
        )
        assertTrue(customerOrder.isFromCustomer)
        orderRepo.saveOrderWithItems(customerOrder, emptyList())
        val found = orderRepo.getOrderByCloudId("cloud-order-xyz")
        assertNotNull(found)
        assertEquals("المعادي - شارع 9", found?.deliveryAddress)
        assertEquals("المعادي والمقطم", found?.deliveryZone)
        assertEquals(45.0, found?.shippingFee ?: 0.0, 0.001)
        assertTrue(found?.isFromCustomer == true)
    }

    @Test
    fun `menu poster renderer generates valid high resolution pages`() = runBlocking {
        businessRepo.ensureInitialCatalog()
        val cats = businessRepo.menuCategories.first()
        val itemsByCat = businessRepo.menuItems.first().groupBy { it.menuCategoryId }
        val sections = cats.map { MenuPosterSection(it, itemsByCat[it.id].orEmpty()) }

        val fullPages = MenuPosterRenderer.renderMenuPages(context, sections)
        assertTrue(fullPages.isNotEmpty())
        fullPages.forEach { bmp ->
            assertEquals(1440, bmp.width)
            assertEquals(2040, bmp.height)
            bmp.recycle()
        }

        val emptyPages = MenuPosterRenderer.renderMenuPages(context, emptyList())
        assertEquals(1, emptyPages.size)
        assertNotNull(emptyPages.first())
        emptyPages.first().recycle()
    }

    @Test
    fun `whatsapp number sanitizer handles Egyptian local international and formatted inputs`() {
        assertEquals("201012345678", com.example.util.WhatsAppUtils.sanitizeWhatsAppNumber("01012345678"))
        assertEquals("201012345678", com.example.util.WhatsAppUtils.sanitizeWhatsAppNumber("+20 101-234-5678"))
        assertEquals("201012345678", com.example.util.WhatsAppUtils.sanitizeWhatsAppNumber("00201012345678"))
        assertEquals("", com.example.util.WhatsAppUtils.sanitizeWhatsAppNumber("   "))
    }

    @Test
    fun `whatsapp message builder formats delivery zone shipping fee and grand total`() = runBlocking {
        businessRepo.ensureInitialCatalog()
        val kornob = businessRepo.menuItems.first().first { it.displayName == "كيلو محشي كرنب" }
        val cart = listOf(
            com.example.model.CustomerCartItem(menuItem = kornob, quantity = 2.0)
        )
        val total = cart.sumOf { it.lineTotal }
        val msg = com.example.util.WhatsAppUtils.buildCustomerOrderWhatsAppMessage(
            customerName = "سارة أحمد",
            customerPhone = "01099887766",
            deliveryAddress = "مدينة نصر - شارع عباس العقاد",
            deliveryZone = "مدينة نصر ومصر الجديدة",
            shippingFee = 35.0,
            notes = "بدون شطة",
            cartItems = cart,
            totalAmount = total
        )

        assertTrue(msg.contains("سارة أحمد"))
        assertTrue(msg.contains("01099887766"))
        assertTrue(msg.contains("مدينة نصر ومصر الجديدة"))
        assertTrue(msg.contains("مدينة نصر - شارع عباس العقاد"))
        assertTrue(msg.contains("2 × كيلو محشي كرنب = 240 ج.م"))
        assertTrue(msg.contains("35 ج.م"))
        assertTrue(msg.contains("275 ج.م"))
        assertTrue(msg.contains("بدون شطة"))
        assertEquals("جديد (طلب عميل)", OrderStatus.NEW_CUSTOMER.titleAr)
    }

    @Test
    fun `app user live online presence respects 90 second heartbeat cutoff`() {
        val now = 1_000_000L
        val activeUser = AppUser(userId = "u1", username = "نهى", isOnline = true, lastSeen = now - 30_000L)
        val staleUser = AppUser(userId = "u2", username = "سارة", isOnline = true, lastSeen = now - 95_000L)
        val offlineUser = AppUser(userId = "u3", username = "منى", isOnline = false, lastSeen = now - 1_000L)

        assertTrue(activeUser.isLiveOnline(now))
        assertFalse(staleUser.isLiveOnline(now))
        assertFalse(offlineUser.isLiveOnline(now))
    }

    @Test
    fun `base64 photos json helper clamps max 3 images and roundtrips cleanly`() {
        val encoded = Base64ImageUtils.encodePhotosJson(listOf("img1", "img2", "img3", "img4_ignored"))
        val decoded = Base64ImageUtils.parsePhotosJson(encoded)
        assertEquals(listOf("img1", "img2", "img3"), decoded)
        assertTrue(Base64ImageUtils.parsePhotosJson("[]").isEmpty())
    }

    @Test
    fun `order status 4-step pipeline progresses from review to preparing to out for delivery to delivered`() {
        assertEquals("قيد المراجعة", OrderStatus.trackingTitle(OrderStatus.PENDING_REVIEW.name))
        assertEquals(OrderStatus.PREPARING, OrderStatus.nextAdminStatus(OrderStatus.PENDING_REVIEW.name))
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.nextAdminStatus(OrderStatus.PREPARING.name))
        assertEquals(OrderStatus.DELIVERED, OrderStatus.nextAdminStatus(OrderStatus.OUT_FOR_DELIVERY.name))
        assertEquals(OrderStatus.PENDING_REVIEW, OrderStatus.nextAdminStatus(OrderStatus.DELIVERED.name))
    }

    @Test
    fun `default cloud config contains hardcoded akla-helwa firebase credentials`() {
        val cfg = com.example.model.CloudConfig()
        assertTrue(cfg.isConfigured)
        assertEquals("akla-helwa", cfg.projectId)
        assertEquals("1:689898276457:android:3d7dcc504a144e9b2144f8", cfg.appId)
        assertEquals("AIzaSyBG6YJFv-n-heOUNVYV7oxMQMYxh1eOpnY", cfg.apiKey)

        val state = com.example.model.CloudSyncState.Connected(cfg.projectId)
        assertTrue(state.isCloudConnected)
        assertEquals("متصل بالسحابة تلقائياً (akla-helwa)", state.statusMessage)
    }
}
