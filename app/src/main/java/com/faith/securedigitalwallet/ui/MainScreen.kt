package com.faith.securedigitalwallet.ui

import androidx.compose.runtime.*
import com.faith.securedigitalwallet.data.*

@Composable
fun MainScreen(userDao: UserDao, bankProfileDao: BankProfileDao) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }

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
    } else {
        BankProfilesScreen(
            user = selectedUser!!,
            bankProfileDao = bankProfileDao,
            onBack = { selectedUser = null },
            onNoProfiles = { selectedUser = null }
        )
    }
}
