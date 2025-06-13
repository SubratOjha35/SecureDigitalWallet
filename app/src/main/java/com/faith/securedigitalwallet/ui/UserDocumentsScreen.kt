package com.faith.securedigitalwallet.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.faith.securedigitalwallet.data.UserDocument
import com.faith.securedigitalwallet.data.UserDocumentFiles
import com.faith.securedigitalwallet.data.UserDocFilesDao
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun UserDocumentsScreen(
    user: UserDocument,
    userDocFilesDao: UserDocFilesDao
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var doc by remember { mutableStateOf<UserDocumentFiles?>(null) }
    var currentLabel by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var newDocLabel by remember { mutableStateOf("") }
    var customDocs by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }
    var imageToView by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentLabel != null && photoUri != null) {
            scope.launch {
                val newPath = photoUri.toString()
                val updated = (doc ?: UserDocumentFiles(userId = user.id)).let { original ->
                    when (currentLabel) {
                        "Aadhaar" -> original.copy(aadhaarPath = newPath)
                        "PAN Card" -> original.copy(panCardPath = newPath)
                        "Voter ID" -> original.copy(voterIdPath = newPath)
                        "Driving Licence" -> original.copy(drivingLicence = newPath)
                        else -> original.copy(
                            extraDocs = (original.extraDocs ?: mutableMapOf()).toMutableMap().apply {
                                this[currentLabel!!] = newPath
                            }
                        )
                    }
                }
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

            DocumentCard(
                label = label,
                filePath = path,
                onView = { imageToView = path },
                onEdit = {
                    currentLabel = label
                    val uri = createImageFile(context, label)
                    photoUri = uri
                    cameraLauncher.launch(uri)
                },
                onShare = {
                    path?.let { shareImage(context, it) }
                },
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
                }
            )
        }

        customDocs.forEach { (label, path) ->
            DocumentCard(
                label = label,
                filePath = path,
                onView = { imageToView = path },
                onEdit = {
                    currentLabel = label
                    val uri = createImageFile(context, label)
                    photoUri = uri
                    cameraLauncher.launch(uri)
                },
                onShare = {
                    path?.let { shareImage(context, it) }
                },
                onDelete = {
                    scope.launch {
                        val updated = doc?.copy(
                            extraDocs = doc?.extraDocs?.toMutableMap()?.apply { remove(label) }
                        ) ?: return@launch
                        userDocFilesDao.insertOrUpdate(updated)
                        doc = updated
                        customDocs = updated.extraDocs ?: emptyMap()
                    }
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

        val trimmedLabel = newDocLabel.trim()
        val isLabelValid = trimmedLabel.isNotEmpty() && !customDocs.containsKey(trimmedLabel)

        if (isLabelValid) {
            Button(
                onClick = {
                    currentLabel = trimmedLabel
                    val uri = createImageFile(context, trimmedLabel)
                    photoUri = uri
                    cameraLauncher.launch(uri)
                    newDocLabel = ""
                }
            ) {
                Text("Capture")
            }
        }
    }

    if (imageToView != null) {
        FullScreenImageDialog(
            uri = imageToView!!,
            onDismiss = { imageToView = null }
        )
    }
}

fun createImageFile(context: Context, label: String): Uri {
    val timestamp = System.currentTimeMillis()
    val dir = File(context.getExternalFilesDir("doc"), "")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "${label.lowercase().replace(" ", "_")}_$timestamp.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

@Composable
fun FullScreenImageDialog(uri: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
            )
        }
    )
}

fun shareImage(context: Context, uriString: String) {
    val uri = Uri.parse(uriString) // Already a content:// URI from FileProvider

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share document"))
}
