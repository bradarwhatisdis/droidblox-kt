package com.drake.droidblox.logger

import android.content.Context
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val MAX_LOG_SIZE = 1024 * 1024

class FileLogger(context: Context) : Logger {
    private val logDir = File(context.filesDir, "logs")
    private val logFile = File(logDir, "droidblox.log")

    init {
        if (!logDir.exists()) logDir.mkdirs()
        if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
            logFile.renameTo(File(logDir, "droidblox_old.log"))
        }
    }

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    private fun log(level: String, tag: String, message: String) {
        val timestamp = LocalDateTime.now().format(formatter)
        val line = "[$timestamp] [$level] [$tag] $message"
        logFile.appendText("$line\n")
    }

    override fun d(tag: String, message: String) = log("D", tag, message)
    override fun i(tag: String, message: String) = log("I", tag, message)
    override fun w(tag: String, message: String) = log("W", tag, message)
    override fun e(tag: String, message: String) = log("E", tag, message)
}
