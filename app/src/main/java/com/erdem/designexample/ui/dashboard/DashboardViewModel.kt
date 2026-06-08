package com.erdem.designexample.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdem.designexample.data.db.entity.HarvestEntity
import com.erdem.designexample.data.repository.HarvestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

/**
 * Ana ekran özeti: en son sürüm bilgisi, toplam kayıt meta'sı ve bekleyen ödeme özeti.
 * Tüm alanlar insan okunabilir metin olarak hazırlanır; Screen yalnızca gösterir.
 */
data class DashboardSummary(
    val latestSeasonLabel: String = "—",   // "2. Sürüm • 2025"
    val latestKg: String = "—",            // "210 Kg"  (o sürümün toplam mahsulü)
    val latestRevenue: String = "—",       // "3.402 TL" (o sürümün toplam geliri)
    val latestPending: String = "—",       // "2 ödeme"
    val recordsMeta: String = "—",         // "24 hasat • 5 bahçe"
    val paymentsMeta: String = "—"         // "2 bekleyen • 4.860 TL"
)

/**
 * Dashboard ekranının ViewModel'i.
 * [HarvestRepository.allHarvests] akışını tek geçişte özetleyerek [DashboardSummary] üretir.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: HarvestRepository
) : ViewModel() {

    /**
     * Başlangıç değeri [null] → Room'un ilk emisyonu gelmeden Screen yükleme göstergesi görünür.
     */
    val summary: StateFlow<DashboardSummary?> = repository.allHarvests
        .map { records -> buildSummary(records) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun buildSummary(records: List<HarvestEntity>): DashboardSummary {
        if (records.isEmpty()) {
            return DashboardSummary(
                latestSeasonLabel = "Kayıt yok",
                latestKg          = "—",
                latestRevenue     = "—",
                latestPending     = "0 ödeme",
                recordsMeta       = "Henüz kayıt eklenmedi",
                paymentsMeta      = "Bekleyen ödeme yok"
            )
        }

        // En son hasat (hasat tarihine göre)
        val latest = records.maxByOrNull { it.harvestDateEpoch }!!
        val latestDate = LocalDate.ofEpochDay(latest.harvestDateEpoch)

        // Aynı yıl + sürümdeki tüm kayıtlar → "bu sürümün" toplamı
        val sameSeasonRecords = records.filter { r ->
            val d = LocalDate.ofEpochDay(r.harvestDateEpoch)
            d.year == latestDate.year && r.season == latest.season
        }
        val seasonKg      = sameSeasonRecords.sumOf { it.weightKg.toDouble() }.toFloat()
        val seasonRevenue = sameSeasonRecords.sumOf { (it.weightKg * it.pricePerKg).toDouble() }.toFloat()

        // Vadesi henüz geçmemiş ödemeler
        val today = LocalDate.now()
        val pendingRecords = records.filter { LocalDate.ofEpochDay(it.dueDateEpoch).isAfter(today) }
        val pendingTotal   = pendingRecords.sumOf { (it.weightKg * it.pricePerKg).toDouble() }.toFloat()

        // Benzersiz bahçe sayısı
        val uniqueGardens = records.map { it.gardenName }.distinct().size

        return DashboardSummary(
            latestSeasonLabel = "${latest.season}. Sürüm • ${latestDate.year}",
            latestKg          = "${seasonKg.toInt()} Kg",
            latestRevenue     = formatMoney(seasonRevenue),
            latestPending     = "${pendingRecords.size} ödeme",
            recordsMeta       = "${records.size} hasat • $uniqueGardens bahçe",
            paymentsMeta      = "${pendingRecords.size} bekleyen • ${formatMoney(pendingTotal)}"
        )
    }

    private fun formatMoney(v: Float) =
        String.format(Locale.US, "%,.0f", v).replace(',', '.') + " TL"
}
