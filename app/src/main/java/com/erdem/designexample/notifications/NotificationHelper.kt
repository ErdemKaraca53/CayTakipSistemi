package com.erdem.designexample.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.erdem.designexample.MainActivity
import com.erdem.designexample.data.db.entity.HarvestEntity
import java.time.LocalDate
import java.util.Locale

/**
 * Bildirim kanalı kurulumu ve "vadesi yaklaşan ödeme" hatırlatma bildirimini gösterme.
 * Tek bir özet bildirim kullanılır (sabit id) — günlük worker tekrar çalışınca üst üste
 * yığılmaz, mevcut bildirim güncellenir.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "payment_reminders"
    private const val NOTIFICATION_ID = 1001

    /** Bildirim kanalını oluşturur (API 26+). Tekrar çağrılması zararsızdır. */
    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ödeme Hatırlatmaları",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Vadesi yaklaşan ödemeler için hatırlatma bildirimleri"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /**
     * Vadesi yaklaşan ödemeler için tek bir özet bildirim gösterir.
     * İzin yoksa (API 33+) sessizce atlar — çökme olmaz.
     */
    @SuppressLint("MissingPermission") // izin runtime'da kontrol ediliyor
    fun showPaymentReminder(context: Context, dueRecords: List<HarvestEntity>) {
        if (dueRecords.isEmpty()) return
        if (!NotificationPermission.isGranted(context)) return

        val (title, text) = buildContent(dueRecords)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // sistem hatırlatma (zil) ikonu
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    // --- İçerik üretimi -----------------------------------------------------------------------

    private fun buildContent(records: List<HarvestEntity>): Pair<String, String> {
        if (records.size == 1) {
            val r = records.first()
            val who = if (r.source == "CAYKUR") "ÇAYKUR" else r.companyName
            val due = LocalDate.ofEpochDay(r.dueDateEpoch)
            return "Vadesi yaklaşan ödeme" to
                "$who • ${money(r.weightKg * r.pricePerKg)} (vade: ${formatDate(due)})"
        }
        val total = records.sumOf { (it.weightKg * it.pricePerKg).toDouble() }.toFloat()
        return "Vadesi yaklaşan ödemeler" to
            "${records.size} ödemenin vadesi yaklaşıyor • toplam ${money(total)}"
    }

    private val trMonths = arrayOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )

    private fun formatDate(d: LocalDate) = "${d.dayOfMonth} ${trMonths[d.monthValue - 1]}"
    private fun money(v: Float) = String.format(Locale.US, "%,.0f", v).replace(',', '.') + " TL"
}
