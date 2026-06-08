package com.erdem.designexample.ui.payments

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
 * Ödemeler ekranının ViewModel'i.
 *
 * Tüm hasat kayıtlarını canlı akış olarak sağlar. Her kaydın [HarvestEntity.dueDateEpoch]
 * alanı vade tarihini taşır; bekleyen / gerçekleşen ayrımı Screen katmanında
 * bugünün tarihiyle yapılır (LocalDate.now()).
 */
@HiltViewModel
class PaymentsViewModel @Inject constructor(
    repository: HarvestRepository
) : ViewModel() {

    /**
     * Tüm hasat kayıtları — vade tarihi bilgisini içerir.
     * Başlangıç değeri [null] → Room'un ilk emisyonu gelmeden Screen yükleme göstergesi görünür.
     */
    val payments: StateFlow<List<HarvestEntity>?> = repository.allHarvests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
