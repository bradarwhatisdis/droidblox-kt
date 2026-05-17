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
import com.drake.droidblox.ui.components.BasicScreen
import com.drake.droidblox.ui.components.ExtendedButton
import com.drake.droidblox.ui.components.ExtendedSwitch
import com.drake.droidblox.ui.components.SectionText
import com.drake.droidblox.ui.components.TitleWithSubtitle
import com.drake.droidblox.ui.view.viewmodels.IntegrationsScreenVM

@Composable
fun IntegrationsScreen(
    viewModel: IntegrationsScreenVM = hiltViewModel(),
    navController: NavController? = null
) {

    BasicScreen("Integrations", navController) {
        val status = viewModel.robloxStatus.value
        if (status != null) {
            if (status.installed) {
                TitleWithSubtitle(
                    "Roblox (v${status.version ?: "?"})",
                    "Installed"
                )
            } else {
                TitleWithSubtitle(
                    "Roblox",
                    "Not installed — install Roblox from the Play Store first"
                )
            }
        }
        ExtendedButton(
            if (status?.installed == true) "Launch Roblox" else "Roblox not installed",
            "Start playing Roblox"
        ) {
            if (status?.installed == true) viewModel.launchRoblox()
        }
        SectionText("Activity tracking")
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
        SectionText("Discord Rich Presence")
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
        IntegrationsScreen(IntegrationsScreenVM(
            context = null,
            settingsManager = SettingsManager(
                logger = logger,
                context = LocalContext.current
            ),
            fflagsManager = FastFlagsManager(
                logger = logger,
                context = LocalContext.current
            )
        ))
    }
}