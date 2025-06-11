package com.faith.securedigitalwallet

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.faith.securedigitalwallet.util.GitHubUpdateHelper

class DownloadCompleteReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DownloadCompleteReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Download complete broadcast received!")

        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null - aborting")
            return
        }

        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            Log.d(TAG, "Received download ID: $downloadId")

            val expectedId = GitHubUpdateHelper.getExpectedDownloadId(context)
            if (downloadId == expectedId) {
                Log.d(TAG, "Download ID matches expected. Proceeding to install.")

                GitHubUpdateHelper.installApk(context) { state ->
                    // Optional: log state; UI won't auto-update from here directly.
                    Log.d(TAG, "Update state changed to: $state")
                }

            } else {
                Log.w(TAG, "Download ID does not match expected. Ignoring.")
            }
        }
    }
}
