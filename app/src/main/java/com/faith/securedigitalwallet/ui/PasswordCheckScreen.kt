package com.faith.securedigitalwallet.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.faith.securedigitalwallet.data.PasswordManager
import kotlinx.coroutines.launch

@Composable
fun PasswordCheckScreen(onAuthComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var isSettingPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val storedPassword = PasswordManager.getMasterPassword(context)
        isSettingPassword = storedPassword.isNullOrEmpty()
        showDialog = true
    }

    if (showDialog) {
        PasswordPromptDialog(  // <--- fix: qualify the function
            isSettingPassword = isSettingPassword,
            onDismiss = { showDialog = false },
            onPasswordEntered = { entered ->
                scope.launch {
                    if (isSettingPassword) {
                        PasswordManager.saveMasterPassword(context, entered)
                        onAuthComplete()
                    } else {
                        val actual = PasswordManager.getMasterPassword(context)
                        if (entered == actual) {
                            onAuthComplete()
                        } else {
                            // Optional: Add error feedback (e.g. Toast or dialog)
                        }
                    }
                }
            }
        )
    }
}
