package com.drake.robloxhooks

import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.io.File

class Loader(
    private val context: Context
) {
    companion object {
        private const val TAG = "DBHookLoader"
    }

    private val guard = ThreadLocal<Boolean>()

    init {
        Log.d(TAG, "Hooking")
        fixFFlagsHook()
    }

    private fun fixFFlagsHook() {
        Log.d(TAG, "Hooking fflags")
        XposedBridge.hookAllConstructors(
            File::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (guard.get() == true) return

                    val path = when (param.args.size) {
                        1 -> param.args[0] as? String
                        2 -> {
                            val child = param.args[1] as? String ?: return@beforeHookedMethod
                            val parent = param.args[0]
                            when (parent) {
                                is String -> File(parent, child).path
                                is File -> File(parent, child).path
                                else -> null
                            }
                        }
                        else -> null
                    } ?: return

                    if (!path.endsWith("ClientSettings/ClientAppSettings.json")) return

                    Log.d(TAG, "Redirecting FFlag path: $path -> /data/local/tmp/ClientAppSettings.json")

                    guard.set(true)
                    try {
                        when (param.args.size) {
                            1 -> param.args[0] = "/data/local/tmp/ClientAppSettings.json"
                            2 -> {
                                when (param.args[0]) {
                                    is File -> {
                                        param.args[0] = File("/data/local/tmp")
                                        param.args[1] = "ClientAppSettings.json"
                                    }
                                    is String -> {
                                        param.args[0] = "/data/local/tmp"
                                        param.args[1] = "ClientAppSettings.json"
                                    }
                                }
                            }
                        }
                    } finally {
                        guard.set(false)
                    }
                }
            }
        )
    }
}