package com.erdem.designexample.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erdem.designexample.data.db.entity.GardenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenDao {

    /**
     * Yeni bahçe adı ekler; aynı isim zaten varsa sessizce atlar (IGNORE).
     * Kaydet'e basıldığında çağrılır.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: GardenEntity)

    /**
     * Verilen ada büyük/küçük harf duyarsız eşleşen mevcut (kanonik) bahçe adını döndürür.
     * Yoksa null. Kayıt kaydedilirken tutarlı yazımı yakalamak için kullanılır.
     */
    @Query("SELECT name FROM gardens WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findCanonicalName(name: String): String?

    /** Tüm bahçe adları — alfabetik, autocomplete için. */
    @Query("SELECT * FROM gardens ORDER BY name ASC")
    fun getAll(): Flow<List<GardenEntity>>

    /** Adına göre arama (autocomplete önerileri). */
    @Query("SELECT * FROM gardens WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<GardenEntity>>
}
