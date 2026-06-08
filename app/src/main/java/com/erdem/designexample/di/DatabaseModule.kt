package com.erdem.designexample.di

import android.content.Context
import androidx.room.Room
import com.erdem.designexample.data.db.AppDatabase
import com.erdem.designexample.data.db.dao.GardenDao
import com.erdem.designexample.data.db.dao.HarvestDao
import com.erdem.designexample.data.db.dao.SellerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "tea_diary.db")
            .fallbackToDestructiveMigration() // geliştirme fazında; prodda Migration eklenecek
            .build()

    @Provides
    fun provideHarvestDao(db: AppDatabase): HarvestDao = db.harvestDao()

    @Provides
    fun provideGardenDao(db: AppDatabase): GardenDao = db.gardenDao()

    @Provides
    fun provideSellerDao(db: AppDatabase): SellerDao = db.sellerDao()
}
