package com.erdem.designexample.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erdem.designexample.BuildConfig
import com.erdem.designexample.ui.components.AppButton
import com.erdem.designexample.ui.theme.AppPalette
import com.erdem.designexample.ui.theme.LightStatusBarIcons

/**
 * Ayarlar ekranı. Hesap bilgisini gösterir ve oturum kapatma imkânı sunar.
 * Çıkış onaylanınca [onLoggedOut] çağrılır; geri yığınını temizleyip giriş ekranına
 * dönmek NavHost'un sorumluluğundadır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    LightStatusBarIcons()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppPalette.Bg,
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppPalette.Bg,
                    titleContentColor = AppPalette.TextPrimary,
                    navigationIconContentColor = AppPalette.TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Hesap bilgisi kartı
            AccountCard(email = vm.userEmail)

            Spacer(Modifier.height(24.dp))

            // Oturumu kapat
            AppButton(
                text = "Oturumu Kapat",
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                containerColor = AppPalette.Card,
                contentColor = AppPalette.Amber
            )

            Spacer(Modifier.weight(1f))

            // Sürüm bilgisi
            Text(
                text = "Sürüm ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = AppPalette.TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = AppPalette.Card,
            titleContentColor = AppPalette.TextPrimary,
            textContentColor = AppPalette.TextSecondary,
            icon = {
                Icon(
                    Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = AppPalette.Amber
                )
            },
            title = { Text("Oturumu kapat", fontWeight = FontWeight.SemiBold) },
            text = { Text("Hesabından çıkış yapmak istediğine emin misin?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    vm.logout()
                    onLoggedOut()
                }) {
                    Text("Çıkış yap", color = AppPalette.Amber, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Vazgeç", color = AppPalette.TextSecondary)
                }
            }
        )
    }
}

/** Oturum açmış kullanıcının e-postasını gösteren kart. */
@Composable
private fun AccountCard(email: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppPalette.Card, RoundedCornerShape(16.dp))
            .border(1.dp, AppPalette.Border, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(AppPalette.Teal.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = AppPalette.Teal)
        }
        Spacer(Modifier.size(16.dp))
        Column {
            Text(
                text = "Hesap",
                style = MaterialTheme.typography.labelMedium,
                color = AppPalette.TextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = email ?: "Bilinmiyor",
                style = MaterialTheme.typography.titleMedium,
                color = AppPalette.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1621)
@Composable
private fun SettingsScreenPreview() {
    AccountCard(email = "ornek@cay.com")
}
