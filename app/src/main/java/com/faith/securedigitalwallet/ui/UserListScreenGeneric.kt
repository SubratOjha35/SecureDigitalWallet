package com.faith.securedigitalwallet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.faith.securedigitalwallet.data.PasswordManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> UserListScreenGeneric(
    usersFlow: State<List<T>>,
    onDeleteUserConfirmed: suspend (T) -> Unit,
    onUserSelected: suspend (T) -> Unit,
    userName: (T) -> String,
    addUserDialogContent: @Composable (onDismiss: () -> Unit, onUserAdded: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val users by usersFlow

    var deletingUser by remember { mutableStateOf<T?>(null) }
    var selectedUser by remember { mutableStateOf<T?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showAddUserDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Users") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddUserDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
    ) { padding ->

        if (users.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No users found. Click + to add a user.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(users) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Icon"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = userName(user),
                                    color = Color(0xFF1A73E8),
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable {
                                        selectedUser = user
                                        showPasswordDialog = true
                                    }
                                )
                            }
                            Row {
                                TextButton(onClick = {
                                    deletingUser = user
                                    showDeleteDialog = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete User")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Confirm delete dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete User") },
                text = { Text("Are you sure you want to delete this user?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        showPasswordDialog = true
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        deletingUser = null
                        showDeleteDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Master password prompt
        if (showPasswordDialog) {
            PasswordPromptDialog(
                isSettingPassword = false,
                onDismiss = {
                    showPasswordDialog = false
                    deletingUser = null
                    selectedUser = null
                    passwordError = null
                },
                errorMessage = passwordError,
                onPasswordEntered = { enteredPassword ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val actualPassword = PasswordManager.getMasterPassword(context)
                        if (enteredPassword == actualPassword) {
                            withContext(Dispatchers.Main) {
                                passwordError = null
                                showPasswordDialog = false
                            }

                            deletingUser?.let {
                                onDeleteUserConfirmed(it)
                                withContext(Dispatchers.Main) {
                                    deletingUser = null
                                }
                            }

                            selectedUser?.let {
                                onUserSelected(it)
                                withContext(Dispatchers.Main) {
                                    selectedUser = null
                                }
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                passwordError = "Incorrect master password"
                            }
                        }
                    }
                }
            )
        }

        // ✅ Add user dialog
        if (showAddUserDialog) {
            addUserDialogContent(
                { showAddUserDialog = false },
                { showAddUserDialog = false }
            )
        }
    }
}
