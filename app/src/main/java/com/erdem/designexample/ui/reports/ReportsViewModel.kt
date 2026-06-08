package com.erdem.designexample.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdem.designexample.data.db.entity.HarvestEntity
import com.erdem.designexample.data.repository.HarvestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Raporlar ekranının ViewModel'i.
 *
 * Ham hasat kayıtlarını ve bahçe adlarını canlı akış olarak sağlar.
 * Aggregasyon (toplam Kg, toplam TL) ve filtre mantığı Screen katmanında
 * reaktif olarak hesaplanır — bu sayede gereksiz ara state ve Flow dönüşümü önlenir.
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    repository: HarvestRepository
) : ViewModel() {

    /**
     * Tüm hasat kayıtları. Screen bu akışı filtre seçimlerine göre anında toplar
     * ([aggregateReal] / [buildSeriesReal] fonksiyonları).
     * Başlangıç değeri [null] → Room'un ilk emisyonu gelmeden Screen yükleme göstergesi görünür.
     */
    val allHarvests: StateFlow<List<HarvestEntity>?> = repository.allHarvests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Bahçe adları. Rapor filtresindeki "Hangi bahçe?" seçenekleri için kullanılır;
     * yeni bahçe eklenince otomatik güncellenir.
     */
    val gardenNames: StateFlow<List<String>> = repository.gardenNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
