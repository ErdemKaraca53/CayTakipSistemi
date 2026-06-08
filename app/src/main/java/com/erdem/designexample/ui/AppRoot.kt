package com.erdem.designexample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.erdem.designexample.ui.navigation.AppNavHost
import com.erdem.designexample.ui.navigation.Routes
import com.erdem.designexample.ui.theme.AppPalette
import com.google.firebase.auth.FirebaseAuth

/**
 * Uygulamanın Compose kök bileşeni. Alt çubuk yoktur; gezinme ana ekrandan (Dashboard) yapılır.
 * Başlangıç hedefi oturum durumuna göre belirlenir (eski SignUp.onStart davranışı).
 */
@Composable
fun AppRoot() {
    val navController = rememberNavController()

    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) Routes.DASHBOARD
        else Routes.SIGNUP
    }

    AppNavHost(
        navController    = navController,
        startDestination = startDestination,
        // Animasyon sırasında Compose katmanının altında da koyu navy — çift güvence.
        modifier = Modifier
            .fillMaxSize()
            .background(AppPalette.Bg)
    )
}
