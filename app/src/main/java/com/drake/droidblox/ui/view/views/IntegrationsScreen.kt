package com.drake.droidblox.ui.view.views

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.drake.droidblox.logger.TestLogger
import com.drake.droidblox.sharedprefs.FastFlagsManager
import com.drake.droidblox.sharedprefs.SettingsManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drake.droidblox.ui.components.BasicScreen
import com.drake.droidblox.ui.components.ExtendedButton
import com.drake.droidblox.ui.components.ExtendedSwitch
import com.drake.droidblox.ui.components.SectionText
import com.drake.droidblox.ui.components.TitleWithSubtitle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import com.drake.droidblox.ui.view.viewmodels.IntegrationsScreenVM
import com.drake.droidblox.ui.view.viewmodels.ShizukuConnectionState

@Composable
fun IntegrationsScreen(
    viewModel: IntegrationsScreenVM = hiltViewModel(),
    navController: NavController? = null
) {

    BasicScreen("Integrations", navController) {
        val status = viewModel.robloxStatus.value

        LaunchedEffect(Unit) {
            viewModel.refreshShizukuState()
        }

        if (status != null) {
            if (status.installed) {
                TitleWithSubtitle(
                    "🎮  Roblox (v${status.version ?: "?"})",
                    "Installed"
                )
            } else {
                TitleWithSubtitle(
                    "🎮  Roblox",
                    "Not installed — install Roblox from the Play Store first"
                )
            }
        }
        ExtendedButton(
            "Launch Roblox",
            "Start playing Roblox",
            enabled = status?.installed == true
        ) {
            viewModel.launchRoblox()
        }
        SectionText("System", "⚙️")
        val shizuku = viewModel.shizukuState.value
        val connState = viewModel.shizukuConnectionState.value
        if (!shizuku.available) {
            TitleWithSubtitle("🛡️  Shizuku", "Not detected — install Shizuku from GitHub")
        } else if (connState == ShizukuConnectionState.Requesting || connState == ShizukuConnectionState.Connecting) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                TitleWithSubtitle("🛡️  Shizuku", "Connecting\u2026")
            }
        } else if (connState == ShizukuConnectionState.Failed) {
            TitleWithSubtitle("🛡️  Shizuku", "Connection failed — tap to retry")
            ExtendedButton("Retry Shizuku", "Try connecting again") {
                viewModel.requestAndConnectShizuku()
            }
        } else if (!shizuku.hasPermission) {
            TitleWithSubtitle("🛡️  Shizuku", "Permission not granted")
            ExtendedButton("Grant Shizuku permission", "Required for Shizuku integration") {
                viewModel.requestAndConnectShizuku()
            }
        } else if (!shizuku.bound) {
            TitleWithSubtitle("🛡️  Shizuku", "Permission granted, tap to connect")
            ExtendedButton("Connect Shizuku service", "Bind to the remote file writer") {
                viewModel.connectShizuku()
            }
        } else {
            ExtendedSwitch(
                "Shizuku integration",
                "Writes FFlags to /data/local/tmp/",
                viewModel.settingsManager.useShizuku
            ) { enabled ->
                viewModel.settingsManager.useShizuku = enabled
            }
        }
        SectionText("Activity tracking", "📊")
        ExtendedSwitch(
            "Enable activity tracking",
            "Allow DroidBlox to detect what Roblox game you're playing.",
            viewModel.settingsManager.enableActivityTracking
        ) { viewModel.settingsManager.enableActivityTracking = it }
        ExtendedSwitch(
            "Query server location",
            "When in game, you'll be able to see where your server is located",
            viewModel.settingsManager.showServerLocation
        ) { viewModel.settingsManager.showServerLocation = it }
        SectionText("Discord Rich Presence", "💬")
        //LoginToDiscordButton(navController)
        ExtendedSwitch(
            "Show game activity",
            "The Roblox game you're playing will be show on your Discord profile.",
            viewModel.settingsManager.showGameActivity
        ) { viewModel.settingsManager.showGameActivity = it }
        ExtendedSwitch(
            "Allow activity joining",
            "Allows for anybody to join the game you're currently in through your Discord profile.",
            viewModel.settingsManager.allowActivityJoining
        ) { viewModel.settingsManager.allowActivityJoining = it }
        ExtendedSwitch(
            "Show Roblox account",
            "Shows the Roblox account you're playing with on your Discord profile.",
            viewModel.settingsManager.showRobloxUser
        ) { viewModel.settingsManager.showRobloxUser = it }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
internal fun PreviewIntegrationsScreen() {
    CompositionLocalProvider(
        LocalContext provides LocalContext.current
    ) {
        val logger = TestLogger
        val settingsManager = SettingsManager(
            logger = logger,
            context = LocalContext.current
        )
        IntegrationsScreen(IntegrationsScreenVM(
            context = null,
            settingsManager = settingsManager,
            fflagsManager = FastFlagsManager(
                logger = logger,
                context = LocalContext.current
            ),
            logger = logger
        ))
    }
}