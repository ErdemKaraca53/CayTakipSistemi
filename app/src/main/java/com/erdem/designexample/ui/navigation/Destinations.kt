package com.erdem.designexample.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

/** Tüm navigasyon rotaları. */
object Routes {
    const val SIGNUP = "signup"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val RECORD_ENTRY = "record_entry"
    const val RECORDS = "records"
    const val PAYMENTS = "payments"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"

    /**
     * Düzenleme modunda RecordEntry rotası: "record_entry?editId=123"
     * AppNavHost'ta opsiyonel navArgument olarak tanımlı; -1 = yeni kayıt (default).
     */
    fun recordEntryEdit(id: Long) = "$RECORD_ENTRY?editId=$id"
}

/**
 * Ana ekrandaki (Dashboard) büyük navigasyon kartları. Alt çubuk yerine geçer;
 * 40+ kitle için büyük dokunmatik hedefler.
 */
enum class MainSection(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    RECORD_ENTRY(Routes.RECORD_ENTRY, "Yeni Kayıt", "Hasat girişi ekle", Icons.Outlined.AddCircleOutline),
    RECORDS(Routes.RECORDS, "Kayıtlar", "Geçmiş hasatları gör", Icons.Outlined.ReceiptLong),
    PAYMENTS(Routes.PAYMENTS, "Ödemeler", "Vade ve tahsilat takibi", Icons.Outlined.Payments),
    REPORTS(Routes.REPORTS, "Raporlar", "Özet ve grafikler", Icons.Outlined.BarChart)
}
