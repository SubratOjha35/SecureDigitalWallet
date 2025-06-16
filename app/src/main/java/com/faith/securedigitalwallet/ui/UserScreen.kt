package com.faith.securedigitalwallet.ui

import androidx.compose.runtime.*
import com.faith.securedigitalwallet.data.User
import com.faith.securedigitalwallet.data.UserDao

@Composable
fun UserScreen(
    userDao: UserDao,
    onUserSelected: (User) -> Unit,
    onUserDeleted: (User) -> Unit
) {
    val usersFlow = userDao.getAllUsers().collectAsState(initial = emptyList())

    UserListScreenGeneric(
        usersFlow = usersFlow,
        onDeleteUserConfirmed = {
            userDao.deleteUser(it)
            onUserDeleted(it)
        },
        onUserSelected = {
            onUserSelected(it)
        },
        userName = { it.name },
        addUserDialogContent = { onDismiss, onUserAdded ->
            AddUserDialogGeneric<User>(
                checkUserExists = { userDao.userExists(it) },
                insertUser = { userDao.insertUser(User(name = it)) },
                onDismiss = onDismiss,
                onUserAdded = onUserAdded
            )
        }
    )
}