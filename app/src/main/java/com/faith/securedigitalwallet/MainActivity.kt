package com.faith.securedigitalwallet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.faith.securedigitalwallet.data.AppDatabase
import com.faith.securedigitalwallet.ui.*
import com.faith.securedigitalwallet.ui.theme.SecureBankAppTheme
import com.faith.securedigitalwallet.util.GitHubUpdateHelper

class MainActivity : AppCompatActivity() {

    private var screen by mutableStateOf<Screen>(Screen.Start)
    private var updateCheckCompleted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots for security
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "securebank-db"
        ).build()

        setContent {
            SecureBankAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!updateCheckCompleted) {
                        // Check for update first
                        LaunchedEffect(Unit) {
                            // Request storage permission on older Android versions
                            requestStoragePermissionIfNeeded()

                            if (canInstallUnknownApps()) {
                                GitHubUpdateHelper.checkForUpdate(
                                    context = this@MainActivity,
                                    onUpdateComplete = { updateCheckCompleted = true },
                                    onUpdateFailed = {
                                        updateCheckCompleted = true
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Update check failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            } else {
                                requestInstallPermission()
                                updateCheckCompleted = true
                            }
                        }

                        // Show a loading indicator while checking updates
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // After update check complete, show normal app screens
                        when (screen) {
                            Screen.Start -> StartScreen(onNavigate = { screen = it })
                            Screen.BankProfiles -> MainScreen(db.userDao(), db.bankProfileDao())
                            Screen.WebLoginProfiles -> Text("Web Login Profiles Screen (Coming soon!)")
                            Screen.LicProfiles -> Text("LIC Profiles Screen (Coming soon!)")
                            Screen.ResetPassword -> ResetPasswordScreen(onBack = { screen = Screen.Start })
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (screen != Screen.Start) {
            screen = Screen.Start
        } else {
            super.onBackPressed()
        }
    }

    private suspend fun requestStoragePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Request permission and suspend until result
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
                // Note: Ideally, handle the callback onRequestPermissionsResult to know when granted
                // For Compose, you can consider Accompanist Permissions library or similar
                // Here, just a simple request, no waiting implemented
            }
        }
    }

    private fun canInstallUnknownApps(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            packageManager.canRequestPackageInstalls()
        } else true
    }

    private fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
            Toast.makeText(
                this,
                "Please allow installing unknown apps to enable updates.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
