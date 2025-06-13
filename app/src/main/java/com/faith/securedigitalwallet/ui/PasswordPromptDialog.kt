package com.faith.securedigitalwallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

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
    var passwordVisible by remember { mutableStateOf(false) }
    var showHelpTooltip by remember { mutableStateOf(false) }

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
                Box {
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            showError = false
                            validationError = null
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                                if (isSettingPassword) {
                                    Box {
                                        IconButton(onClick = { showHelpTooltip = !showHelpTooltip }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Help,
                                                contentDescription = "Password requirements"
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showHelpTooltip,
                                            onDismissRequest = { showHelpTooltip = false },
                                            properties = PopupProperties(focusable = false),
                                            modifier = Modifier
                                                .widthIn(min = 240.dp)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("• At least 8 characters") },
                                                onClick = {}
                                            )
                                            DropdownMenuItem(
                                                text = { Text("• At least one uppercase letter") },
                                                onClick = {}
                                            )
                                            DropdownMenuItem(
                                                text = { Text("• At least one lowercase letter") },
                                                onClick = {}
                                            )
                                            DropdownMenuItem(
                                                text = { Text("• At least one number") },
                                                onClick = {}
                                            )
                                            DropdownMenuItem(
                                                text = { Text("• At least one special character") },
                                                onClick = {}
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
