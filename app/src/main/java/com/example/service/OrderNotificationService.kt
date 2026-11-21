package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppPreferences
import com.example.data.CloudSyncManager
import com.example.model.CloudConfig
import com.example.util.DateUtils
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration

class OrderNotificationService : Service() {

    private var ordersListener: ListenerRegistration? = null
    private var offersListener: ListenerRegistration? = null
    private val notifiedOrderIds = HashSet<String>()
    private var isInitialOrdersSnapshot = true
    private var lastNotifiedOfferStamp: Long = -1L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureNotificationChannels(applicationContext)
        // Cancel any lingering legacy persistent foreground notification immediately
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(LEGACY_FOREGROUND_NOTIFICATION_ID)

        val prefs = AppPreferences(applicationContext)
        if (prefs.isAdminDeviceSync()) {
            attachOrdersListener()
        }
        attachOffersListener()
        return START_STICKY
    }

    private fun attachOrdersListener() {
        if (ordersListener != null) return
        try {
            val db = CloudSyncManager.getOrInitFirestore(applicationContext, CloudConfig())
            isInitialOrdersSnapshot = true
            ordersListener = db.collection(CloudSyncManager.COL_ORDERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    if (isInitialOrdersSnapshot) {
                        snapshot.documents.forEach { notifiedOrderIds.add(it.id) }
                        isInitialOrdersSnapshot = false
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
        }
    }

    private fun attachOffersListener() {
        if (offersListener != null) return
        try {
            val db = CloudSyncManager.getOrInitFirestore(applicationContext, CloudConfig())
            offersListener = db.collection(CloudSyncManager.COL_STORE_SETTINGS)
                .document(CloudSyncManager.DOC_STORE_PROFILE)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val offerText = snapshot.getString("dailyOfferText")?.trim().orEmpty()
                    val offerStamp = snapshot.getLong("dailyOfferUpdatedAt") ?: 0L
                    if (lastNotifiedOfferStamp == -1L) {
                        lastNotifiedOfferStamp = offerStamp
                        return@addSnapshotListener
                    }
                    if (offerText.isNotBlank() && offerStamp > lastNotifiedOfferStamp) {
                        lastNotifiedOfferStamp = offerStamp
                        showOfferBroadcastNotification(applicationContext, offerText)
                    }
                }
        } catch (_: Exception) {
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
            .setOngoing(false)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0L, 350L, 180L, 450L))
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(orderDocId.hashCode(), notification)
    }

    override fun onDestroy() {
        ordersListener?.remove()
        ordersListener = null
        offersListener?.remove()
        offersListener = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_NEW_ORDERS = "akleh_helwa_new_orders_high"
        const val CHANNEL_OFFERS = "akleh_helwa_daily_offers"
        const val LEGACY_FOREGROUND_NOTIFICATION_ID = 4101
        const val EXTRA_OPEN_ADMIN_ORDERS = "extra_open_admin_orders"

        fun ensureNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

            val offersChannel = NotificationChannel(
                CHANNEL_OFFERS,
                "عروض أكلة حلوة اليومية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات فورية بالعروض والخصومات الجديدة"
                enableVibration(true)
                setSound(soundUri, audioAttr)
            }
            manager.createNotificationChannel(offersChannel)
        }

        fun showOfferBroadcastNotification(context: Context, offerText: String) {
            if (offerText.isBlank()) return
            ensureNotificationChannels(context)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                offerText.hashCode(),
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notification = NotificationCompat.Builder(context, CHANNEL_OFFERS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("عرض جديد من أكلة حلوة \uD83D\uDD25")
                .setContentText(offerText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(offerText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOngoing(false)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .build()
            manager.notify(7700 + (offerText.hashCode() and 0x0FFF), notification)
        }

        fun start(context: Context) {
            try {
                context.startService(Intent(context, OrderNotificationService::class.java))
            } catch (_: Exception) {
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, OrderNotificationService::class.java))
            } catch (_: Exception) {
            }
        }
    }
}
