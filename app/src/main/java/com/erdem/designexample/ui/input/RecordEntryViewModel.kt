package com.erdem.designexample.ui.input

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdem.designexample.data.db.entity.HarvestEntity
import com.erdem.designexample.data.repository.HarvestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import javax.inject.Inject

/** Hangi form sayfasında olunduğu. */
enum class RecordSource(val displayName: String) {
    CAYKUR("ÇAYKUR"),
    OZEL("Özel")
}

/** Kayıt ekleme formunun tüm alan durumları. */
data class RecordFormState(
    val source: RecordSource = RecordSource.CAYKUR,
    val gardenName: String = "",
    val companyName: String = "ÇAYKUR",         // ÇAYKUR için sabit; Özel için serbest
    val harvestDate: LocalDate = LocalDate.now(),
    val season: Int = 0,                         // 0 = seçilmedi, 1–4
    val weightKg: String = "",
    val pricePerKg: String = CAYKUR_PRICE_STR,  // ÇAYKUR için sabit 35 TL; Özel için serbest
    val dueDate: LocalDate = caykurDueDate(LocalDate.now()) // ÇAYKUR başlangıç değeri
)

/** ÇAYKUR sabit fiyatı. */
private const val CAYKUR_PRICE_STR = "35"

/**
 * Firma adlarını standartlaştırmak için Türkçe locale. "iğdır" → "İĞDIR" gibi
 * doğru büyük harf dönüşümü için gerekir (İngilizce locale "i" → "I" yapar, yanlış).
 */
private val TR_LOCALE = Locale.forLanguageTag("tr-TR")

/**
 * ÇAYKUR vade tarihi hesabı: satış ayının bir sonraki ayının son günü.
 * Örnek: 3 Temmuz → 31 Ağustos | 29 Haziran → 31 Temmuz
 */
private fun caykurDueDate(harvestDate: LocalDate): LocalDate =
    YearMonth.from(harvestDate).plusMonths(1).atEndOfMonth()

/** Kaydet işleminin sonuç durumu. */
sealed interface SaveState {
    data object Idle : SaveState
    data object Loading : SaveState
    data object Success : SaveState
    data class Error(val message: String) : SaveState
}

/**
 * Kayıt ekleme/düzenleme ekranının ortak ViewModel'i.
 *
 * Navigation argümanı [editId]:
 *  - -1 (varsayılan) → yeni kayıt modu: form boş, başarıda insert.
 *  - > 0            → düzenleme modu: kayıt yüklenir, başarıda update.
 *
 * [isEditMode] RecordEntryScreen tarafından başlık metnini değiştirmek için kullanılır.
 */
@HiltViewModel
class RecordEntryViewModel @Inject constructor(
    private val repository: HarvestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editId: Long? = savedStateHandle.get<Long>("editId")?.takeIf { it != -1L }

    /** Düzenleme modunda true; ekran bu değerden başlığı belirler. */
    val isEditMode: Boolean = editId != null

    /** Düzenleme sırasında orijinal createdAt korunur. */
    private var originalCreatedAt: Long = System.currentTimeMillis()

    // Form durumu
    private val _formState = MutableStateFlow(RecordFormState())
    val formState: StateFlow<RecordFormState> = _formState.asStateFlow()

    // Kaydet durumu
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    // Autocomplete listeleri — DB'den canlı akış
    val gardenNames: StateFlow<List<String>> = repository.gardenNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sellerNames: StateFlow<List<String>> = repository.sellerNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        editId?.let { loadForEdit(it) }
    }

    // --- Düzenleme yükleyici ---------------------------------------------------------------

    /** Mevcut kaydı veritabanından çekip form alanlarını doldurur. */
    private fun loadForEdit(id: Long) {
        viewModelScope.launch {
            val entity = repository.getHarvestById(id) ?: return@launch
            originalCreatedAt = entity.createdAt
            val source = if (entity.source == "CAYKUR") RecordSource.CAYKUR else RecordSource.OZEL
            _formState.update {
                RecordFormState(
                    source = source,
                    gardenName = entity.gardenName,
                    companyName = entity.companyName,
                    harvestDate = LocalDate.ofEpochDay(entity.harvestDateEpoch),
                    season = entity.season,
                    // Tam sayı ise ".0" gizle
                    weightKg = if (entity.weightKg == entity.weightKg.toInt().toFloat()) {
                        entity.weightKg.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.2f", entity.weightKg)
                    },
                    pricePerKg = String.format(Locale.US, "%.2f", entity.pricePerKg),
                    dueDate = LocalDate.ofEpochDay(entity.dueDateEpoch)
                )
            }
        }
    }

    // --- Form alanı güncelleyiciler -------------------------------------------------------

    fun setSource(source: RecordSource) = _formState.update { current ->
        // Kaynak zaten aynıysa hiçbir şeyi değiştirme — edit modunda loadForEdit'in
        // doldurduğu companyName, pager LaunchedEffect'i tarafından sıfırlanmasın.
        if (current.source == source) return@update current
        current.copy(
            source      = source,
            companyName = if (source == RecordSource.CAYKUR) "ÇAYKUR" else "",
            // ÇAYKUR'a geçince fiyat ve vade otomatik ayarlanır
            pricePerKg  = if (source == RecordSource.CAYKUR) CAYKUR_PRICE_STR else "",
            dueDate     = if (source == RecordSource.CAYKUR) caykurDueDate(current.harvestDate) else current.dueDate
        )
    }

    fun setGardenName(name: String) = _formState.update { it.copy(gardenName = name) }
    fun setCompanyName(name: String) = _formState.update { it.copy(companyName = name) }

    fun setHarvestDate(date: LocalDate) = _formState.update {
        // ÇAYKUR'da hasat tarihi değişince vade de otomatik yeniden hesaplanır
        val newDueDate = if (it.source == RecordSource.CAYKUR) caykurDueDate(date) else it.dueDate
        it.copy(harvestDate = date, dueDate = newDueDate)
    }

    fun setSeason(displayText: String) {
        val number = displayText.firstOrNull()?.digitToIntOrNull() ?: 0
        _formState.update { it.copy(season = number) }
    }

    fun setWeightKg(value: String) = _formState.update { it.copy(weightKg = value) }
    fun setPricePerKg(value: String) = _formState.update { it.copy(pricePerKg = value) }
    fun setDueDate(date: LocalDate) = _formState.update { it.copy(dueDate = date) }

    /** Kaydet işlemi tamamlandıktan sonra durumu sıfırlar. */
    fun consumeSaveState() = _saveState.update { SaveState.Idle }

    // --- Kaydet / Güncelle ----------------------------------------------------------------

    fun save() {
        val s = _formState.value

        // Validasyon
        val error = when {
            s.gardenName.isBlank()  -> "Bahçe adı boş olamaz."
            s.season == 0           -> "Sürüm seçiniz."
            s.weightKg.isBlank()    -> "Miktar giriniz."
            s.weightKg.toFloatOrNull() == null || s.weightKg.toFloat() <= 0 -> "Geçerli bir miktar giriniz."
            // ÇAYKUR fiyatı sabit 35 TL — sadece Özel kayıtlarda fiyat doğrulanır
            s.source == RecordSource.OZEL && s.pricePerKg.isBlank() -> "Fiyat giriniz."
            s.source == RecordSource.OZEL && (s.pricePerKg.toFloatOrNull() == null || s.pricePerKg.toFloat() <= 0) -> "Geçerli bir fiyat giriniz."
            s.source == RecordSource.OZEL && s.companyName.isBlank() -> "Satış yeri boş olamaz."
            else -> null
        }

        if (error != null) {
            _saveState.update { SaveState.Error(error) }
            return
        }

        _saveState.update { SaveState.Loading }

        viewModelScope.launch {
            runCatching {
                // Bahçe adını havuzdaki kanonik yazıma çöz ("merze" → kayıtlı "Merze").
                // Böylece yazım farkları raporlarda aynı bahçeyi ikiye bölmez.
                val canonicalGarden = repository.canonicalGardenName(s.gardenName)

                // Firma adını standartlaştır: nasıl girilirse girilsin Türkçe BÜYÜK HARF sakla.
                val normalizedCompany = s.companyName.trim().uppercase(TR_LOCALE)

                val entity = HarvestEntity(
                    id = editId ?: 0,          // 0 → Room autoGenerate; editId → id'yi korur
                    gardenName = canonicalGarden,
                    harvestDateEpoch = s.harvestDate.toEpochDay(),
                    season = s.season,
                    weightKg = s.weightKg.toFloat(),
                    pricePerKg = s.pricePerKg.toFloat(),
                    source = s.source.name,
                    companyName = normalizedCompany,
                    dueDateEpoch = s.dueDate.toEpochDay(),
                    createdAt = originalCreatedAt   // düzenlemede orijinal zaman damgası korunur
                )
                if (editId != null) {
                    repository.updateHarvest(entity)
                } else {
                    repository.insertHarvest(entity)
                }

                // Autocomplete havuzunu güncelle (kanonik/normalize adlarla — index dublikasyonu önler)
                repository.registerGarden(canonicalGarden)
                if (s.source == RecordSource.OZEL) repository.registerSeller(normalizedCompany)

            }.onSuccess {
                _saveState.update { SaveState.Success }
                // Sadece yeni kayıt modunda form sıfırlanır; düzenleme sonrası geri dönülür.
                if (editId == null) _formState.update { RecordFormState() }
            }.onFailure { e ->
                _saveState.update { SaveState.Error(e.message ?: "Kayıt kaydedilemedi.") }
            }
        }
    }
}
