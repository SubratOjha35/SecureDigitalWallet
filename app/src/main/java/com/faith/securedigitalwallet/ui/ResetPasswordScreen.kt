package com.faith.securedigitalwallet.ui

import android.content.Context
import androidx.activity.ComponentActivity
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
import androidx.fragment.app.FragmentActivity
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
    val activity = context as? FragmentActivity

    var showPasswordPrompt by remember { mutableStateOf(false) }
    var isBiometricChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (activity == null) {
            // fallback immediately if activity is not valid
            showPasswordPrompt = true
            isBiometricChecked = true
            return@LaunchedEffect
        }

        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(context)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate to reset password")
                .setSubtitle("Use your device screen lock or biometric")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        showPasswordPrompt = true
                        isBiometricChecked = true
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onBack()
                            else -> {
                                showPasswordPrompt = true
                                isBiometricChecked = true
                            }
                        }
                    }

                    override fun onAuthenticationFailed() {
                        // Optional: show retry option
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        } else {
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
