package com.tree

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.slf4j.LoggerFactory
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.io.File
import java.util.Random

class Bark {

    companion object {
        @SuppressLint("LogNotTimber")
        @JvmStatic
        fun init(appContext: Context) {
            val context = LoggerFactory.getILoggerFactory() as LoggerContext
            context.reset()

            val configurator = JoranConfigurator()
            configurator.setContext(context)
            // Set the external directory for log files
            context.putProperty("BARK_LOG_DIR", appContext.cacheDir.absolutePath)
            File(appContext.cacheDir, "bark_007").mkdirs() // this is needed otherwise, logback will silently fail
            try {
                configurator.doConfigure(appContext.assets.open("bark-logback.xml"))
            } catch (e: Exception) {
                // we need to log error to Android's Log as we failed to initialize this logger
                // this will avoid error from being swallowed ;)
                android.util.Log.e("Bark", "doConfigure failed", e)
            }
            // Plant debug tree for logcat output
            if(BuildConfig.DEBUG)
                Timber.plant(DebugTree())
            // Plant logback tree for file logging with rotation
            Timber.plant(LogbackTree())
        }

        @JvmStatic
        fun shareLog(context: Context) {
            try {
                val password = "bark-home"
                val zipFile = collectLogsAndShare(context, password) ?: return

                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "application/zip"

                val fileUri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".bark",
                    zipFile
                )
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Logs")
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Logs from " + zipFile.name)

                context.startActivity(Intent.createChooser(shareIntent, "Share logs via"))
            } catch (e: java.lang.Exception) {
                Timber.e(e, "Error sharing zip file")
                Toast.makeText(context, "Error sharing file: " +
                        e.message, Toast.LENGTH_SHORT).show()
            }
        }

        private fun collectLogsAndShare(appContext: Context, password: String? = null) : File? {
        try {
            // Source directory - inferred from logback.xml configuration
            // Logback config uses: ${EXT_DIR}/bark_007/ where EXT_DIR = cacheDir()
            val dumpsDir: File = File(appContext.cacheDir, "bark_007") // this dir name ("bark_007") should match what's inside assets/bark-logback.xml

            // Check if directory exists
            if (!dumpsDir.exists() || !dumpsDir.isDirectory) {
                Timber.e("Dumps directory not found: %s", dumpsDir.absolutePath)
            }

            // Create zip file in app's cache directory
            val random = Random()
            val randomNum = random.nextInt(1000000)  // Generate random number for filename
            val zipFileName = "logs_$randomNum.zip"

            val zipDumpDir = File(appContext.cacheDir, "bark_007_dump") // this dir should match with file_paths.xml config in res/xml dir
            if (zipDumpDir.exists()) zipDumpDir.deleteRecursively()
            zipDumpDir.mkdirs()
            val zipFile = File(zipDumpDir, zipFileName)

            // Create zip
            val success = createZipFromDirectory(dumpsDir, zipFile, password)

            if (success) {
                Timber.i("Logs collected successfully: %s", zipFile.absolutePath)
                return zipFile;
            } else {
                Timber.e("Failed to create zip file")
            }
        } catch (e: java.lang.Exception) {
            Timber.e(e, "Error collecting logs")
        }
            return null;
        }

        private fun createZipFromDirectory(sourceDir: File, zipFile: File, password: String? = null): Boolean {
            return try {
                val files = sourceDir.listFiles()
                if (files == null || files.isEmpty()) {
                    Timber.w("No files found in directory: %s", sourceDir.absolutePath)
                    return false
                }

                val zipParameters = ZipParameters().apply {
                    if (password != null) {
                        isEncryptFiles = true
                        encryptionMethod = EncryptionMethod.AES
                        aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                    }
                }

                val zip = if (password != null) {
                    ZipFile(zipFile, password.toCharArray())
                } else {
                    ZipFile(zipFile)
                }

                files.forEach { entry ->
                    if (entry.isFile) {
                        zip.addFile(entry, zipParameters)
                    } else if (entry.isDirectory) {
                        zip.addFolder(entry, zipParameters)
                    }
                }
                true
            } catch (e: Exception) {
                Timber.e(e, "Error creating zip file")
                false
            }
        }
    }
}