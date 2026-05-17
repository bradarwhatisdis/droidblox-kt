package com.drake.droidblox.ui.view.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.drake.droidblox.ui.view.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    screenName: String,
    navController: NavController? = null,
    content: @Composable ((scope: CoroutineScope, drawerState: DrawerState) -> Unit)
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "⚡ DroidBlox",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Roblox bootstrapper",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("🔗  Integrations") },
                    selected = screenName == Routes.INTEGRATIONS,
                    onClick = {
                        navController?.navigate(Routes.INTEGRATIONS)
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("⚙️  Fast Flags") },
                    selected = screenName == Routes.FFLAGS,
                    onClick = {
                        navController?.navigate(Routes.FFLAGS)
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("📄  About") },
                    selected = screenName == Routes.ABOUT,
                    onClick = {
                        navController?.navigate(Routes.ABOUT)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) { content(scope, drawerState) }
}
