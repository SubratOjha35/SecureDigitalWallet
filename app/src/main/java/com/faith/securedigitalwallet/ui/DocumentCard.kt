package com.faith.securedigitalwallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Show confirmation dialog before deleting
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete \"$label\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDelete?.invoke()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
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
                    IconButton(onClick = { onView() }) {
                        Icon(Icons.Default.Visibility, contentDescription = "View $label")
                    }
                    IconButton(onClick = { onEdit() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit $label")
                    }
                    onShare?.let {
                        IconButton(onClick = { it() }) {
                            Icon(Icons.Default.Share, contentDescription = "Share $label")
                        }
                    }
                    onDelete?.let {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete $label")
                        }
                    }
                }
            }
        }
    }
}
