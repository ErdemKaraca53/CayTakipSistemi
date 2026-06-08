package com.erdem.designexample.design.log_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.erdem.designexample.R
import com.erdem.designexample.database.FirebaseSyncHelper
import com.erdem.designexample.ui.auth.LoginScreen
import com.erdem.designexample.ui.theme.DesignExampleTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Giriş ekranı host'u (hibrit mod).
 *
 * UI tamamen Jetpack Compose ([LoginScreen]); doğrulama ve Firebase auth çağrısı
 * AuthViewModel'de yapılır. Navigasyon ve Firestore senkronizasyonu — eski davranışı
 * birebir korumak için — burada, mevcut XML nav graph üzerinden gerçekleştirilir.
 */
@AndroidEntryPoint
class Login : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Navigation bar giriş ekranında gerekli değil.
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
            .visibility = View.GONE

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DesignExampleTheme {
                    LoginScreen(onLoggedIn = ::onLoggedIn)
                }
            }
        }
    }

    /**
     * Giriş başarılı olduğunda: SQLite, Firestore ile arka planda senkronize edilir ve
     * kullanıcı ana ekrana yönlendirilir. (Eski Login davranışıyla aynı.)
     */
    private fun onLoggedIn() {
        // Not: userId constructor'da yakalandığı için taze instance gerekiyor (login sonrası).
        val syncHelper = FirebaseSyncHelper(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            syncHelper.syncFirestoreToSQLite()
        }
        findNavController().navigate(R.id.action_login_to_new_design)
    }
}
