package com.faith.securedigitalwallet.ui

import androidx.compose.runtime.*
import com.faith.securedigitalwallet.data.*

@Composable
fun MainScreenDoc(userDocDao: UserDocDao, userDocFilesDao: UserDocFilesDao) {
    var selectedUser by remember { mutableStateOf<UserDocument?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }

    if (selectedUser == null) {
        UserListScreenDoc(
            userDocDao = userDocDao,
            onUserSelected = { selectedUser = it },
            onUserDeleted = { deletedUser ->
                if (selectedUser == deletedUser) {
                    selectedUser = null
                }
            },
            onAddUser = { showAddUserDialog = true }
        )
    } else {
        UserDocumentsScreen(
            user = selectedUser!!,
            userDocFilesDao = userDocFilesDao
        )
    }
}