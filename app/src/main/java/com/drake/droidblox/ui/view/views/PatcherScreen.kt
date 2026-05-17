package com.drake.droidblox.ui.view.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.drake.droidblox.patcher.PatchStep
import com.drake.droidblox.ui.components.BasicScreen
import com.drake.droidblox.ui.view.navigation.Routes
import com.drake.droidblox.ui.view.viewmodels.PatcherViewModel

@Composable
fun PatcherScreen(
    navController: NavController? = null,
    viewModel: PatcherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    BasicScreen(
        name = "APK Patcher",
        navController = navController
    ) { _ ->
        Text(
            "Patch the official Roblox APK with your Fast Flags. " +
                    "This embeds FFlags into the APK, signs it, and installs it.\n\n" +
                    "⚠️  Requires Shizuku.\n" +
                    "⚠️  The patched APK has a different signature — " +
                    "official Roblox will be replaced. Updates must be done through DroidBlox.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        state.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        if (state.step != PatchStep.IDLE && state.step != PatchStep.DONE) {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                state.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
        }

        if (state.step == PatchStep.DONE) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✅  Patched & Installed!", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { viewModel.reset() }) {
                Text("Done")
            }
        }

        if (state.step == PatchStep.IDLE && state.error == null) {
            Button(
                onClick = { viewModel.startPatching() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚀  Patch & Install")
            }
        }
    }
}
