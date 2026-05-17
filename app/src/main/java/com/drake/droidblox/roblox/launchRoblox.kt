package com.drake.droidblox.roblox

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import com.drake.droidblox.logger.AndroidLogger
import com.drake.droidblox.logger.Logger
import com.drake.droidblox.sharedprefs.FastFlagsManager
import com.drake.droidblox.sharedprefs.SettingsManager
import java.io.File

private const val TAG = "DBLaunchRoblox"
private const val ROBLOX_PACKAGE = "com.roblox.client"

private val logger: Logger = AndroidLogger

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

fun launchRoblox(
    context: Context,
    settingsManager: SettingsManager,
    fflagsManager: FastFlagsManager,
    deeplink: String = ""
) {
    if (settingsManager.applyFFlags) {
        logger.d(TAG, "Applying fast flags")
        val currentFFlags = fflagsManager.rawFFlags
        if (currentFFlags != null) {
            val fflagDir = File(context.filesDir, "exe/ClientSettings")
            val fflagFile = File(fflagDir, "ClientAppSettings.json")
            if (!fflagDir.exists()) {
                logger.d(TAG, "Creating ClientSettings directory")
                if (fflagDir.mkdirs()) {
                    logger.d(TAG, "Successfully created directory, writing fflags")
                    fflagFile.writeText(currentFFlags)
                } else {
                    logger.e(TAG, "Couldn't create directory")
                }
            } else {
                logger.d(TAG, "Directory exists, writing fflags")
                fflagFile.writeText(currentFFlags)
            }
        }
    }
    val intent = Intent(context, Class.forName("$ROBLOX_PACKAGE.ActivityProtocolLaunch"))
    intent.setData(deeplink.toUri())
    logger.d(TAG, "Starting activity")
    context.startActivity(intent)
}