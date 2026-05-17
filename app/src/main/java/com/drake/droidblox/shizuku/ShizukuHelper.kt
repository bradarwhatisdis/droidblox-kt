package com.drake.droidblox.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

object ShizukuHelper {

    private var writer: IFileWriter? = null
    private var connection: ServiceConnection? = null
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
        val args = Shizuku.UserServiceArgs(
            ComponentName(context, FileWriterService::class.java)
        ).processNameSuffix("filewriter").daemon(false).version(1)
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
            Shizuku.bindUserService(args, connection!!)
        } catch (_: Exception) { }
    }

    fun unbind() {
        writer = null
        _bound = false
        val conn = connection ?: return
        try {
            Shizuku.unbindUserService(
                Shizuku.UserServiceArgs(
                    ComponentName("com.drake.droidblox", FileWriterService::class.java.name)
                ),
                conn
            )
        } catch (_: Exception) { }
        connection = null
    }

    fun writeFile(path: String, content: String): Boolean {
        val w = writer ?: return false
        return try {
            w.writeFile(path, content)
        } catch (_: Exception) {
            false
        }
    }

    class ShizukuReceiver : ShizukuProvider() {
        override fun onPermissionResult(code: Int, grantResult: Int) {
            super.onPermissionResult(code, grantResult)
        }
    }
}
