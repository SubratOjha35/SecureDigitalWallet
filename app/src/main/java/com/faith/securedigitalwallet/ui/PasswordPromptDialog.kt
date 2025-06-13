package com.faith.securedigitalwallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordPromptDialog(
    isSettingPassword: Boolean,
    onDismiss: () -> Unit,
    onPasswordEntered: (String) -> Unit,
    errorMessage: String? = null
) {
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        showError = !errorMessage.isNullOrEmpty()
        validationError = errorMessage
    }

    fun validatePassword(pw: String): String? {
        if (pw.length < 8) return "Password must be at least 8 characters"
        if (!pw.any { it.isUpperCase() }) return "Password must contain at least one uppercase letter"
        if (!pw.any { it.isLowerCase() }) return "Password must contain at least one lowercase letter"
        if (!pw.any { it.isDigit() }) return "Password must contain at least one number"
        if (!pw.any { !it.isLetterOrDigit() }) return "Password must contain at least one special character"
        return null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isSettingPassword) "Set Master Password" else "Enter Master Password")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                        validationError = null
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        validationError ?: "Password cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (isSettingPassword) {
                    val validation = validatePassword(password)
                    if (validation != null) {
                        validationError = validation
                        showError = true
                        return@TextButton
                    }
                }
                if (password.isBlank()) {
                    validationError = "Password cannot be empty"
                    showError = true
                    return@TextButton
                }
                onPasswordEntered(password)
            }) {
                Text(if (isSettingPassword) "Set" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
