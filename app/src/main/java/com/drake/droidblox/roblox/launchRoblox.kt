package com.drake.droidblox.roblox

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import com.drake.droidblox.logger.Logger
import com.drake.droidblox.sharedprefs.FastFlagsManager
import com.drake.droidblox.sharedprefs.SettingsManager
import com.drake.droidblox.shizuku.ShizukuHelper
import java.io.File

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
                if (settingsManager.useShizuku && ShizukuHelper.isAvailable()) {
                    logger.d(TAG, "Writing via Shizuku to /data/local/tmp/")
                    val ok = ShizukuHelper.writeFile(
                        "/data/local/tmp/ClientAppSettings.json",
                        currentFFlags
                    )
                    if (ok) {
                        logger.d(TAG, "Shizuku write succeeded")
                    } else {
                        logger.w(TAG, "Shizuku write failed, falling back to local")
                        writeLocalFFlags(context, currentFFlags, logger)
                    }
                } else {
                    writeLocalFFlags(context, currentFFlags, logger)
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

private fun writeLocalFFlags(context: Context, fflags: String, logger: Logger) {
    logger.d(TAG, "Writing fflags to app files dir")
    val fflagDir = File(context.filesDir, "exe/ClientSettings")
    val fflagFile = File(fflagDir, "ClientAppSettings.json")
    if (!fflagDir.exists()) {
        logger.d(TAG, "Creating ClientSettings directory")
        if (fflagDir.mkdirs()) {
            logger.d(TAG, "Successfully created directory, writing fflags")
            fflagFile.writeText(fflags)
        } else {
            logger.e(TAG, "Couldn't create directory")
        }
    } else {
        logger.d(TAG, "Directory exists, writing fflags")
        fflagFile.writeText(fflags)
    }
}