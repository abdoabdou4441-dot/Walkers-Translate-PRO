# ═══════════════════════════════════════════════════════════════════════════════
# Online RP Trans — Strict ProGuard / R8 Rules
# Author: Leo Walker
# Purpose: Aggressively obfuscate app logic while protecting runtime-critical
#          classes. Branding and entry points are preserved; everything else
#          is renamed, flattened, and stripped.
# ═══════════════════════════════════════════════════════════════════════════════

# ── Optimization passes ──────────────────────────────────────────────────────
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-allowaccessmodification
-overloadaggressively

# Flatten all obfuscated classes into a single package to maximise confusion
-repackageclasses 'o'

# ── Source map stripping (no line numbers in release crashes) ─────────────────
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Keep annotations needed by Compose and Kotlin reflection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ── Entry points (must survive obfuscation) ──────────────────────────────────
-keep class com.onlinerptrans.MainActivity { *; }
-keep class com.onlinerptrans.service.FloatingOverlayService { *; }

# ── Android framework components ─────────────────────────────────────────────
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keepclassmembers class * extends android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ── Jetpack Compose ──────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-keepclassmembers class * {
    @androidx.compose.ui.tooling.preview.Preview *;
}

# ── Lifecycle / ViewModel / SavedState ───────────────────────────────────────
-keep class androidx.lifecycle.** { *; }
-keep class androidx.savedstate.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }

# ── ML Kit Text Recognition ──────────────────────────────────────────────────
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# ── Coroutines ───────────────────────────────────────────────────────────────
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Kotlin metadata (needed for reflection-based libraries) ──────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ── Remove all logging in release builds ─────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# ── Protect branding string constants from being optimised away ──────────────
# R8 must not inline or remove the branding literals referenced in BrandingFooter
-keepclassmembers class com.onlinerptrans.ui.overlay.TranslationPanelKt {
    private static final java.lang.String *;
}

# ── Serialization safety ─────────────────────────────────────────────────────
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ── Suppress irrelevant warnings ─────────────────────────────────────────────
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**
