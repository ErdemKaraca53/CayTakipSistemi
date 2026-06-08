package com.erdem.designexample.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.erdem.designexample.data.db.dao.GardenDao
import com.erdem.designexample.data.db.dao.HarvestDao
import com.erdem.designexample.data.db.dao.SellerDao
import com.erdem.designexample.data.db.entity.GardenEntity
import com.erdem.designexample.data.db.entity.HarvestEntity
import com.erdem.designexample.data.db.entity.SellerEntity

/**
 * Uygulamanın tek Room veritabanı.
 * Versiyon arttırılırsa Migration eklenmelidir; geliştirme sürecinde fallbackToDestructiveMigration
 * kullanılabilir (DatabaseModule içinde).
 */
@Database(
    entities = [HarvestEntity::class, GardenEntity::class, SellerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun harvestDao(): HarvestDao
    abstract fun gardenDao(): GardenDao
    abstract fun sellerDao(): SellerDao
}
