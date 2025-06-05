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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.faith.securedigitalwallet.data.PasswordManager
import com.faith.securedigitalwallet.data.User
import com.faith.securedigitalwallet.data.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    userDao: UserDao,
    onUserSelected: (User) -> Unit,
    onUserDeleted: (User) -> Unit,
    onAddUser: () -> Unit
) {
    val context = LocalContext.current
    val users by userDao.getAllUsers().collectAsState(initial = emptyList())

    var deletingUser by remember { mutableStateOf<User?>(null) }
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
                contentAlignment = androidx.compose.ui.Alignment.Center
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
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Icon"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // User name as clickable hyperlink
                                Text(
                                    text = user.name,
                                    color = Color(0xFF1A73E8),
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { onUserSelected(user) }
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

        // Confirm deletion dialog
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

        // Master password prompt for deletion
        if (showPasswordDialog) {
            PasswordPromptDialog(
                isSettingPassword = false,
                onDismiss = {
                    showPasswordDialog = false
                    deletingUser = null
                    passwordError = null
                },
                errorMessage = passwordError,
                onPasswordEntered = { enteredPassword ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val actualPassword = PasswordManager.getMasterPassword(context)
                        if (enteredPassword == actualPassword) {
                            deletingUser?.let { userToDelete ->
                                userDao.deleteUser(userToDelete)
                                withContext(Dispatchers.Main) {
                                    onUserDeleted(userToDelete)
                                    showPasswordDialog = false
                                    deletingUser = null
                                    passwordError = null
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

        // Add user dialog
        if (showAddUserDialog) {
            AddUserDialog(
                userDao = userDao,
                onDismiss = { showAddUserDialog = false },
                onUserAdded = { showAddUserDialog = false }
            )
        }
    }
}
