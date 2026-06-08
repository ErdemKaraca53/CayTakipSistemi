package com.erdem.designexample.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdem.designexample.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Auth ekranlarının (Login + SignUp) durumunu yöneten ViewModel.
 *
 * Navigasyon ve Firestore senkronizasyonu bilinçli olarak BURADA yapılmaz; bunlar
 * hibrit dönemde Fragment tarafında (eski nav graph üzerinden) kalır. ViewModel yalnızca
 * doğrulama + Firebase auth çağrısı + UI durumunu yönetir. Mesajlar eski koddan birebir korunur.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Giriş akışı. Başarılı olursa [AuthUiState.success] true olur. */
    fun login(email: String, password: String) {
        val mail = email.trim()
        val pass = password.trim()

        if (!isValidEmail(mail)) {
            showMessage("Geçerli bir e-posta giriniz!")
            return
        }
        if (!isValidPassword(pass)) {
            showMessage("Şifre en az 6 karakter olmalıdır!")
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.login(mail, pass)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, message = "Giriş başarılı!", success = true)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Giriş başarısız! Bilgilerinizi kontrol edin."
                        )
                    }
                }
        }
    }

    /** Kayıt akışı. Başarılı olursa doğrulama e-postası gönderilir ve [AuthUiState.success] true olur. */
    fun signUp(email: String, password: String) {
        val mail = email.trim()
        val pass = password.trim()

        if (!isValidEmail(mail)) {
            showMessage("Geçerli bir e-posta adresi giriniz!")
            return
        }
        if (!isValidPassword(pass)) {
            showMessage("Şifre en az 6 karakter olmalıdır!")
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.signUp(mail, pass)
                .onSuccess { user ->
                    repository.sendEmailVerification() // sonucu bilgilendirme amaçlı, akışı bloklamaz
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Hoş geldiniz: ${user.email}",
                            success = true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, message = mapSignUpError(e)) }
                }
        }
    }

    /** Snackbar mesajı gösterildikten sonra çağrılır. */
    fun consumeMessage() = _uiState.update { it.copy(message = null) }

    /** Başarılı navigasyon tetiklendikten sonra çağrılır. */
    fun consumeSuccess() = _uiState.update { it.copy(success = false) }

    private fun showMessage(text: String) = _uiState.update { it.copy(message = text) }

    private fun mapSignUpError(e: Throwable): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Bu e-posta zaten kayıtlı!"
        is FirebaseAuthWeakPasswordException -> "Şifre çok zayıf! Daha güçlü bir şifre deneyin."
        is FirebaseAuthInvalidCredentialsException -> "Geçersiz e-posta adresi!"
        else -> "Kayıt işlemi başarısız!"
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidPassword(password: String): Boolean = password.length >= 6
}

/** Login ve SignUp ekranlarının ortak UI durumu. */
data class AuthUiState(
    val isLoading: Boolean = false,
    val message: String? = null,   // Snackbar ile gösterilecek tek seferlik mesaj
    val success: Boolean = false   // true → Fragment navigasyonu tetikler
)
