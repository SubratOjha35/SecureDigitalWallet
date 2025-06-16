package com.faith.securedigitalwallet.ui

import androidx.compose.runtime.*
import com.faith.securedigitalwallet.data.*

@Composable
fun MainScreenDoc(userDao: UserDao, userDocFilesDao: DocumentProfileDao) {
    var selectedUser by remember { mutableStateOf<User?>(null) }

    if (selectedUser == null) {
        UserScreen(
            userDao = userDao,
            onUserSelected = { selectedUser = it },
            onUserDeleted = { deletedUser ->
                if (selectedUser == deletedUser) {
                    selectedUser = null
                }
            }
        )
    } else {
        DocumentProfileScreen(
            user = selectedUser!!,
            userDocFilesDao = userDocFilesDao
        )
    }
}