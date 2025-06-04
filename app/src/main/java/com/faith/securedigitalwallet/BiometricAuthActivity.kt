package com.faith.securedigitalwallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.faith.securedigitalwallet.data.PasswordManager
import com.faith.securedigitalwallet.ui.PasswordPromptDialog
import kotlinx.coroutines.launch

class BiometricAuthActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    lifecycleScope.launch {
                        val masterPassword = PasswordManager.getMasterPassword(applicationContext)
                        if (masterPassword.isNullOrEmpty()) {
                            // Master password not set yet – show dialog to set it
                            setContent {
                                PasswordPromptDialog(
                                    isSettingPassword = true,
                                    onDismiss = {
                                        finish() // Exit app if user cancels password setup
                                    },
                                    onPasswordEntered = { password ->
                                        lifecycleScope.launch {
                                            PasswordManager.saveMasterPassword(applicationContext, password)
                                            startActivity(Intent(this@BiometricAuthActivity, MainActivity::class.java))
                                            finish()
                                        }
                                    }
                                )
                            }
                        } else {
                            // Master password is already set – proceed to main
                            startActivity(Intent(this@BiometricAuthActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish() // Exit on error
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Optional: show toast or retry logic
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("SecureDigitalWallet")
            .setSubtitle("Authenticate with screen lock or biometrics")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
