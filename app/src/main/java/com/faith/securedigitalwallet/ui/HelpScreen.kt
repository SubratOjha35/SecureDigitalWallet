package com.faith.securedigitalwallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Secure Digital Wallet", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))
            Text("A trusted app to store your digital secrets securely and locally.")

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("1. Bank Profiles")
            Text(
                "• Store multiple profiles with details like Account Number, IFSC, ATM PIN, "
                        + "Mobile Number, Internet Banking Password, and more.\n"
                        + "• Edit or delete entries securely."
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("2. LIC Profiles")
            Text(
                "• Manage your Life Insurance details including Policy Number, Premium, "
                        + "Due Dates, and Nominee Info."
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("3. Web Login Profiles")
            Text(
                "• Save website credentials securely.\n"
                        + "• Store site name, username, password, and notes."
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("4. Documents")
            Text(
                "• Upload and store Aadhaar, PAN, Voter ID, Passbook, or any custom docs.\n"
                        + "• Supports both Photo and PDF.\n"
                        + "• Open files with external viewer.\n"
                        + "• Master password protects view, edit, and share actions."
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Security Features")
            Text(
                "• Master Password: Required for sensitive operations.\n"
                        + "• Local-only storage: Data never leaves your device.\n"
                        + "• Password-protected document actions."
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Support & Info")
            Text("\t1. Subrat Kumar Ojha <ojhasubrat35@gmail.com> \n"
                    + "\t2. Anshuman Jha <anshuman.jha18@gmail.com>")
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
}
