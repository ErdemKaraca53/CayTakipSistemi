package com.erdem.designexample.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Daha önce kullanılmış bahçe adları (autocomplete öneri havuzu).
 * [name] unique index ile korunur; aynı isim iki kez eklenemez.
 *
 * Collation = NOCASE: benzersizlik büyük/küçük harf duyarsızdır; "Merze" ve "merze"
 * aynı sayılır, havuzda tek varyant tutulur (raporlarda bahçe bölünmesini önler).
 */
@Entity(tableName = "gardens", indices = [Index(value = ["name"], unique = true)])
data class GardenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String
)
