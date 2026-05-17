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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class RobloxStatus(
    val installed: Boolean,
    val version: String?
)

@HiltViewModel
class IntegrationsScreenVM @Inject constructor(
    @ApplicationContext private val context: Context?,
    val settingsManager: SettingsManager,
    val fflagsManager: FastFlagsManager,
    private val logger: Logger
) : ViewModel() {
    var robloxStatus = mutableStateOf(context?.let { RobloxStatus(isRobloxInstalled(it), getRobloxVersion(it)) })

    fun refreshRobloxStatus() {
        robloxStatus.value = context?.let { RobloxStatus(isRobloxInstalled(it), getRobloxVersion(it)) }
    }

    fun launchRoblox() = context?.let {
        launchRoblox(
            context = it,
            settingsManager = settingsManager,
            fflagsManager = fflagsManager,
            logger = logger
        )
    }
}
