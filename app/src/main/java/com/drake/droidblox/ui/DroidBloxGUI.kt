package com.drake.droidblox.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.drake.droidblox.ui.view.views.AboutScreen
import com.drake.droidblox.ui.view.views.FFlagsScreen
import com.drake.droidblox.ui.view.views.IntegrationsScreen
import com.drake.droidblox.ui.view.views.PatcherScreen
import com.drake.droidblox.ui.view.navigation.Routes
import com.drake.droidblox.ui.view.navigation.animatedComposable

@Composable
fun DroidBloxGUI() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.INTEGRATIONS
    ) {
        animatedComposable(Routes.INTEGRATIONS) {
            IntegrationsScreen(
                navController = navController
            )
        }
        animatedComposable(Routes.FFLAGS) {
            FFlagsScreen(
                navController = navController
            )
        }
        animatedComposable(Routes.PATCHER) {
            PatcherScreen(navController = navController)
        }
        animatedComposable(Routes.ABOUT) {
            AboutScreen(
                navController = navController
            )
        }
    }
}