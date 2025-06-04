// ui/AddUserDialog.kt
package com.faith.securedigitalwallet.ui

import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.faith.securedigitalwallet.data.User
import com.faith.securedigitalwallet.data.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.LocalContext


@Composable
fun AddUserDialog(
    userDao: UserDao,
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
                    val exists = userDao.userExists(userName.trim())
                    if (exists) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Account Number already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        userDao.insertUser(User(name = userName.trim()))
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
