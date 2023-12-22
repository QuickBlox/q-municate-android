# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions

# GSON @Expose annotation
-keepattributes *Annotation*

# Quickblox sdk
-keep class com.quickblox.** { *; }

# Smack xmpp library
-keep class org.jxmpp.** { *; }
-keep class org.jivesoftware.** { *; }
-dontwarn org.jivesoftware.**

# Glide
-keep class com.bumptech.** { *; }

# Google gms
-keep class com.google.android.gms.** { *; }

# Json
-keep class org.json.** { *; }