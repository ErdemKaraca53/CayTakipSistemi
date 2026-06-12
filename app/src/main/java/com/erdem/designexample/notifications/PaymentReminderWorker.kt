package com.erdem.designexample.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.erdem.designexample.data.db.dao.HarvestDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate

/**
 * Günlük çalışan arka plan işi: vadesi yaklaşan ödemeleri Room'dan okur ve bildirim gösterir.
 * Uygulama kapalı olsa bile WorkManager tarafından tetiklenir; yalnızca yerel veri okuduğu için
 * internet gerektirmez.
 *
 * Not: `@HiltWorker` yerine **EntryPoint** kullanılıyor — androidx.hilt-compiler işlemcisi
 * Kotlin 2.1 metadata'sını okuyamayıp derleme hatası veriyordu. Bağımlılık ([HarvestDao]) mevcut
 * Dagger Hilt grafiğinden EntryPoint ile alınır; bu yüzden ek bir KSP işlemcisi gerekmez.
 */
class PaymentReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    /** Worker doğrudan enjekte edilmediği için bağımlılıklar bu EntryPoint üzerinden alınır. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun harvestDao(): HarvestDao
    }

    override suspend fun doWork(): Result {
        val harvestDao = EntryPointAccessors
            .fromApplication(applicationContext, Deps::class.java)
            .harvestDao()

        val today = LocalDate.now()
        val windowEnd = today.plusDays(REMINDER_WINDOW_DAYS)

        val due = harvestDao.getDueBetween(today.toEpochDay(), windowEnd.toEpochDay())
        if (due.isNotEmpty()) {
            NotificationHelper.showPaymentReminder(applicationContext, due)
        }
        return Result.success()
    }

    companion object {
        /** Bugünden itibaren kaç gün içindeki vadeler "yaklaşan" sayılır. */
        const val REMINDER_WINDOW_DAYS = 3L
    }
}
