// ui/AddUserDialog.kt
package com.faith.securedigitalwallet.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun <T> AddUserDialogGeneric(
    checkUserExists: suspend (String) -> Boolean,
    insertUser: suspend (String) -> Unit,
    onDismiss: () -> Unit,
    onUserAdded: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            Column {
                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        errorMessage = null
                    },
                    label = { Text("Name") },
                    isError = errorMessage != null
                )
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (userName.isBlank()) {
                    errorMessage = "Name cannot be empty"
                    return@TextButton
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val trimmed = userName.trim()
                    val exists = checkUserExists(trimmed)
                    if (exists) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "User already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        insertUser(trimmed)
                        withContext(Dispatchers.Main) {
                            onUserAdded()
                            onDismiss()
                        }
                    }
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
