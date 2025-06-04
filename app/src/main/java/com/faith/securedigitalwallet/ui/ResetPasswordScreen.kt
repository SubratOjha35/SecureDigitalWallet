package com.faith.securedigitalwallet.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.faith.securedigitalwallet.data.PasswordManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var showPasswordPrompt by remember { mutableStateOf(false) }
    var isBiometricChecked by remember { mutableStateOf(false) } // to control UI

    val biometricPrompt = remember {
        val executor = ContextCompat.getMainExecutor(context)
        BiometricPrompt(context as androidx.fragment.app.FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    showPasswordPrompt = true
                    isBiometricChecked = true
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            // User cancelled: navigate back immediately
                            onBack()
                        }
                        else -> {
                            // Other errors: fallback to password prompt
                            showPasswordPrompt = true
                            isBiometricChecked = true
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    // Optional: Show message or retry behavior
                }
            })
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate to reset password")
            .setSubtitle("Use your device screen lock or biometric")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    LaunchedEffect(Unit) {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            // No biometric: fallback to password prompt directly
            showPasswordPrompt = true
            isBiometricChecked = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Master Password") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                showPasswordPrompt -> {
                    PasswordPromptDialog(
                        isSettingPassword = true,
                        onDismiss = {
                            showPasswordPrompt = false
                            onBack()
                        },
                        onPasswordEntered = { newPassword ->
                            CoroutineScope(Dispatchers.IO).launch {
                                PasswordManager.saveMasterPassword(context, newPassword)
                            }
                            showPasswordPrompt = false
                            onBack()
                        }
                    )
                }

                !isBiometricChecked -> {
                    Text("Authenticating...")
                }
            }
        }
    }
}
