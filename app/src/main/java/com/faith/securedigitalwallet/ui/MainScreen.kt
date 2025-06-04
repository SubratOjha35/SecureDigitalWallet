package com.faith.securedigitalwallet.ui

import androidx.compose.runtime.*
import com.faith.securedigitalwallet.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*

@Composable
fun MainScreen(userDao: UserDao, bankProfileDao: BankProfileDao) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (selectedUser == null) {
        UserListScreen(
            userDao = userDao,
            onUserSelected = { selectedUser = it },
            onUserDeleted = { deletedUser ->
                if (selectedUser == deletedUser) {
                    selectedUser = null
                }
            },
            onAddUser = { showAddUserDialog = true }
        )

        if (showAddUserDialog) {
            AddUserDialog(
                onDismiss = { showAddUserDialog = false },
                onSave = { newUser ->
                    showAddUserDialog = false
                    coroutineScope.launch(Dispatchers.IO) {
                        userDao.insertUser(newUser)
                    }
                }
            )
        }
    } else {
        BankProfilesScreen(
            user = selectedUser!!,
            bankProfileDao = bankProfileDao,
            onBack = { selectedUser = null },
            onNoProfiles = { selectedUser = null }
        )
    }
}

@Composable
fun AddUserDialog(onDismiss: () -> Unit, onSave: (User) -> Unit) {
    var userName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            Column {
                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        if (it.isNotBlank()) showError = false
                    },
                    label = { Text("User Name") },
                    isError = showError,
                    singleLine = true
                )
                if (showError) {
                    Text(
                        text = "User name cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (userName.isNotBlank()) {
                    onSave(User(id = 0, name = userName))
                } else {
                    showError = true
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
