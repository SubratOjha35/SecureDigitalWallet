package com.faith.securedigitalwallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StartScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp)) // Push header down a bit

        Text(
            text = "SecureDigitalWallet",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        )

        Spacer(modifier = Modifier.height(48.dp)) // Space between header and buttons

        Button(
            onClick = { onNavigate(Screen.ResetPassword) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Password")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onNavigate(Screen.BankProfiles) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Bank Profiles")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onNavigate(Screen.WebLoginProfiles) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Web Login Profiles")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onNavigate(Screen.LicProfiles) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LIC Profiles")
        }
    }
}
