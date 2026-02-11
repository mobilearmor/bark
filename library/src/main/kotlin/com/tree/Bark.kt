package com.tree

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.slf4j.LoggerFactory
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Random
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Bark {

    public fun init(appContext: Context) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.reset()

        val configurator = JoranConfigurator()
        configurator.setContext(context)
        // Set the external directory for log files
        context.putProperty("EXT_DIR", appContext.cacheDir.absolutePath)
        try {
            configurator.doConfigure(appContext.assets.open("bark-logback.xml"))
        } catch (e: Exception) {
            // Fallback to basic configuration if XML fails
            Timber.e(e)
        }
        // Plant debug tree for logcat output
        if(BuildConfig.DEBUG)
            Timber.plant(DebugTree())
        // Plant logback tree for file logging with rotation
        Timber.plant(LogbackTree())
    }

    public fun shareZipFile(context: Context, zipFile: File) {
        try {
            val zipFile = collectLogsAndShare(context) ?: return

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

    private fun collectLogsAndShare(appContext: Context) : File? {
        try {
            // Source directory - inferred from logback.xml configuration
            // Logback config uses: ${EXT_DIR}/dump_007/ where EXT_DIR = cacheDir()
            val dumpsDir: File = File(appContext.cacheDir, "dump_007")

            // Check if directory exists
            if (!dumpsDir.exists() || !dumpsDir.isDirectory) {
                Timber.e("Dumps directory not found: %s", dumpsDir.absolutePath)
            }

            // Create zip file in app's cache directory
            val random = Random()
            val randomNum = random.nextInt(1000000)  // Generate random number for filename
            val zipFileName = "logs_$randomNum.zip"

            val zipFile = File(appContext.cacheDir, zipFileName)

            // Create zip
            val success = createZipFromDirectory(dumpsDir, zipFile)

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

    private fun createZipFromDirectory(sourceDir: File, zipFile: File): Boolean {
        try {
            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    val files = sourceDir.listFiles()
                    if (files == null || files.size == 0) {
                        Timber.w("No files found in directory: %s", sourceDir.absolutePath)
                        return false
                    }

                    val buffer = ByteArray(1024)

                    for (file in files) {
                        if (file.isFile) {
                            FileInputStream(file).use { fis ->
                                val zipEntry = ZipEntry(file.name)
                                zos.putNextEntry(zipEntry)

                                var length: Int
                                while ((fis.read(buffer).also { length = it }) > 0) {
                                    zos.write(buffer, 0, length)
                                }

                                zos.closeEntry()
                            }
                        } else if (file.isDirectory) {
                            // Recursively add directory contents
                            addDirectoryToZip(file, file.name, zos, buffer)
                        }
                    }
                    return true
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Error creating zip file")
            return false
        }
    }

    @Throws(IOException::class)
    private fun addDirectoryToZip(
        directory: File,
        parentPath: String?,
        zos: ZipOutputStream,
        buffer: ByteArray
    ) {
        val files = directory.listFiles() ?: return

        for (file in files) {
            val filePath = parentPath + "/" + file.name

            if (file.isFile) {
                FileInputStream(file).use { fis ->
                    val zipEntry = ZipEntry(filePath)
                    zos.putNextEntry(zipEntry)

                    var length: Int
                    while ((fis.read(buffer).also { length = it }) > 0) {
                        zos.write(buffer, 0, length)
                    }

                    zos.closeEntry()
                }
            } else if (file.isDirectory) {
                addDirectoryToZip(file, filePath, zos, buffer)
            }
        }
    }
}