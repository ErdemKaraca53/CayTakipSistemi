package com.erdem.designexample.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Özel satış yeri / firma adları (autocomplete öneri havuzu).
 * [name] unique index ile korunur.
 */
@Entity(tableName = "sellers", indices = [Index(value = ["name"], unique = true)])
data class SellerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
