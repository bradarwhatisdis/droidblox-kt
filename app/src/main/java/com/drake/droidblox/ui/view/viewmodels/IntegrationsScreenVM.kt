package com.drake.droidblox.ui.view.viewmodels

import android.content.Context
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

    fun requestShizukuPermission() = ShizukuHelper.requestPermission()

    fun bindShizuku() {
        context?.let { ShizukuHelper.bind(it) }
        refreshShizukuState()
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
        ShizukuHelper.unbind()
    }
}
