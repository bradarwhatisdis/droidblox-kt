package com.drake.droidblox.ui.view.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.droidblox.patcher.ApkPatcher
import com.drake.droidblox.patcher.PatcherState
import com.drake.droidblox.sharedprefs.FastFlagsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatcherViewModel @Inject constructor(
    private val patcher: ApkPatcher,
    private val fflagsManager: FastFlagsManager
) : ViewModel() {

    private val _state = MutableStateFlow(PatcherState())
    val state = _state.asStateFlow()

    fun startPatching() {
        val raw = fflagsManager.rawFFlags
        if (raw.isNullOrBlank()) {
            _state.value = PatcherState(error = "No FFlags configured. Go to Fast Flags screen first.")
            return
        }

        viewModelScope.launch {
            patcher.run(raw) { newState ->
                _state.value = newState
            }
        }
    }

    fun reset() {
        _state.value = PatcherState()
    }
}
