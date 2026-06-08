package com.erdem.designexample.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erdem.designexample.data.db.entity.SellerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SellerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: SellerEntity)

    @Query("SELECT * FROM sellers ORDER BY name ASC")
    fun getAll(): Flow<List<SellerEntity>>

    @Query("SELECT * FROM sellers WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<SellerEntity>>
}
