# Bark

A lightweight Android logging library that wraps Timber + Logback + SLF4J to provide persistent file-based logging with encrypted log sharing.

## Features

- File-based logging with automatic size-based rotation (5 MB per file, 3 archives)
- Simultaneous logcat and file output
- Optional AES-256 encrypted ZIP export for secure log sharing
- Drop-in Timber integration — no changes to existing log calls
- ProGuard/R8 compatible

## Installation

Bark is distributed via [JitPack](https://jitpack.io).

**`settings.gradle.kts`**
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

**`build.gradle.kts` (app module)**
```kotlin
dependencies {
    implementation("com.github.mobilearmor:bark:0.0.1")
}
```

## Integration

### 1. Create a custom Application class

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Bark.init(applicationContext)
    }
}
```

Register it in `AndroidManifest.xml`:

```xml
<application
    android:name=".App"
    ...>
```

### 2. Log with Timber

After `Bark.init()` is called, all Timber calls are automatically routed to both logcat and the log file:

```kotlin
Timber.d("Debug message")
Timber.i("Info message")
Timber.w("Warning message")
Timber.e("Error message")
Timber.e(exception, "Error with throwable")
```

No additional setup is required. Bark plants a `DebugTree` (for logcat) and a `LogbackTree` (for file output) during initialization.

### 3. Share logs

Call `Bark.shareLog(context)` to package all log files into a ZIP and trigger the system share sheet.

**Without encryption:**
```kotlin
Bark.shareLog(context)
```

**With AES-256 encryption:**
```kotlin
Bark.shareLog(context, "your-password")
```

The recipient needs the same password to open an encrypted ZIP.

## Log file location

Logs are written to the app's cache directory and are automatically removed when the app is uninstalled:

```
{cacheDir}/bark_007/app.log          ← active log
{cacheDir}/bark_007/app.log.1.gz     ← rotated archive
{cacheDir}/bark_007/app.log.2.gz
{cacheDir}/bark_007/app.log.3.gz
{cacheDir}/bark_007_dump/<random>.zip ← encrypted export
```

Log format:
```
2026-03-27 14:22:01.123 [main] DEBUG com.example.MyClass - Your message here
```

## ProGuard / R8

The library ships with embedded ProGuard rules. If you need to add them manually:

```proguard
-keep class com.tree.Bark { *; }
-keep class com.tree.Bark$Companion { *; }
-keepclassmembers class ch.qos.logback.** { *; }
-keepclassmembers class org.slf4j.impl.** { *; }
```

## Requirements

| | Minimum |
|---|---|
| Android SDK | 24 (Android 7.0) |
| Kotlin | 1.9+ |
| Java | 17 |

## Dependencies

| Library | Version |
|---|---|
| Timber | 5.0.1 |
| logback-android | 3.0.0 |
| SLF4J API | 2.0.17 |
| Zip4j | 2.11.5 |

## API reference

```kotlin
object Bark {
    /** Initialize Bark. Call once in Application.onCreate(). */
    fun init(appContext: Context)

    /** Compress all log files into a ZIP and open the share sheet. No encryption. */
    fun shareLog(context: Context)

    /** Compress all log files into an AES-256 encrypted ZIP and open the share sheet. */
    fun shareLog(context: Context, password: String?)
}
```
