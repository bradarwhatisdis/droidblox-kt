package com.drake.droidblox.shizuku

import rikka.shizuku.Shizuku
import java.io.OutputStream

object ShizukuHelper {
    fun isAvailable(): Boolean = try {
        Shizuku.pingBinder()
    } catch (_: Exception) {
        false
    }

    fun writeFile(path: String, content: String): Boolean {
        return try {
            val dir = path.substringBeforeLast("/")
            val script = "mkdir -p \"$dir\" && cat > \"$path\""
            val process = Shizuku.newProcess(arrayOf("sh", "-c", script))
            val stdin: OutputStream = process.outputStream
            stdin.write(content.toByteArray(Charsets.UTF_8))
            stdin.close()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }
}
