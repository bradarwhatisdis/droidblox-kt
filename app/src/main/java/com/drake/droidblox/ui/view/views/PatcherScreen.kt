package com.drake.droidblox.ui.view.views

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.drake.droidblox.patcher.PatchStep
import com.drake.droidblox.ui.components.BasicScreen
import com.drake.droidblox.ui.view.viewmodels.PatcherViewModel

@Composable
fun PatcherScreen(
    navController: NavController? = null,
    viewModel: PatcherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    BasicScreen(
        name = "APK Patcher",
        navController = navController
    ) { _ ->
        Text(
            "Patch the official Roblox APK with your Fast Flags. " +
                    "This embeds FFlags into the APK, signs it, and saves it.\n\n" +
                    "⚠️  No root / Shizuku needed.\n" +
                    "⚠️  The patched APK has a different signature — " +
                    "you must uninstall official Roblox first.\n" +
                    "⚠️  Updates must be done through DroidBlox.",
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
                    Text("✅  Patch Complete!", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            state.patchedApk?.let { apk ->
                Button(
                    onClick = {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
                        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                            data = uri
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📲  Install APK")
                }
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { viewModel.reset() }) {
                Text("Done")
            }
        }

        if (state.step == PatchStep.IDLE && state.error == null) {
            Button(
                onClick = { viewModel.startPatching() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚀  Start Patching")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Patch Log", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                state.log.ifBlank { "— no output yet —" },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, state.log)
                }
                context.startActivity(Intent.createChooser(share, "Export Patch Log"))
            },
            enabled = state.log.isNotBlank()
        ) {
            Text("📤  Export Log")
        }
    }
}
