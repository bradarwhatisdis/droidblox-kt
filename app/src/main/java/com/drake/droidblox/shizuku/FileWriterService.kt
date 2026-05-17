package com.drake.droidblox.shizuku

import java.io.File

class FileWriterService : IFileWriter.Stub() {

    override fun writeFile(path: String, content: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }
}
