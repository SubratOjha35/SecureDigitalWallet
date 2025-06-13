package com.faith.securedigitalwallet.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.faith.securedigitalwallet.data.UserDocumentFiles
import java.io.File

fun updateDocPath(
    doc: UserDocumentFiles?,
    label: String,
    newPath: String,
    userId: Int
): UserDocumentFiles {
    return (doc ?: UserDocumentFiles(userId = userId)).let { original ->
        when (label) {
            "Aadhaar" -> original.copy(aadhaarPath = newPath)
            "PAN Card" -> original.copy(panCardPath = newPath)
            "Voter ID" -> original.copy(voterIdPath = newPath)
            "Driving Licence" -> original.copy(drivingLicence = newPath)
            else -> original.copy(
                extraDocs = (original.extraDocs ?: mutableMapOf()).toMutableMap().apply {
                    this[label] = newPath
                }
            )
        }
    }
}

fun createImageFile(context: Context, label: String): Uri {
    val timestamp = System.currentTimeMillis()
    val dir = File(context.getExternalFilesDir("doc"), "")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "${label.lowercase().replace(" ", "_")}_$timestamp.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

fun savePickedFileToAppStorage(context: Context, sourceUri: Uri, label: String, ext: String): File {
    val inputStream = context.contentResolver.openInputStream(sourceUri)!!
    val dir = File(context.getExternalFilesDir("doc"), "")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "${label.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}.$ext")
    file.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
    return file
}

fun shareFile(context: Context, uriString: String) {
    val uri = Uri.parse(uriString)
    val mimeType = if (uriString.endsWith(".pdf")) "application/pdf" else "image/*"

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share document"))
}

fun launchGalleryView(context: Context, uriString: String?) {
    if (uriString == null) return
    val uri = Uri.parse(uriString)
    val mimeType = if (uriString.endsWith(".pdf")) "application/pdf" else "image/*"

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "View document"))
}
