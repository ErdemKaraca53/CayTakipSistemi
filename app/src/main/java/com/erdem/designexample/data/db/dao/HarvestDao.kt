package com.erdem.designexample.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.erdem.designexample.data.db.entity.HarvestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HarvestEntity): Long

    @Update
    suspend fun update(entity: HarvestEntity)

    @Delete
    suspend fun delete(entity: HarvestEntity)

    @Query("DELETE FROM harvests WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Tüm kayıtlar — hasat tarihine göre yeniden eskiye. */
    @Query("SELECT * FROM harvests ORDER BY harvestDateEpoch DESC")
    fun getAll(): Flow<List<HarvestEntity>>

    /** Tek kayıt (düzenleme ekranı için). */
    @Query("SELECT * FROM harvests WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HarvestEntity?

    /**
     * Dinamik filtre: yıl aralığı zorunlu, sürüm ve bahçe opsiyonel.
     * [seasonFilter] = 0 ise tüm sürümler; [gardenFilter] boşsa tüm bahçeler.
     * yearStart / yearEnd = LocalDate(year,1,1).toEpochDay() ve LocalDate(year,12,31).toEpochDay()
     */
    @Query(
        """
        SELECT * FROM harvests
        WHERE harvestDateEpoch BETWEEN :yearStart AND :yearEnd
          AND (:seasonFilter = 0 OR season = :seasonFilter)
          AND (:gardenFilter = '' OR gardenName = :gardenFilter COLLATE NOCASE)
        ORDER BY harvestDateEpoch DESC
        """
    )
    fun getFiltered(
        yearStart: Long,
        yearEnd: Long,
        seasonFilter: Int = 0,
        gardenFilter: String = ""
    ): Flow<List<HarvestEntity>>

    /** Toplam kayıt sayısı. */
    @Query("SELECT COUNT(*) FROM harvests")
    fun getCount(): Flow<Int>

    /**
     * Vade tarihi [startEpoch] – [endEpoch] (epoch-day) arasında olan kayıtlar.
     * Ödeme hatırlatma worker'ı tarafından tek seferlik (suspend) okunur.
     */
    @Query("SELECT * FROM harvests WHERE dueDateEpoch BETWEEN :startEpoch AND :endEpoch ORDER BY dueDateEpoch ASC")
    suspend fun getDueBetween(startEpoch: Long, endEpoch: Long): List<HarvestEntity>
}
