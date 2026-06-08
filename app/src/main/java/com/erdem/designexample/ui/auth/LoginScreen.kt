package com.erdem.designexample.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erdem.designexample.ui.components.AppButton
import com.erdem.designexample.ui.components.AppPasswordField
import com.erdem.designexample.ui.components.AppTextField
import com.erdem.designexample.ui.theme.DesignExampleTheme

/**
 * Giriş ekranı. Navigasyon ve Firestore senkronizasyonu [onLoggedIn] içinde (Fragment'ta) yapılır.
 */
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onLoggedIn()
            viewModel.consumeSuccess()
        }
    }

    LoginContent(
        email = email,
        password = password,
        isLoading = uiState.isLoading,
        snackbarHostState = snackbarHostState,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onSubmit = { viewModel.login(email, password) },
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    email: String,
    password: String,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandHeader(
                title = "Tekrar hoş geldiniz",
                subtitle = "Hasat ve ödemelerinizi takip etmek için giriş yapın"
            )

            Spacer(Modifier.height(40.dp))

            AppTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "E-posta",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                enabled = !isLoading
            )

            Spacer(Modifier.height(16.dp))

            AppPasswordField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Şifre",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.Lock,
                imeAction = ImeAction.Done,
                enabled = !isLoading
            )

            Spacer(Modifier.height(28.dp))

            AppButton(
                text = "Giriş Yap",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                loading = isLoading
            )
        }
    }
}

/** Auth ekranlarında ortak marka başlığı (çay yaprağı ikonu + başlık + alt başlık). */
@Composable
internal fun BrandHeader(title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(80.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalCafe,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }
    }
    Spacer(Modifier.height(24.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    DesignExampleTheme {
        LoginContent(
            email = "ornek@eposta.com",
            password = "123456",
            isLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {}
        )
    }
}
