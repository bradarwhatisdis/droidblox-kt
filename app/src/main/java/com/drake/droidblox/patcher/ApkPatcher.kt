package com.drake.droidblox.patcher

import android.content.Context
import com.android.apksig.ApkSigner
import com.drake.droidblox.logger.Logger
import com.drake.droidblox.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApkPatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger
) {
    companion object {
        private const val TAG = "DBApkPatcher"
        private const val KEYSTORE_PW = "droidblox"
        private const val KEY_PW = "droidblox"
        private const val KEY_ALIAS = "droidblox"
        private const val OBB_PREFIX = "assets/main."
        private const val OBB_SUFFIX = ".com.roblox.client.obb"
        private const val FFLAGS_PATH = "ClientSettings/ClientAppSettings.json"
    }

    private val workDir: File get() = File(context.filesDir, "patcher").also { it.mkdirs() }

    suspend fun run(allFlagsJson: String, onState: (PatcherState) -> Unit) {
        try {
            onState(PatcherState(PatchStep.DUMPING_APK, 0f, "Getting Roblox APK..."))

            val installedApk = getRobloxApkPath()
            if (installedApk == null) {
                onState(PatcherState(error = "Roblox is not installed"))
                return
            }
            logger.d(TAG, "Roblox APK at: $installedApk")

            val rawApk = File(workDir, "original.apk")
            onState(PatcherState(PatchStep.DUMPING_APK, 0.2f, "Copying Roblox APK..."))

            File(installedApk).copyTo(rawApk, overwrite = true)

            if (!rawApk.exists() || rawApk.length() == 0L) {
                onState(PatcherState(error = "Failed to copy APK"))
                return
            }
            logger.d(TAG, "Roblox APK at: $installedApk")

            val rawApk = File(workDir, "original.apk")
            onState(PatcherState(PatchStep.DUMPING_APK, 0.2f, "Copying APK via Shizuku..."))

            com.drake.droidblox.shizuku.ShizukuHelper.runCommand("cp \"$installedApk\" \"${rawApk.absolutePath}\"")

            if (!rawApk.exists() || rawApk.length() == 0L) {
                onState(PatcherState(error = "Failed to copy APK. Ensure Shizuku is connected."))
                return
            }

            val patched = File(workDir, "patched.apk")
            onState(PatcherState(PatchStep.PATCHING, 0.4f, "Injecting FFlags into APK..."))

            injectFFlags(rawApk, patched, allFlagsJson)

            val signed = File(workDir, "signed.apk")
            onState(PatcherState(PatchStep.SIGNING, 0.6f, "Signing patched APK..."))

            signApk(patched, signed)

            onState(PatcherState(PatchStep.INSTALLING, 0.8f, "Installing patched APK..."))

            val installResult = com.drake.droidblox.shizuku.ShizukuHelper.runCommand("pm install -r -t -d \"${signed.absolutePath}\" 2>&1")
            logger.d(TAG, "Install result: $installResult")

            onState(PatcherState(
                PatchStep.DONE, 1f,
                "Patched APK installed!\nNote: Roblox now has a different signature. Updates must be done through DroidBlox.",
                patchedApk = signed
            ))
        } catch (e: Exception) {
            logger.e(TAG, "Patch failed: ${e.message}")
            onState(PatcherState(error = e.message ?: "Unknown error"))
        }
    }

    private fun getRobloxApkPath(): String? {
        return try {
            val info = context.packageManager.getApplicationInfo("com.roblox.client", 0)
            info.sourceDir
        } catch (_: Exception) {
            null
        }
    }

    private fun injectFFlags(input: File, output: File, fflags: String) {
        val zipFile = ZipFile(input)
        ZipOutputStream(FileOutputStream(output)).use { zos ->
            val entries = zipFile.entries()
            var obbFound = false

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()

                if (entry.name.startsWith(OBB_PREFIX) && entry.name.endsWith(OBB_SUFFIX)) {
                    logger.d(TAG, "Patching OBB: ${entry.name}")
                    val obbBytes = zipFile.getInputStream(entry).readBytes()
                    val modObb = addFFlagsToObb(obbBytes, fflags)
                    val newEntry = ZipEntry(entry.name)
                    zos.putNextEntry(newEntry)
                    zos.write(modObb)
                    zos.closeEntry()
                    obbFound = true
                } else {
                    zos.putNextEntry(ZipEntry(entry.name))
                    if (!entry.isDirectory) {
                        zipFile.getInputStream(entry).use { it.copyTo(zos) }
                    }
                    zos.closeEntry()
                }
            }

            if (!obbFound) logger.w(TAG, "OBB entry not found in APK")
        }
        zipFile.close()
    }

    private fun addFFlagsToObb(obbBytes: ByteArray, fflags: String): ByteArray {
        val tempIn = File(workDir, "in.obb")
        val tempOut = File(workDir, "out.obb")
        tempIn.writeBytes(obbBytes)

        val zis = ZipFile(tempIn)
        ZipOutputStream(FileOutputStream(tempOut)).use { zos ->
            for (entry in zis.entries()) {
                zos.putNextEntry(ZipEntry(entry.name))
                if (!entry.isDirectory) zis.getInputStream(entry).use { it.copyTo(zos) }
                zos.closeEntry()
            }
            zos.putNextEntry(ZipEntry(FFLAGS_PATH))
            zos.write(fflags.toByteArray())
            zos.closeEntry()
        }
        zis.close()

        val result = tempOut.readBytes()
        tempIn.delete(); tempOut.delete()
        return result
    }

    private fun signApk(input: File, output: File) {
        val ks = KeyStore.getInstance("PKCS12")
        context.resources.openRawResource(R.raw.apksign_keystore).use { ks.load(it, KEYSTORE_PW.toCharArray()) }

        val privateKey = ks.getKey(KEY_ALIAS, KEY_PW.toCharArray()) as java.security.PrivateKey
        val cert = ks.getCertificate(KEY_ALIAS) as X509Certificate

        val signerConfig = ApkSigner.SignerConfig.Builder("CERT", privateKey, listOf(cert)).build()

        ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(input)
            .setOutputApk(output)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(false)
            .build()
            .sign()

        logger.d(TAG, "APK signed")
    }
}
