package com.drake.droidblox.roblox

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import com.drake.droidblox.logger.Logger
import com.drake.droidblox.sharedprefs.FastFlagsManager
import com.drake.droidblox.sharedprefs.SettingsManager
import com.drake.droidblox.shizuku.ShizukuHelper

private const val TAG = "DBLaunchRoblox"
const val ROBLOX_PACKAGE = "com.roblox.client"

fun isRobloxInstalled(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo(ROBLOX_PACKAGE, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}

fun getRobloxVersion(context: Context): String? {
    return try {
        val pkgInfo = context.packageManager.getPackageInfo(ROBLOX_PACKAGE, 0)
        pkgInfo.versionName
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}

private fun resolveLaunchIntent(context: Context, deeplink: String): Intent? {
    if (deeplink.isNotBlank()) {
        val deepIntent = Intent(Intent.ACTION_VIEW, deeplink.toUri()).apply {
            `package` = ROBLOX_PACKAGE
        }
        if (deepIntent.resolveActivity(context.packageManager) != null) return deepIntent
    }

    val launchIntent = context.packageManager.getLaunchIntentForPackage(ROBLOX_PACKAGE)
    if (launchIntent != null) return launchIntent

    return null
}

fun launchRoblox(
    context: Context,
    settingsManager: SettingsManager,
    fflagsManager: FastFlagsManager,
    logger: Logger,
    deeplink: String = ""
) {
    try {
        if (settingsManager.applyFFlags) {
            logger.d(TAG, "Applying fast flags")
            val currentFFlags = fflagsManager.rawFFlags
            if (currentFFlags != null) {
                if (settingsManager.useShizuku && ShizukuHelper.isBound) {
                    val path = "/data/local/tmp/ClientAppSettings.json"
                    logger.d(TAG, "Writing fflags via Shizuku to $path")
                    if (!ShizukuHelper.writeFile(path, currentFFlags)) {
                        logger.e(TAG, "Shizuku write failed")
                    }
                } else {
                    logger.w(TAG, "Shizuku not available — FFlags require Shizuku + Xposed (LSPosed) to work")
                }
            }
        }

        val intent = resolveLaunchIntent(context, deeplink)
        if (intent == null) {
            logger.e(TAG, "No launch intent found — is Roblox really installed?")
            return
        }
        logger.d(TAG, "Starting activity: action=${intent.action} data=${intent.data} flags=${intent.flags}")
        context.startActivity(intent)
    } catch (e: Exception) {
        logger.e(TAG, "Failed to launch Roblox: ${e.message}")
    }
}
