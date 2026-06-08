package com.erdem.designexample.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdem.designexample.data.db.entity.HarvestEntity
import com.erdem.designexample.data.repository.HarvestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Aktif filtre seçimleri. null = "Tümü". */
data class RecordsFilter(
    val year: Int? = LocalDate.now().year,
    val season: Int? = null,     // 1–4, null = tümü
    val gardenName: String? = null
)

/** Silme onayı için tutulan kayıt. */
data class RecordsUiState(
    val filter: RecordsFilter = RecordsFilter(),
    val pendingDeleteId: Long? = null,   // null = dialog kapalı
    val snackMessage: String? = null
)

/**
 * Kayıtlar ekranının ViewModel'i. Filtrelenmiş kayıt listesini ve toplam istatistikleri
 * Flow olarak sağlar; silme işlemini yönetir.
 */
@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val repository: HarvestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    /** Bahçe adları autocomplete için (filtre seçiminde kullanılır). */
    val gardenNames: StateFlow<List<String>> = repository.gardenNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Aktif filtreye göre kayıt listesi akışı.
     * Başlangıç değeri [null] → Room'un ilk emisyonu gelmeden Screen yükleme göstergesi görünür.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val records: StateFlow<List<HarvestEntity>?> = _uiState
        .map { it.filter }
        .flatMapLatest { f ->
            repository.getFiltered(
                year = f.year,
                season = f.season,
                garden = f.gardenName
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // --- Filtre ---

    fun setFilter(filter: RecordsFilter) = _uiState.update { it.copy(filter = filter) }

    fun setYear(year: Int?) = _uiState.update { it.copy(filter = it.filter.copy(year = year)) }
    fun setSeason(season: Int?) = _uiState.update { it.copy(filter = it.filter.copy(season = season)) }
    fun setGarden(garden: String?) = _uiState.update { it.copy(filter = it.filter.copy(gardenName = garden?.takeIf { n -> n.isNotBlank() })) }

    // --- Silme ---

    /** Silme onay dialogunu açar. */
    fun requestDelete(id: Long) = _uiState.update { it.copy(pendingDeleteId = id) }

    /** Silme onaylandı. */
    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        _uiState.update { it.copy(pendingDeleteId = null) }
        viewModelScope.launch {
            runCatching { repository.deleteHarvest(id) }
                .onSuccess { _uiState.update { s -> s.copy(snackMessage = "Kayıt silindi.") } }
                .onFailure { _uiState.update { s -> s.copy(snackMessage = "Silinemedi, tekrar dene.") } }
        }
    }

    /** Silme iptal edildi. */
    fun cancelDelete() = _uiState.update { it.copy(pendingDeleteId = null) }

    /** Snackbar gösterildikten sonra çağrılır. */
    fun consumeMessage() = _uiState.update { it.copy(snackMessage = null) }
}
