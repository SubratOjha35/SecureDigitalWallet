package com.faith.securedigitalwallet.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.faith.securedigitalwallet.data.DocumentProfile
import com.faith.securedigitalwallet.data.DocumentProfileDao
import com.faith.securedigitalwallet.util.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import com.faith.securedigitalwallet.data.User

@Composable
fun DocumentProfileScreen(
    user: User,
    userDocFilesDao: DocumentProfileDao
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var doc by remember { mutableStateOf<DocumentProfile?>(null) }
    var currentLabel by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var newDocLabel by remember { mutableStateOf("") }
    var customDocs by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }
    var showEditDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentLabel != null && photoUri != null) {
            scope.launch {
                val newPath = photoUri.toString()
                val updated = updateDocPath(doc, currentLabel!!, newPath, user.id)
                userDocFilesDao.insertOrUpdate(updated)
                doc = updated
                customDocs = updated.extraDocs ?: emptyMap()
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && currentLabel != null) {
            scope.launch {
                val destFile = savePickedFileToAppStorage(context, uri, currentLabel!!, "pdf")
                val newPath = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    destFile
                ).toString()

                val updated = updateDocPath(doc, currentLabel!!, newPath, user.id)
                userDocFilesDao.insertOrUpdate(updated)
                doc = updated
                customDocs = updated.extraDocs ?: emptyMap()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && currentLabel != null) {
            scope.launch {
                val destFile = savePickedFileToAppStorage(context, uri, currentLabel!!, "jpg")
                val newPath = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    destFile
                ).toString()

                val updated = updateDocPath(doc, currentLabel!!, newPath, user.id)
                userDocFilesDao.insertOrUpdate(updated)
                doc = updated
                customDocs = updated.extraDocs ?: emptyMap()
            }
        }
    }

    LaunchedEffect(user.id) {
        doc = userDocFilesDao.getByUserId(user.id)
        customDocs = doc?.extraDocs ?: emptyMap()
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Documents for ${user.name}", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        val standardLabels = listOf("Aadhaar", "PAN Card", "Voter ID", "Driving Licence")

        standardLabels.forEach { label ->
            val path = when (label) {
                "Aadhaar" -> doc?.aadhaarPath
                "PAN Card" -> doc?.panCardPath
                "Voter ID" -> doc?.voterIdPath
                "Driving Licence" -> doc?.drivingLicence
                else -> null
            }

            DocumentEntry(
                label = label,
                filePath = path,
                onView = { launchGalleryView(context, path) },
                onEdit = {
                    currentLabel = label
                    showEditDialog = true
                },
                onShare = { path?.let { shareFile(context, it) } },
                onDelete = {
                    scope.launch {
                        val updated = doc?.let {
                            when (label) {
                                "Aadhaar" -> it.copy(aadhaarPath = null)
                                "PAN Card" -> it.copy(panCardPath = null)
                                "Voter ID" -> it.copy(voterIdPath = null)
                                "Driving Licence" -> it.copy(drivingLicence = null)
                                else -> it
                            }
                        }
                        if (updated != null) {
                            userDocFilesDao.insertOrUpdate(updated)
                            doc = updated
                        }
                    }
                },
                onCaptureImage = {
                    val uri = createImageFile(context, label)
                    photoUri = uri
                    currentLabel = label
                    cameraLauncher.launch(uri)
                },
                onPickFromGallery = {
                    currentLabel = label
                    galleryLauncher.launch("image/*")
                },
                onUploadPdf = {
                    currentLabel = label
                    pdfPickerLauncher.launch("application/pdf")
                }
            )
        }

        // Show all existing and new custom document labels
        val trimmedLabel = newDocLabel.trim()
        val allCustomLabels = buildSet {
            addAll(customDocs.keys)
            if (trimmedLabel.isNotBlank()) add(trimmedLabel)
        }

        allCustomLabels.forEach { label ->
            val path = customDocs[label]

            DocumentEntry(
                label = label,
                filePath = path,
                onView = { path?.let { launchGalleryView(context, it) } },
                onEdit = {
                    currentLabel = label
                    showEditDialog = true
                },
                onShare = { path?.let { shareFile(context, it) } },
                onDelete = {
                    scope.launch {
                        val updated = doc?.copy(
                            extraDocs = doc?.extraDocs?.toMutableMap()?.apply { remove(label) }
                        ) ?: return@launch
                        userDocFilesDao.insertOrUpdate(updated)
                        doc = updated
                        customDocs = updated.extraDocs ?: emptyMap()
                    }
                },
                onCaptureImage = {
                    currentLabel = label
                    val uri = createImageFile(context, label)
                    photoUri = uri
                    cameraLauncher.launch(uri)
                    newDocLabel = ""
                },
                onPickFromGallery = {
                    currentLabel = label
                    galleryLauncher.launch("image/*")
                    newDocLabel = ""
                },
                onUploadPdf = {
                    currentLabel = label
                    pdfPickerLauncher.launch("application/pdf")
                    newDocLabel = ""
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = newDocLabel,
            onValueChange = { newDocLabel = it },
            label = { Text("Add custom document") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        if (showEditDialog && currentLabel != null) {
            EditOptionsDialog(
                label = currentLabel!!,
                onDismiss = { showEditDialog = false },
                onCaptureImage = {
                    val uri = createImageFile(context, currentLabel!!)
                    photoUri = uri
                    cameraLauncher.launch(uri)
                },
                onPickFromGallery = {
                    galleryLauncher.launch("image/*")
                },
                onUploadPdf = {
                    pdfPickerLauncher.launch("application/pdf")
                }
            )
        }
    }
}

@Composable
fun EditOptionsDialog(
    label: String,
    onDismiss: () -> Unit,
    onCaptureImage: () -> Unit,
    onPickFromGallery: () -> Unit,
    onUploadPdf: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $label") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    onDismiss()
                    onCaptureImage()
                }) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture Image"
                    )
                }

                IconButton(onClick = {
                    onDismiss()
                    onPickFromGallery()
                }) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Pick from Gallery"
                    )
                }

                IconButton(onClick = {
                    onDismiss()
                    onUploadPdf()
                }) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Pick PDF"
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
