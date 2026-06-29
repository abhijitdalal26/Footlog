# Kotlin serialization (type-safe nav routes)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.** { *; }

# Room entities are serialized to/from Firestore
-keep class com.abhijit.footlog.data.entity.** { *; }

# Keep Google Sign-In / Credential Manager classes
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Firebase — keep data classes used in Firestore reads
-keep class com.google.firebase.** { *; }

# MapLibre — JNI classes must be kept
-keep class org.maplibre.android.** { *; }

# Coil
-keep class coil.** { *; }

# Keep Compose runtime
-dontwarn androidx.compose.**
