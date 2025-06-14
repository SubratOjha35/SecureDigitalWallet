package com.faith.securedigitalwallet.util

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

fun getAppInfo(context: Context): Pair<String, String> {
    val packageManager = context.packageManager
    val packageName = context.packageName

    val appName = try {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        "Unknown App"
    }

    val versionName = try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }

    return Pair(appName, versionName)
}

@Composable
fun SetSecureFlag(enable: Boolean) {
    val context = LocalContext.current
    LaunchedEffect(enable) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val window = activity.window
        if (enable) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
