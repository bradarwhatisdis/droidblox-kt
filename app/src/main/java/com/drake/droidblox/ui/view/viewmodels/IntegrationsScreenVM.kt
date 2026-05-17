package com.drake.droidblox.ui.view.viewmodels

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.drake.droidblox.logger.Logger
import com.drake.droidblox.roblox.getRobloxVersion
import com.drake.droidblox.roblox.isRobloxInstalled
import com.drake.droidblox.roblox.launchRoblox
import com.drake.droidblox.sharedprefs.FastFlagsManager
import com.drake.droidblox.sharedprefs.SettingsManager
import com.drake.droidblox.shizuku.ShizukuHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import rikka.shizuku.Shizuku
import javax.inject.Inject

data class RobloxStatus(
    val installed: Boolean,
    val version: String?
)

data class ShizukuState(
    val available: Boolean,
    val hasPermission: Boolean,
    val bound: Boolean
)

enum class ShizukuConnectionState {
    Idle, Requesting, Connecting, Connected, Failed
}

@HiltViewModel
class IntegrationsScreenVM @Inject constructor(
    @ApplicationContext private val context: Context?,
    val settingsManager: SettingsManager,
    val fflagsManager: FastFlagsManager,
    private val logger: Logger
) : ViewModel() {
    var robloxStatus = mutableStateOf(context?.let { RobloxStatus(isRobloxInstalled(it), getRobloxVersion(it)) })
    var shizukuState = mutableStateOf(
        ShizukuState(
            available = ShizukuHelper.isAvailable(),
            hasPermission = ShizukuHelper.hasPermission(),
            bound = ShizukuHelper.isBound
        )
    )
    var shizukuConnectionState = mutableStateOf(ShizukuConnectionState.Idle)

    private var permissionListener: Shizuku.OnRequestPermissionResultListener? = null

    fun refreshRobloxStatus() {
        robloxStatus.value = context?.let { RobloxStatus(isRobloxInstalled(it), getRobloxVersion(it)) }
    }

    fun refreshShizukuState() {
        shizukuState.value = ShizukuState(
            available = ShizukuHelper.isAvailable(),
            hasPermission = ShizukuHelper.hasPermission(),
            bound = ShizukuHelper.isBound
        )
    }

    fun requestAndConnectShizuku() {
        shizukuConnectionState.value = ShizukuConnectionState.Requesting
        permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                shizukuConnectionState.value = ShizukuConnectionState.Connecting
                connectShizuku()
            } else {
                shizukuConnectionState.value = ShizukuConnectionState.Idle
            }
            permissionListener?.let { Shizuku.removeRequestPermissionResultListener(it) }
            permissionListener = null
        }
        Shizuku.addRequestPermissionResultListener(permissionListener!!)
        ShizukuHelper.requestPermission()
    }

    fun connectShizuku() {
        context?.let { ctx ->
            ShizukuHelper.bind(ctx) { success ->
                shizukuConnectionState.value = if (success) ShizukuConnectionState.Connected else ShizukuConnectionState.Failed
                refreshShizukuState()
            }
        } ?: run {
            shizukuConnectionState.value = ShizukuConnectionState.Failed
        }
    }

    fun launchRoblox() = context?.let {
        launchRoblox(
            context = it,
            settingsManager = settingsManager,
            fflagsManager = fflagsManager,
            logger = logger
        )
    }

    override fun onCleared() {
        super.onCleared()
        permissionListener?.let { Shizuku.removeRequestPermissionResultListener(it) }
        ShizukuHelper.unbind()
    }
}
