package com.erdem.designexample.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.erdem.designexample.ui.auth.LoginScreen
import com.erdem.designexample.ui.auth.SignUpScreen
import com.erdem.designexample.ui.dashboard.DashboardScreen
import com.erdem.designexample.ui.home.RecordEntryScreen
import com.erdem.designexample.ui.payments.PaymentsScreen
import com.erdem.designexample.ui.records.RecordsScreen
import com.erdem.designexample.ui.reports.ReportsScreen
import com.erdem.designexample.ui.settings.SettingsScreen

// --- Animasyon sabitleri -------------------------------------------------------------------

private const val ANIM_DURATION     = 900   // ileri geçiş (ms)
private const val POP_DURATION      = 600   // geri dönüş (ms) — hızlı ve anlık hissi
private const val SLIDE_OFFSET      = 0.25f

private fun slideSpec(ms: Int) = tween<IntOffset>(ms, easing = FastOutSlowInEasing)
private fun fadeSpec(ms: Int)  = tween<Float>(ms,    easing = FastOutSlowInEasing)

/** İleri geçiş: yeni ekran sağdan kayarak girer. */
private val enterExpand = slideInHorizontally(
    initialOffsetX = { (it * SLIDE_OFFSET).toInt() },
    animationSpec  = slideSpec(ANIM_DURATION)
) + fadeIn(animationSpec = fadeSpec(ANIM_DURATION))

/** İleri çıkış: mevcut ekran sola doğru kayarak çıkar. */
private val exitShrink = slideOutHorizontally(
    targetOffsetX = { -(it * SLIDE_OFFSET).toInt() },
    animationSpec = slideSpec(ANIM_DURATION)
) + fadeOut(animationSpec = fadeSpec(ANIM_DURATION / 2))

/** Geri geçiş: önceki ekran soldan hızlıca girer. */
private val popEnterExpand = slideInHorizontally(
    initialOffsetX = { -(it * SLIDE_OFFSET).toInt() },
    animationSpec  = slideSpec(POP_DURATION)
) + fadeIn(animationSpec = fadeSpec(POP_DURATION))

/** Geri çıkış: aktif ekran sağa doğru hızlıca çıkar. */
private val popExitShrink = slideOutHorizontally(
    targetOffsetX = { (it * SLIDE_OFFSET).toInt() },
    animationSpec = slideSpec(POP_DURATION)
) + fadeOut(animationSpec = fadeSpec(POP_DURATION / 2))

// --- NavHost ---------------------------------------------------------------------------------

/**
 * Uygulamanın Compose navigasyon grafiği.
 *
 * Tüm ekranlar varsayılan olarak yatay kayma + soluklaşma animasyonu alır.
 * İleri gidişte sağdan girer / sola çıkar; geri dönüşte soldan girer / sağa çıkar.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    fun goToDashboard() {
        navController.navigate(Routes.DASHBOARD) {
            popUpTo(Routes.SIGNUP) { inclusive = true }
            launchSingleTop = true
        }
    }

    val onBack: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController       = navController,
        startDestination    = startDestination,
        modifier            = modifier,
        enterTransition     = { enterExpand },
        exitTransition      = { exitShrink },
        popEnterTransition  = { popEnterExpand },
        popExitTransition   = { popExitShrink }
    ) {
        // --- Auth akışı ---
        composable(Routes.SIGNUP) {
            SignUpScreen(
                onSignedUp        = { goToDashboard() },
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                // Bulut → Room geri yüklemesi Dashboard açılışında yapılır (DashboardViewModel).
                onLoggedIn = { goToDashboard() }
            )
        }

        // --- Ana ekran ve bölümleri ---
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = onBack,
                onLoggedOut = {
                    // Çıkışta tüm geri yığınını temizleyip giriş/kayıt ekranına dön.
                    navController.navigate(Routes.SIGNUP) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // RecordEntry: hem yeni kayıt hem düzenleme — editId opsiyonel (-1 = yeni).
        composable(
            route     = "${Routes.RECORD_ENTRY}?editId={editId}",
            arguments = listOf(
                navArgument("editId") {
                    type         = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            RecordEntryScreen(onBack = onBack)
        }

        composable(Routes.RECORDS) {
            RecordsScreen(
                onBack           = onBack,
                onNavigateToEdit = { id -> navController.navigate(Routes.recordEntryEdit(id)) }
            )
        }

        composable(Routes.PAYMENTS) { PaymentsScreen(onBack = onBack) }
        composable(Routes.REPORTS)  { ReportsScreen(onBack = onBack) }
    }
}
