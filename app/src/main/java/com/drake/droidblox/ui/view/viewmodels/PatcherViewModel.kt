package com.drake.droidblox.ui.view.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.droidblox.logger.Logger
import com.drake.droidblox.patcher.ApkPatcher
import com.drake.droidblox.patcher.PatchStep
import com.drake.droidblox.patcher.PatcherState
import com.drake.droidblox.sharedprefs.FastFlagsManager
import com.drake.droidblox.shizuku.ShizukuHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class PatcherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val patcher: ApkPatcher,
    private val fflagsManager: FastFlagsManager,
    private val logger: Logger
) : ViewModel() {

    private val _state = MutableStateFlow(PatcherState())
    val state = _state.asStateFlow()

    fun startPatching() {
        val raw = fflagsManager.rawFFlags
        if (raw.isNullOrBlank()) {
            _state.value = PatcherState(error = "No FFlags configured. Go to Fast Flags screen first.")
            return
        }

        _state.value = PatcherState(PatchStep.IDLE, progress = 0f, message = "Connecting to Shizuku...")
        viewModelScope.launch {
            patcher.run(raw, execCommand = { cmd ->
                ensureConnected()
                try {
                    ShizukuHelper.runCommand(cmd)
                } catch (e: Exception) {
                    logger.e("Shizuku exec failed, retrying: ${e.message}")
                    ensureConnected()
                    ShizukuHelper.runCommand(cmd)
                }
            }) { newState ->
                _state.value = newState
            }
        }
    }

    private suspend fun ensureConnected() {
        if (ShizukuHelper.isBound) return
        if (!ShizukuHelper.isAvailable() || !ShizukuHelper.hasPermission())
            throw IllegalStateException("Shizuku unavailable or permission not granted")
        suspendCancellableCoroutine<Unit> { cont ->
            ShizukuHelper.bind(context) { ok ->
                if (ok) cont.resume(Unit) else cont.resumeWith(Exception("Shizuku bind failed"))
            }
        }
    }

    fun reset() {
        _state.value = PatcherState()
    }
}
