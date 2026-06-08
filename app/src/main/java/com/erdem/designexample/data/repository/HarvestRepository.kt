package com.erdem.designexample.data.repository

import com.erdem.designexample.data.db.dao.GardenDao
import com.erdem.designexample.data.db.dao.HarvestDao
import com.erdem.designexample.data.db.dao.SellerDao
import com.erdem.designexample.data.db.entity.GardenEntity
import com.erdem.designexample.data.db.entity.HarvestEntity
import com.erdem.designexample.data.db.entity.SellerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hasat kayıtları, bahçe ve satış yeri öneri havuzları için tek erişim noktası.
 * ViewModel'ler yalnızca bu sınıfla konuşur; DAO'lara doğrudan erişmez.
 */
@Singleton
class HarvestRepository @Inject constructor(
    private val harvestDao: HarvestDao,
    private val gardenDao: GardenDao,
    private val sellerDao: SellerDao
) {

    // --- Hasat kayıtları -----------------------------------------------------------------

    /** Tüm kayıtlar akışı (yeniden eskiye). */
    val allHarvests: Flow<List<HarvestEntity>> = harvestDao.getAll()

    /** Yeni kayıt ekler. Eklenen satırın id'sini döndürür. */
    suspend fun insertHarvest(entity: HarvestEntity): Long = harvestDao.insert(entity)

    /** Mevcut kaydı günceller. */
    suspend fun updateHarvest(entity: HarvestEntity) = harvestDao.update(entity)

    /** Kaydı id ile siler. */
    suspend fun deleteHarvest(id: Long) = harvestDao.deleteById(id)

    /** Tek kayıt (düzenleme için). */
    suspend fun getHarvestById(id: Long): HarvestEntity? = harvestDao.getById(id)

    /**
     * Filtrelenmiş kayıtlar.
     * [year] = null  → **tüm yıllar** (DAO'ya Long.MIN/MAX aralığı geçilir).
     * [season] = null → tüm sürümler.
     * [garden] = null → tüm bahçeler.
     */
    fun getFiltered(year: Int?, season: Int?, garden: String?): Flow<List<HarvestEntity>> {
        val (start, end) = if (year != null) {
            LocalDate.of(year, 1, 1).toEpochDay() to LocalDate.of(year, 12, 31).toEpochDay()
        } else {
            Long.MIN_VALUE to Long.MAX_VALUE   // tüm zaman dilimi
        }
        return harvestDao.getFiltered(
            yearStart = start,
            yearEnd = end,
            seasonFilter = season ?: 0,
            gardenFilter = garden ?: ""
        )
    }

    // --- Autocomplete havuzları ----------------------------------------------------------

    /** Tüm bahçe adları (alfabetik). */
    val gardenNames: Flow<List<String>> = gardenDao.getAll().map { list -> list.map { it.name } }

    /**
     * Bahçe adını havuzdaki **kanonik yazıma** çözer: aynı ad büyük/küçük harf farkıyla
     * zaten varsa onun mevcut yazımını döndürür ("merze" → kayıtlı "Merze"). Yoksa girilen
     * (trimlenmiş) adı döndürür. Kayıt kaydederken çağrılır; raporlarda "Merze"/"merze"
     * gibi yazım farklarının aynı bahçeyi ikiye bölmesini önler.
     */
    suspend fun canonicalGardenName(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return trimmed
        return gardenDao.findCanonicalName(trimmed) ?: trimmed
    }

    /** Tüm satış yeri / firma adları (alfabetik). */
    val sellerNames: Flow<List<String>> = sellerDao.getAll().map { list -> list.map { it.name } }

    /**
     * Bahçe adını öneri havuzuna ekler. Zaten varsa sessizce atlar (DAO IGNORE stratejisi).
     * Kaydet butonuna basıldığında çağrılır.
     */
    suspend fun registerGarden(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) gardenDao.insertIfAbsent(GardenEntity(name = trimmed))
    }

    /** Satış yeri adını öneri havuzuna ekler. */
    suspend fun registerSeller(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) sellerDao.insertIfAbsent(SellerEntity(name = trimmed))
    }
}
