// MainActivity.kt
package com.faith.securedigitalwallet

import android.os.Bundle
import android.view.WindowManager
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

class MainActivity : AppCompatActivity() {
    private var screen by mutableStateOf<Screen>(Screen.Start)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                    when (screen) {
                        Screen.Start -> StartScreen(onNavigate = { screen = it })
                        Screen.BankProfiles -> MainScreen(db.userDao(), db.bankProfileDao())
                        Screen.WebLoginProfiles -> {
                            // TODO: Add WebLoginProfilesScreen
                            Text("Web Login Profiles Screen (Coming soon!)")
                        }
                        Screen.LicProfiles -> {
                            // TODO: Add LicProfilesScreen
                            Text("LIC Profiles Screen (Coming soon!)")
                        }
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
}
