package com.erdem.designexample.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Günlük ödeme hatırlatma işini planlar. [ExistingPeriodicWorkPolicy.KEEP] sayesinde yalnızca
 * bir kez planlanır; uygulama her açıldığında çağrılması zararsızdır. WorkManager işi cihaz
 * yeniden başlatılsa bile korur (uygulama açılmasa da çalışır).
 */
object PaymentReminderScheduler {

    private const val WORK_NAME = "payment_reminder_daily"

    fun ensureScheduled(context: Context) {
        val request = PeriodicWorkRequestBuilder<PaymentReminderWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Tek seferlik anlık kontrol — örn. kullanıcı bildirim iznini yeni verdiğinde,
     * günlük döngüyü beklemeden vadesi yaklaşan ödeme varsa hemen hatırlatır.
     */
    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<PaymentReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
