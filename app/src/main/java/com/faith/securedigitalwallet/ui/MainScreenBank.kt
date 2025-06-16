package com.faith.securedigitalwallet.ui

import androidx.compose.runtime.*
import com.faith.securedigitalwallet.data.*

@Composable
fun MainScreenBank(userDao: UserDao, bankProfileDao: BankProfileDao) {
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
        BankProfilesScreen(
            user = selectedUser!!,
            bankProfileDao = bankProfileDao,
            onBack = { selectedUser = null },
            onNoProfiles = { selectedUser = null }
        )
    }
}
