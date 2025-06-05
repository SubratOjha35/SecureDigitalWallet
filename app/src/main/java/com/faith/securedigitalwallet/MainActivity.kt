package com.faith.securedigitalwallet

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.faith.securedigitalwallet.data.AppDatabase
import com.faith.securedigitalwallet.ui.*
import com.faith.securedigitalwallet.ui.theme.SecureBankAppTheme
import com.faith.securedigitalwallet.util.GitHubUpdateHelper

class MainActivity : AppCompatActivity() {
    private var screen by mutableStateOf<Screen>(Screen.Start)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots / screen recording
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        ensureStoragePermission()
        GitHubUpdateHelper.checkForUpdate(this)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "securebank-db"
        ).build()

        setContent {
            SecureBankAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (screen) {
                        Screen.Start -> StartScreen(onNavigate = { screen = it })
                        Screen.BankProfiles -> MainScreen(db.userDao(), db.bankProfileDao())
                        Screen.WebLoginProfiles -> {
                            Text("Web Login Profiles Screen (Coming soon!)")
                        }
                        Screen.LicProfiles -> {
                            Text("LIC Profiles Screen (Coming soon!)")
                        }
                        Screen.ResetPassword -> ResetPasswordScreen(onBack = { screen = Screen.Start })
                    }
                }
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (screen != Screen.Start) {
            screen = Screen.Start
        } else {
            super.onBackPressed()
        }
    }

    private fun ensureStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 1001)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 &&
            (grantResults.isEmpty() || grantResults[0] != android.content.pm.PackageManager.PERMISSION_GRANTED)
        ) {
            Toast.makeText(
                this,
                "Storage permission is required to download updates",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
