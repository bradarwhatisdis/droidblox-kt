package com.drake.droidblox.patcher

import java.io.File

enum class PatchStep {
    IDLE,
    DUMPING_APK,
    DOWNLOADING,
    PATCHING,
    SIGNING,
    DONE
}

data class PatcherState(
    val step: PatchStep = PatchStep.IDLE,
    val progress: Float = 0f,
    val message: String = "",
    val error: String? = null,
    val patchedApk: File? = null,
    val log: String = ""
)
