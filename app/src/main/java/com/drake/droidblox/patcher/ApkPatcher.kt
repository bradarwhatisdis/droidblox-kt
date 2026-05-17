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
        val log = StringBuilder()
        fun emit(step: PatchStep, progress: Float, msg: String) {
            log.appendLine(msg)
            logger.d(TAG, msg)
            onState(PatcherState(step, progress, msg, log = log.toString()))
        }
        fun err(msg: String) {
            log.appendLine(msg)
            logger.e(TAG, msg)
            onState(PatcherState(error = msg, log = log.toString()))
        }

        try {
            emit(PatchStep.DUMPING_APK, 0f, "Getting Roblox APK...")

            val installedApk = getRobloxApkPath()
            if (installedApk == null) {
                err("Roblox is not installed")
                return
            }
            emit(PatchStep.DUMPING_APK, 0.1f, "Roblox APK at: $installedApk")

            val rawApk = File(workDir, "original.apk")
            emit(PatchStep.DUMPING_APK, 0.15f, "Copying APK...")
            File(installedApk).copyTo(rawApk, overwrite = true)
            emit(PatchStep.DUMPING_APK, 0.2f, "APK copied (${rawApk.length()} bytes)")

            if (!rawApk.exists() || rawApk.length() == 0L) {
                err("Failed to copy APK")
                return
            }

            val patched = File(workDir, "patched.apk")
            emit(PatchStep.PATCHING, 0.25f, "Injecting FFlags into APK...")
            injectFFlags(rawApk, patched, allFlagsJson, onState, log)

            val signed = File(workDir, "signed.apk")
            emit(PatchStep.SIGNING, 0.6f, "Signing patched APK...")
            signApk(patched, signed)
            emit(PatchStep.SIGNING, 0.7f, "APK signed (${signed.length()} bytes)")

            log.appendLine("Patch complete! APK saved at: ${signed.absolutePath}")
            logger.d(TAG, "Patch complete! APK saved at: ${signed.absolutePath}")
            onState(PatcherState(PatchStep.DONE, 1f, "APK ready for install at: ${signed.absolutePath}", patchedApk = signed, log = log.toString()))
        } catch (e: Exception) {
            err("Patch failed: ${e.message}")
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

    private fun injectFFlags(input: File, output: File, fflags: String, onState: (PatcherState) -> Unit, log: StringBuilder) {
        val zipFile = ZipFile(input)
        val total = zipFile.size()
        var processed = 0
        ZipOutputStream(FileOutputStream(output)).use { zos ->
            val entries = zipFile.entries()
            var obbFound = false

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                processed++
                val entryName = entry.name

                if (entryName.startsWith(OBB_PREFIX) && entryName.endsWith(OBB_SUFFIX)) {
                    log.appendLine("Patching OBB: $entryName")
                    logger.d(TAG, "Patching OBB: $entryName")
                    val obbBytes = zipFile.getInputStream(entry).readBytes()
                    val modObb = addFFlagsToObb(obbBytes, fflags)
                    val newEntry = ZipEntry(entryName)
                    zos.putNextEntry(newEntry)
                    zos.write(modObb)
                    zos.closeEntry()
                    obbFound = true
                    log.appendLine("FFlags injected into OBB")
                    logger.d(TAG, "FFlags injected into OBB")
                } else {
                    zos.putNextEntry(ZipEntry(entryName))
                    if (!entry.isDirectory) {
                        zipFile.getInputStream(entry).use { it.copyTo(zos) }
                    }
                    zos.closeEntry()
                }

                if (processed % 100 == 0 || processed == total) {
                    val p = 0.25f + (processed.toFloat() / total) * 0.35f
                    val msg = "Processing APK entries: $processed / $total"
                    log.appendLine(msg)
                    onState(PatcherState(PatchStep.PATCHING, p, msg, log = log.toString()))
                }
            }

            if (!obbFound) {
                val w = "OBB entry not found in APK — FFlags will not be embedded!"
                log.appendLine(w)
                logger.w(TAG, w)
            }
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
