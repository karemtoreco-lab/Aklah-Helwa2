package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppPreferences
import com.example.data.CloudSyncManager
import com.example.model.CloudConfig
import com.example.util.DateUtils
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration

/**
 * Admin-only background Foreground Service (`START_STICKY`) that monitors
 * the Firestore `orders` collection in real time and triggers a high-priority
 * sound + vibration push notification whenever a new customer order arrives.
 */
class OrderNotificationService : Service() {

    private var ordersListener: ListenerRegistration? = null
    private val notifiedOrderIds = HashSet<String>()
    private var isInitialSnapshot = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = AppPreferences(applicationContext)
        if (!prefs.isAdminDeviceSync()) {
            stopSelf()
            return START_NOT_STICKY
        }

        ensureNotificationChannels()
        startPersistentForeground()
        attachOrdersListener()
        return START_STICKY
    }

    private fun startPersistentForeground() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_OPEN_ADMIN_ORDERS, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            100,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_FOREGROUND_MONITOR)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("أكلة حلوة — وضع الإدارة")
            .setContentText("متصل لاستقبال إشعارات الطلبات الجديدة فوراً")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        ServiceCompat.startForeground(this, FOREGROUND_NOTIFICATION_ID, notification, fgsType)
    }

    private fun attachOrdersListener() {
        if (ordersListener != null) return
        try {
            val db = CloudSyncManager.getOrInitFirestore(applicationContext, CloudConfig())
            isInitialSnapshot = true
            ordersListener = db.collection(CloudSyncManager.COL_ORDERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    if (isInitialSnapshot) {
                        snapshot.documents.forEach { notifiedOrderIds.add(it.id) }
                        isInitialSnapshot = false
                        return@addSnapshotListener
                    }

                    for (change in snapshot.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {
                            val doc = change.document
                            if (notifiedOrderIds.add(doc.id)) {
                                val customerName = doc.getString("customerName")?.ifBlank { "عميل" } ?: "عميل"
                                val totalPrice = doc.getDouble("salePrice") ?: 0.0
                                showHighPriorityOrderNotification(doc.id, customerName, totalPrice)
                            }
                        }
                    }
                }
        } catch (_: Exception) {
            // Firestore initialization retry happens on next restart
        }
    }

    private fun showHighPriorityOrderNotification(
        orderDocId: String,
        customerName: String,
        totalPrice: Double
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openOrdersIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_OPEN_ADMIN_ORDERS, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            orderDocId.hashCode(),
            openOrdersIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val totalFormatted = DateUtils.formatCurrency(totalPrice)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, CHANNEL_NEW_ORDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("طلب جديد وصل لـ أكلة حلوة \uD83C\uDF72")
            .setContentText("عميل جديد: $customerName — الإجمالي: $totalFormatted")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("عميل جديد: $customerName — الإجمالي: $totalFormatted\nاضغطي لمراجعة تفاصيل الطلب فوراً")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0L, 350L, 180L, 450L))
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(orderDocId.hashCode(), notification)
    }

    private fun ensureNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val monitorChannel = NotificationChannel(
            CHANNEL_FOREGROUND_MONITOR,
            "مراقبة الطلبات الجديدة",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "يعمل في الخلفية لمتابعة طلبات العملاء على جهاز الإدارة فقط"
            setShowBadge(false)
        }
        manager.createNotificationChannel(monitorChannel)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val ordersChannel = NotificationChannel(
            CHANNEL_NEW_ORDERS,
            "طلبات العملاء الجديدة",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "تنبيهات صوتية فورية عند وصول طلب جديد"
            enableVibration(true)
            vibrationPattern = longArrayOf(0L, 350L, 180L, 450L)
            setSound(soundUri, audioAttr)
        }
        manager.createNotificationChannel(ordersChannel)
    }

    override fun onDestroy() {
        ordersListener?.remove()
        ordersListener = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_FOREGROUND_MONITOR = "akleh_helwa_admin_monitor"
        const val CHANNEL_NEW_ORDERS = "akleh_helwa_new_orders_high"
        const val FOREGROUND_NOTIFICATION_ID = 4101
        const val EXTRA_OPEN_ADMIN_ORDERS = "extra_open_admin_orders"

        fun start(context: Context) {
            val intent = Intent(context, OrderNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OrderNotificationService::class.java))
        }
    }
}
