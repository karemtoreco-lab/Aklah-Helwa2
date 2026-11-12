package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(entities = [Order::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "akleh_helwa_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
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
                        populateInitialOrders(database.orderDao())
                    }
                }
            }
        }

        suspend fun populateInitialOrders(dao: OrderDao) {
            val now = Calendar.getInstance()
            val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
            val currentYearMonth = monthFormat.format(now.time)

            // Calculate dates within current month
            val cal = Calendar.getInstance()
            
            val sampleOrders = listOf(
                Order(
                    customerName = "أم محمد",
                    customerPhone = "01012345678",
                    itemsDescription = "طاجن مكرونة بشاميل باللحمة المفرومة + صينية جلاش بالجبنة",
                    salePrice = 450.0,
                    costPrice = 260.0,
                    netProfit = 190.0,
                    orderDate = System.currentTimeMillis() - 86400000L * 1, // yesterday
                    yearMonth = currentYearMonth,
                    status = OrderStatus.DELIVERED.name,
                    paymentStatus = PaymentStatus.PAID.name,
                    notes = "توصيل للبيت الساعة ٤ عصراً سخن جاهز"
                ),
                Order(
                    customerName = "أحمد طارق",
                    customerPhone = "01198765432",
                    itemsDescription = "كيلو ونص محشي مشكل (كرنب وورق عنب وكوسة) + بطة محمرة بلدي",
                    salePrice = 900.0,
                    costPrice = 540.0,
                    netProfit = 360.0,
                    orderDate = System.currentTimeMillis() - 86400000L * 2,
                    yearMonth = currentYearMonth,
                    status = OrderStatus.DELIVERED.name,
                    paymentStatus = PaymentStatus.PAID.name,
                    notes = "عزومة عائلية - مع ليمون ومخلل مشكل"
                ),
                Order(
                    customerName = "مريم إبراهيم",
                    customerPhone = "01234567890",
                    itemsDescription = "٥ أرغفة حواوشي إسكندراني بلدي بالجبنة الموتزاريلا + سلطة طحينة",
                    salePrice = 380.0,
                    costPrice = 220.0,
                    netProfit = 160.0,
                    orderDate = System.currentTimeMillis() - 86400000L * 3,
                    yearMonth = currentYearMonth,
                    status = OrderStatus.DELIVERED.name,
                    paymentStatus = PaymentStatus.PAID.name,
                    notes = "شطة خفيفة"
                ),
                Order(
                    customerName = "د. سامي العزازي",
                    customerPhone = "01555543210",
                    itemsDescription = "صينية كفتة حاتي مشوية كيلو + كيلو أرز بسمتي بالمكسرات + سلطة خضراء",
                    salePrice = 750.0,
                    costPrice = 460.0,
                    netProfit = 290.0,
                    orderDate = System.currentTimeMillis() - 86400000L * 5,
                    yearMonth = currentYearMonth,
                    status = OrderStatus.DELIVERED.name,
                    paymentStatus = PaymentStatus.PAID.name,
                    notes = "استلام من المطبخ الساعة ٥"
                ),
                Order(
                    customerName = "مدام نادية",
                    customerPhone = "01099887766",
                    itemsDescription = "طاجن عكاوي بالبصل القاورما + صينية رقاق بلحمة مفرومة",
                    salePrice = 980.0,
                    costPrice = 610.0,
                    netProfit = 370.0,
                    orderDate = System.currentTimeMillis() - 86400000L * 7,
                    yearMonth = currentYearMonth,
                    status = OrderStatus.DELIVERED.name,
                    paymentStatus = PaymentStatus.PAID.name,
                    notes = "تسليم للمهندسين"
                ),
                Order(
                    customerName = "أستاذة هند الشافعي",
                    customerPhone = "01122334455",
                    itemsDescription = "صينية بسبوسة مكرملة بالسمنة البلدي + صينية كنافة نابلسية بالقشطة",
                    salePrice = 420.0,
                    costPrice = 230.0,
                    netProfit = 190.0,
                    orderDate = System.currentTimeMillis(),
                    yearMonth = currentYearMonth,
                    status = OrderStatus.PENDING.name,
                    paymentStatus = PaymentStatus.PAID.name,
                    notes = "أوردر اليوم تحت التجهيز للعشاء"
                )
            )

            dao.insertOrders(sampleOrders)
        }
    }
}
