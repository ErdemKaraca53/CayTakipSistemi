package com.erdem.designexample

import android.app.Application
import com.erdem.designexample.notifications.NotificationHelper
import com.erdem.designexample.notifications.PaymentReminderScheduler
import dagger.hilt.android.HiltAndroidApp

/**
 * Uygulama giriş noktası. Hilt bağımlılık enjeksiyonunu başlatır.
 * Manifest'te android:name=".DesignApp" olarak tanımlıdır.
 *
 * Açılışta bildirim kanalını oluşturur ve günlük ödeme hatırlatma işini planlar (KEEP).
 * WorkManager, varsayılan başlatıcısıyla (androidx.startup) otomatik başlatılır; worker
 * bağımlılıklarını EntryPoint ile aldığı için özel WorkerFactory'e gerek yoktur.
 */
@HiltAndroidApp
class DesignApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        PaymentReminderScheduler.ensureScheduled(this)
    }
}
