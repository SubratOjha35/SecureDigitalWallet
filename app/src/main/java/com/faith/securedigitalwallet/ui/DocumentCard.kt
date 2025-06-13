package com.faith.securedigitalwallet.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.faith.securedigitalwallet.data.PasswordManager
import kotlinx.coroutines.launch

@Composable
fun DocumentCard(
    label: String,
    filePath: String?,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onShare: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onCaptureImage: () -> Unit,
    onPickFromGallery: () -> Unit,
    onUploadPdf: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showPasswordPrompt by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun requestPasswordBefore(action: () -> Unit) {
        pendingAction = action
        showPasswordPrompt = true
    }

    if (showPasswordPrompt) {
        PasswordPromptDialog(
            isSettingPassword = false,
            onDismiss = {
                showPasswordPrompt = false
                passwordError = null
            },
            errorMessage = passwordError,
            onPasswordEntered = { enteredPassword ->
                coroutineScope.launch {
                    val savedPassword = PasswordManager.getMasterPassword(context)
                    if (enteredPassword == savedPassword) {
                        showPasswordPrompt = false
                        passwordError = null
                        pendingAction?.invoke()
                    } else {
                        passwordError = "Incorrect password"
                    }
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = label, style = MaterialTheme.typography.titleMedium)

                if (filePath == null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCaptureImage) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Capture Image")
                        }
                        IconButton(onClick = onPickFromGallery) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick from Gallery")
                        }
                        IconButton(onClick = onUploadPdf) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Upload PDF")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filePath != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { requestPasswordBefore(onView) }) {
                        Icon(Icons.Default.Visibility, contentDescription = "View $label")
                    }
                    IconButton(onClick = { requestPasswordBefore(onEdit) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit $label")
                    }
                    onShare?.let {
                        IconButton(onClick = { requestPasswordBefore(it) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share $label")
                        }
                    }
                    onDelete?.let {
                        IconButton(onClick = { requestPasswordBefore(it) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete $label")
                        }
                    }
                }
            }
        }
    }
}
