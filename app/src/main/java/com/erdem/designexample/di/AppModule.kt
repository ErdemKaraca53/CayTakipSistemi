package com.erdem.designexample.di

import android.content.Context
import com.erdem.designexample.database.DatabaseHelper
import com.erdem.designexample.database.DatabaseOperations
import com.erdem.designexample.database.FirebaseSyncHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Uygulama genelinde paylaşılan bağımlılıkların sağlandığı Hilt modülü.
 *
 * Faz 0: Mevcut veri katmanı (SQLite + Firebase) buraya bağlanır; ekran ViewModel'leri
 * ileriki fazlarda bu bağımlılıkları enjekte ederek kullanacak. Repository katmanı
 * Faz 3'te eklenecek ve bu modüle taşınacak.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabaseHelper(@ApplicationContext context: Context): DatabaseHelper =
        DatabaseHelper(context)

    @Provides
    @Singleton
    fun provideDatabaseOperations(): DatabaseOperations = DatabaseOperations()

    @Provides
    @Singleton
    fun provideFirebaseSyncHelper(@ApplicationContext context: Context): FirebaseSyncHelper =
        FirebaseSyncHelper(context)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
