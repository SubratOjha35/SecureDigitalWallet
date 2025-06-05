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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.faith.securedigitalwallet.data.AppDatabase
import com.faith.securedigitalwallet.ui.*
import com.faith.securedigitalwallet.ui.theme.SecureBankAppTheme
import com.faith.securedigitalwallet.util.GitHubHelper

class MainActivity : AppCompatActivity() {

    private var screen by mutableStateOf<Screen>(Screen.Start)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots / screen recording
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

                    // Initial setup for permissions and updates
                    LaunchedEffect(Unit) {
                        ensureStoragePermission()
                        if (canInstallUnknownApps()) {
                            GitHubHelper.checkForUpdate(this@MainActivity)
                        } else {
                            requestInstallPermission()
                        }
                    }

                    // App navigation
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

    override fun onBackPressed() {
        if (screen != Screen.Start) {
            screen = Screen.Start
        } else {
            super.onBackPressed()
        }
    }

    private fun ensureStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            }
        }
    }

    private fun canInstallUnknownApps(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    private fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
            Toast.makeText(
                this,
                "Please allow installing unknown apps to enable update feature.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (canInstallUnknownApps()) {
                    GitHubHelper.checkForUpdate(this)
                }
            } else {
                Toast.makeText(
                    this,
                    "Storage permission is required to download app updates.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
