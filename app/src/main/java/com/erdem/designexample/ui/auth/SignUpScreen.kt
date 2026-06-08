package com.erdem.designexample.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 * Kayıt ekranı. Navigasyon [onSignedUp]/[onNavigateToLogin] içinde (Fragment'ta) yapılır.
 */
@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onNavigateToLogin: () -> Unit,
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
            onSignedUp()
            viewModel.consumeSuccess()
        }
    }

    SignUpContent(
        email = email,
        password = password,
        isLoading = uiState.isLoading,
        snackbarHostState = snackbarHostState,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onSubmit = { viewModel.signUp(email, password) },
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier
    )
}

@Composable
private fun SignUpContent(
    email: String,
    password: String,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit,
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
                title = "Hesap oluştur",
                subtitle = "Çay bahçesi hasatlarınızı yönetmeye başlayın"
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
                text = "Kayıt Ol",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                loading = isLoading
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Zaten hesabın var mı?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
                    Text("Giriş Yap")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpContentPreview() {
    DesignExampleTheme {
        SignUpContent(
            email = "",
            password = "",
            isLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onNavigateToLogin = {}
        )
    }
}
