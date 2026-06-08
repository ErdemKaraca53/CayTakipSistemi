package com.erdem.designexample.ui.settings

import androidx.lifecycle.ViewModel
import com.erdem.designexample.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Ayarlar ekranının ViewModel'i. Mevcut hesap bilgisini sağlar ve oturum kapatma
 * işlemini yürütür. Çıkıştan sonra navigasyon (geri yığını temizleme) ekran/NavHost
 * tarafından yapılır.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Oturum açmış kullanıcının e-postası (yoksa null). */
    val userEmail: String? = authRepository.currentUser?.email

    /** Oturumu kapatır. */
    fun logout() = authRepository.signOut()
}
