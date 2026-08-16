# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/.../android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Keep SceneView models & Filament native bindings
-keep class io.github.sceneview.** { *; }
-keep class com.google.android.filament.** { *; }

# Keep Kotlinx Serialization models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
