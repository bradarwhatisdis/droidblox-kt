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

    override fun execCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode == 0) stdout.trim() else throw RuntimeException("exit $exitCode: $stderr")
        } catch (e: Exception) {
            throw RuntimeException(e.message ?: "exec failed")
        }
    }
}
