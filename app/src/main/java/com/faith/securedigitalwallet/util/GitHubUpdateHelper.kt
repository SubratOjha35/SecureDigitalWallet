package com.faith.securedigitalwallet.util

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object GitHubUpdateHelper {
    private const val TAG = "GitHubUpdateHelper"
    private const val GITHUB_REPO = "SubratOjha35/SecureDigitalWallet"
    private const val APK_FILE_NAME = "SecureDigitalWallet.apk"

    private const val PREFS_NAME = "update_prefs"
    private const val KEY_DOWNLOAD_ID = "expected_download_id"

    private fun saveExpectedDownloadId(context: Context, downloadId: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_DOWNLOAD_ID, downloadId)
            .apply()
    }

    fun getExpectedDownloadId(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_DOWNLOAD_ID, -1L)
    }

    /**
     * Checks GitHub for the latest release APK.
     * Calls onUpdateComplete if no update or after successful download.
     * Calls onUpdateFailed on any failure.
     */
    fun checkForUpdate(
        context: Context,
        onUpdateComplete: () -> Unit = {},
        onUpdateFailed: () -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiUrl = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
                val connection = URL(apiUrl).openConnection() as HttpsURLConnection
                connection.connect()

                if (connection.responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val latestVersion = json.getString("tag_name").removePrefix("v")

                    val assets = json.getJSONArray("assets")
                    var apkUrl: String? = null
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.getString("name").endsWith(".apk")) {
                            apkUrl = asset.getString("browser_download_url")
                            break
                        }
                    }

                    val currentVersion = getCurrentVersion(context)

                    if (apkUrl != null && isNewerVersion(latestVersion, currentVersion)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Update available: $currentVersion → $latestVersion",
                                Toast.LENGTH_LONG
                            ).show()
                            startDownloadAndInstall(context, apkUrl, onUpdateComplete, onUpdateFailed)
                        }
                    } else {
                        withContext(Dispatchers.Main) { onUpdateComplete() }
                    }
                } else {
                    Log.w(TAG, "GitHub API request failed: ${connection.responseCode}")
                    withContext(Dispatchers.Main) { onUpdateFailed() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for update", e)
                withContext(Dispatchers.Main) { onUpdateFailed() }
            }
        }
    }

    private fun startDownloadAndInstall(
        context: Context,
        apkUrl: String,
        onUpdateComplete: () -> Unit,
        onUpdateFailed: () -> Unit
    ) {
        val apkFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            APK_FILE_NAME
        )

        if (apkFile.exists()) {
            Log.d(TAG, "Deleting old APK: ${apkFile.absolutePath}")
            apkFile.delete()
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(apkUrl.toUri()).apply {
            setTitle("Downloading Update")
            setDescription("SecureDigitalWallet update")
            setMimeType("application/vnd.android.package-archive")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
            setAllowedOverRoaming(false)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, APK_FILE_NAME)
        }

        try {
            val downloadId = downloadManager.enqueue(request)
            saveExpectedDownloadId(context, downloadId)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                    if (id == downloadId) {
                        context.unregisterReceiver(this)
                        installApk(context)
                        onUpdateComplete()
                    }
                }
            }

            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
            onUpdateFailed()
        }
    }

    private fun getCurrentVersion(context: Context): String {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName ?: "0.0.0"
        } catch (e: Exception) {
            Log.e(TAG, "Version fetch failed", e)
            "0.0.0"
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun installApk(context: Context) {
        val apkFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            APK_FILE_NAME
        )

        if (!apkFile.exists()) {
            Toast.makeText(context, "Update file missing, please retry.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(intent)

            CoroutineScope(Dispatchers.IO).launch {
                delay(5000)
                if (apkFile.exists()) {
                    apkFile.delete()
                    Log.d(TAG, "APK file deleted after install")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch installer", e)
            Toast.makeText(context, "Unable to install update", Toast.LENGTH_SHORT).show()
        }
    }
}
