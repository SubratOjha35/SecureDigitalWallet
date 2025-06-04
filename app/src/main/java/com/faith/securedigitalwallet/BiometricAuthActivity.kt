package com.faith.securedigitalwallet

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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

    companion object {
        private const val REQUEST_CODE_PIN_AUTH = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isBiometricAvailable()) {
            showBiometricPrompt()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                launchPinAuthentication()
            } else {
                Toast.makeText(this, "No authentication method available", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            else
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    proceedAfterAuth()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        launchPinAuthentication()
                    } else {
                        Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("SecureDigitalWallet")
                .setSubtitle("Use biometrics or PIN/password")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        } else {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("SecureDigitalWallet")
                .setSubtitle("Use fingerprint")
                .setNegativeButtonText("Use PIN/password")
                .build()
        }

        biometricPrompt.authenticate(promptInfo)
    }

    private fun launchPinAuthentication() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
            "SecureDigitalWallet",
            "Confirm your screen lock"
        )
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_PIN_AUTH)
        } else {
            Toast.makeText(this, "Device credential not available", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PIN_AUTH) {
            if (resultCode == RESULT_OK) {
                proceedAfterAuth()
            } else {
                Toast.makeText(this, "PIN authentication failed", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun proceedAfterAuth() {
        lifecycleScope.launch {
            val masterPassword = PasswordManager.getMasterPassword(applicationContext)
            if (masterPassword.isNullOrEmpty()) {
                setContent {
                    PasswordPromptDialog(
                        isSettingPassword = true,
                        onDismiss = { finish() },
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
                startActivity(Intent(this@BiometricAuthActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
