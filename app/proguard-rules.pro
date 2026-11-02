# Proguard rules for Escaner v1

# Android Jetpack Compose
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }

# CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# Application Models
-keep class com.escaner.app.data.model.** { *; }
