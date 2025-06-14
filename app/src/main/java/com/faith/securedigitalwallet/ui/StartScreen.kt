package com.faith.securedigitalwallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faith.securedigitalwallet.util.getAppInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(onNavigate: (Screen) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showContributorsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                NavigationDrawerItem(
                    label = { Text("Reset Password") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigate(Screen.ResetPassword)
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Help") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigate(Screen.Help) // <- Navigate to Help screen
                    }
                )
                NavigationDrawerItem(
                    label = { Text("About") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showAboutDialog = true
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Contributors") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showContributorsDialog = true
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("SecureDigitalWallet", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

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

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigate(Screen.UserDocument) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("User Documents")
                }
            }
        }

        if (showContributorsDialog) {
            AlertDialog(
                onDismissRequest = { showContributorsDialog = false },
                title = { Text("Contributors") },
                text = {
                    Column {
                        Text("1. Subrat Kumar Ojha")
                        Text("2. Anshuman Jha")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showContributorsDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showAboutDialog) {
            val (appName, versionName) = getAppInfo(context)
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("About") },
                text = {
                    Column {
                        Text("App Name: $appName")
                        Text("Version: $versionName")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
