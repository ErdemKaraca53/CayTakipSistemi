package com.erdem.designexample.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Authentication işlemlerini suspend fonksiyonlarla saran katman.
 *
 * Eski Fragment'lardaki `addOnCompleteListener` tabanlı callback akışı, ViewModel'den
 * çağrılabilmesi için coroutine `await()` ile sarmalanmıştır. Hata türleri çağırana
 * (ViewModel) `Result.failure` içinde aynen iletilir; mesaj eşlemesi orada yapılır.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /** E-posta/şifre ile giriş yapar. */
    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user ?: error("Kullanıcı bulunamadı")
    }

    /** Yeni hesap oluşturur. */
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user ?: error("Kullanıcı oluşturulamadı")
    }

    /** Mevcut kullanıcıya e-posta doğrulama bağlantısı gönderir. */
    suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Oturum açmış kullanıcı yok")
        user.sendEmailVerification().await()
    }

    /** Oturumu kapatır. */
    fun signOut() = auth.signOut()
}
