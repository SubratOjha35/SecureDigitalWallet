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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.faith.securedigitalwallet.data.AppDatabase
import com.faith.securedigitalwallet.ui.*
import com.faith.securedigitalwallet.ui.theme.SecureBankAppTheme
import com.faith.securedigitalwallet.util.GitHubUpdateHelper
import com.faith.securedigitalwallet.util.UpdateState

class MainActivity : AppCompatActivity() {

    private var screen by mutableStateOf<Screen>(Screen.Start)
    private var updateState by mutableStateOf<UpdateState>(UpdateState.Checking)

    private lateinit var apkInstallLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
*/
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "securebank-db"
        ).build()

        // Register launcher before setContent
        apkInstallLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Regardless of result, reset the update state
            updateState = UpdateState.Completed
        }

        setContent {
            SecureBankAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (updateState) {
                        is UpdateState.Checking -> LoadingScreen("Checking for updates…")
                        is UpdateState.Downloading -> LoadingScreen("Waiting for download…")
                        else -> {
                            when (screen) {
                                Screen.Start -> StartScreen(onNavigate = { screen = it })
                                Screen.BankProfiles -> MainScreenBank(db.userDao(), db.bankProfileDao())
                                Screen.WebLoginProfiles -> Text("Web Login Profiles Screen (Coming soon!)")
                                Screen.LicProfiles -> Text("LIC Profiles Screen (Coming soon!)")
                                Screen.ResetPassword -> ResetPasswordScreen(onBack = { screen = Screen.Start })
                                Screen.UserDocument -> MainScreenDoc(db.userDao(), db.docProfileDao())
                                Screen.Help ->  HelpScreen(onBack = { screen = Screen.Start })
                            }
                        }
                    }

                    LaunchedEffect(Unit) {
                        requestStoragePermissionIfNeeded()
                        if (canInstallUnknownApps()) {
                            GitHubUpdateHelper.checkForUpdate(
                                context = this@MainActivity,
                                onStateChanged = { updateState = it },
                                onUpdateComplete = {
                                    updateState = UpdateState.Completed
                                },
                                onUpdateFailed = {
                                    updateState = UpdateState.Failed
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Update check failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                launchInstaller = { intent ->
                                    apkInstallLauncher.launch(intent)
                                }
                            )
                        } else {
                            requestInstallPermission()
                            updateState = UpdateState.Completed
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
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
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

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}
