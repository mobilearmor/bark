# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Issue #229
# https://github.com/tony19/logback-android/
-keepclassmembers class ch.qos.logback.core.rolling.helper.* { <init>(); }

# The following rules should only be used if you plan to keep
# the logging calls in your released app.
-keepclassmembers class ch.qos.logback.** { *; }
-keepclassmembers class org.slf4j.impl.** { *; }
-keepattributes *Annotation*