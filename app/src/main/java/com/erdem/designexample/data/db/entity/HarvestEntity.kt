package com.erdem.designexample.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Veritabanındaki tek bir hasat kaydı.
 * Tarihler epoch-day (Long) olarak tutulur; LocalDate.toEpochDay() / LocalDate.ofEpochDay() ile dönüştürülür.
 * [source] = "CAYKUR" veya "OZEL" — hangi formdan girildiğini belirtir.
 */
@Entity(tableName = "harvests")
data class HarvestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gardenName: String,
    val harvestDateEpoch: Long,   // LocalDate.toEpochDay()
    val season: Int,              // 1 – 4
    val weightKg: Float,
    val pricePerKg: Float,
    val source: String,           // "CAYKUR" | "OZEL"
    val companyName: String,      // ÇAYKUR veya özel firma adı
    val dueDateEpoch: Long,       // LocalDate.toEpochDay()
    val createdAt: Long = System.currentTimeMillis()
)
