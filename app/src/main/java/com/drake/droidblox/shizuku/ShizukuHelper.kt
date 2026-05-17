package com.drake.droidblox.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import rikka.shizuku.Shizuku

object ShizukuHelper {

    private var writer: IFileWriter? = null
    private var connection: ServiceConnection? = null
    private var args: Shizuku.UserServiceArgs? = null
    private var _bound = false

    val isBound: Boolean get() = _bound && writer != null

    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(0)
        } catch (_: Exception) { }
    }

    fun bind(context: Context) {
        if (_bound || !isAvailable() || !hasPermission()) return
        val a = Shizuku.UserServiceArgs(
            ComponentName(context, FileWriterService::class.java)
        ).processNameSuffix("filewriter").daemon(false).version(1)
        args = a
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                writer = IFileWriter.Stub.asInterface(service)
                _bound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                writer = null
                _bound = false
            }
        }
        try {
            Shizuku.bindUserService(a, connection!!)
        } catch (_: Exception) { }
    }

    fun unbind() {
        writer = null
        _bound = false
        val conn = connection ?: return
        val a = args ?: return
        try {
            Shizuku.unbindUserService(a, conn, false)
        } catch (_: Exception) { }
        connection = null
        args = null
    }

    fun writeFile(path: String, content: String): Boolean {
        val w = writer ?: return false
        return try {
            w.writeFile(path, content)
        } catch (_: Exception) {
            false
        }
    }

}
