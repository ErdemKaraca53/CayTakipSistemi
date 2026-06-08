package com.erdem.designexample.design.log_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.erdem.designexample.R
import com.erdem.designexample.database.FirebaseSyncHelper
import com.erdem.designexample.ui.auth.SignUpScreen
import com.erdem.designexample.ui.theme.DesignExampleTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Kayıt ekranı host'u (hibrit mod) ve uygulamanın başlangıç hedefi.
 *
 * UI tamamen Jetpack Compose ([SignUpScreen]); kayıt + doğrulama AuthViewModel'de yapılır.
 * Navigasyon ve Firestore yedekleme — eski davranışı korumak için — burada gerçekleştirilir.
 */
@AndroidEntryPoint
class SignUp : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Navigation bar kayıt ekranında gerekli değil.
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
            .visibility = View.GONE

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DesignExampleTheme {
                    SignUpScreen(
                        onSignedUp = {
                            findNavController().navigate(R.id.action_signUp2_to_new_design)
                        },
                        onNavigateToLogin = {
                            findNavController().navigate(R.id.action_signUp2_to_login)
                        }
                    )
                }
            }
        }
    }

    /**
     * Zaten giriş yapmış bir kullanıcı varsa, SQLite Firestore'a yedeklenir ve doğrudan
     * ana ekrana yönlendirilir. (Eski SignUp.onStart davranışıyla aynı.)
     */
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            Toast.makeText(requireContext(), "Sayfa yenilendi sign-up!", Toast.LENGTH_SHORT).show()
            // Not: userId constructor'da yakalandığı için taze instance gerekiyor.
            val syncHelper = FirebaseSyncHelper(requireContext())
            CoroutineScope(Dispatchers.IO).launch {
                syncHelper.backupSQLiteToFirestore()
            }
            findNavController().navigate(R.id.action_signUp2_to_new_design)
        }
    }
}
