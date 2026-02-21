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

# Jetpack Compose Rules
-keepclassmembers class androidx.compose.runtime.Composer { *; }
-keepclassmembers class androidx.compose.runtime.PausableMonotonicFrameClock { *; }
-keepclassmembers class androidx.compose.runtime.Recomposer { *; }
-keepclassmembers class androidx.compose.runtime.SlotTable { *; }
-keepclassmembers class androidx.compose.runtime.CompositionImpl { *; }
-keepclassmembers class androidx.compose.runtime.Applier { *; }
-keepclassmembers class androidx.compose.runtime.CompositingStrategy { *; }
-keepclassmembers class androidx.compose.runtime.Composer$Companion { *; }
-keepclassmembers class androidx.compose.runtime.Composition { *; }
-keepclassmembers class androidx.compose.runtime.CompositionContext { *; }
-keepclassmembers class androidx.compose.runtime.CompositionLocal { *; }
-keepclassmembers class androidx.compose.runtime.Effects { *; }
-keepclassmembers class androidx.compose.runtime.MonotonicFrameClock { *; }
-keepclassmembers class androidx.compose.runtime.Snapshot { *; }
-keepclassmembers class androidx.compose.runtime.snapshots.** { *; }
