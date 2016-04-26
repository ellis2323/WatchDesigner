# Global
-dontobfuscate
-keepattributes SourceFile, LineNumberTable
-dontoptimize

# Picasso
-dontwarn com.squareup.okhttp.**

# OkHttp (Okio)
-dontwarn okio.**

-keepclasseswithmembers public class com.iopixel.library.DateTool
