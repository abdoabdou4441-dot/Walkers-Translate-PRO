# Online RP Trans — Native Android App

أداة ترجمة عائمة لمتابعي لعبة **Online RP Mobile**، تترجم النصوص الروسية إلى العربية/الدارجة المغربية بدون مغادرة اللعبة.

---

## Project Structure

```
online-rp-trans/
├── app/
│   ├── src/main/
│   │   ├── java/com/onlinerptrans/
│   │   │   ├── MainActivity.kt                  # Entry point, RTL layout
│   │   │   ├── MainViewModel.kt                 # Service state + permission logic
│   │   │   ├── service/
│   │   │   │   ├── FloatingOverlayService.kt    # Foreground service + WindowManager overlay
│   │   │   │   ├── ClipboardMonitor.kt          # Watches clipboard for Russian text
│   │   │   │   └── TranslationManager.kt        # ML Kit RU→AR translation wrapper
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt                 # Neon Cyan / Gaming Black palette
│   │   │   │   │   ├── Type.kt                  # Typography
│   │   │   │   │   └── Theme.kt                 # MaterialTheme dark scheme
│   │   │   │   ├── overlay/
│   │   │   │   │   ├── FloatingBubble.kt        # Animated draggable bubble
│   │   │   │   │   └── TranslationPanel.kt      # Bottom-sheet overlay (Translate + Dictionary tabs)
│   │   │   │   └── screen/
│   │   │   │       └── MainScreen.kt            # Main app screen (Start/Stop + how-to)
│   │   │   └── data/
│   │   │       └── GamerDictionary.kt           # 60+ RP terms (RU → AR + Darija)
│   │   ├── res/
│   │   │   ├── drawable/
│   │   │   │   ├── ic_bubble.xml                # Neon cyan T-icon (vector)
│   │   │   │   └── ic_notification.xml          # Notification bell icon
│   │   │   ├── values/
│   │   │   │   ├── strings.xml                  # Arabic UI strings
│   │   │   │   └── themes.xml                   # App theme (no title bar)
│   │   │   ├── xml/
│   │   │   │   └── locales_config.xml           # AR + RU locale support
│   │   │   └── mipmap-*/
│   │   │       └── ic_launcher*.xml             # Adaptive launcher icons
│   │   └── AndroidManifest.xml                  # All permissions declared
│   ├── build.gradle.kts                         # App-level Gradle config
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml                       # Version catalog
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts                             # Project-level
├── settings.gradle.kts
└── gradle.properties
```

---

## Requirements

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog 2023.1.1+ |
| Gradle | 8.9 |
| Android Gradle Plugin | 8.5.2 |
| Kotlin | 2.0.21 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| JDK | 17 |

---

## Key Dependencies

| Library | Purpose |
|---------|---------|
| Jetpack Compose BOM 2024.08 | All UI |
| ML Kit Translate 17.0.3 | On-device RU→AR translation |
| Material3 | Gaming dark theme components |
| Coroutines 1.8.1 | Async translation + clipboard flow |

---

## Permissions (AndroidManifest.xml)

| Permission | Purpose |
|-----------|---------|
| `SYSTEM_ALERT_WINDOW` | Floating bubble over all apps |
| `FOREGROUND_SERVICE` | Keep translation service alive |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Required Android 14+ for overlay FGS |
| `POST_NOTIFICATIONS` | Persistent notification (Android 13+) |
| `INTERNET` | ML Kit model download (first run only) |
| `WAKE_LOCK` | Keep CPU alive during translation |

---

## Build Instructions

### 1 — Clone / open project

Open the `android-project/` folder in **Android Studio**.

### 2 — First run — sync dependencies

```bash
./gradlew dependencies
```

### 3 — Debug APK

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### 4 — Release APK

Create a keystore (if you don't have one):

```bash
keytool -genkey -v \
  -keystore release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias onlinerptrans
```

Configure signing in `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile     = file("release.jks")
        storePassword = "YOUR_STORE_PASSWORD"
        keyAlias      = "onlinerptrans"
        keyPassword   = "YOUR_KEY_PASSWORD"
    }
}
```

Then build:

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### 5 — Install on device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## First-Run Flow

1. Launch the app → it asks for **"Display over other apps"** permission
2. Tap **"منح الإذن"** → Android Settings opens → enable the toggle
3. Return to the app → tap **"تشغيل الخدمة"**
4. The neon cyan bubble appears over your home screen / game
5. Open Online RP Mobile → copy any Russian text → Arabic translation appears instantly
6. Tap the bubble to open the full panel with Translation + Gamer Dictionary tabs

---

## ML Kit Model Download

On first use, ML Kit downloads the **Russian + Arabic** on-device translation models (~30 MB each). This requires an internet connection once. After that, translation works fully **offline** with low latency.

---

## Architecture Notes

- **FloatingOverlayService** implements `LifecycleOwner`, `ViewModelStoreOwner`, and `SavedStateRegistryOwner` so `ComposeView` works correctly outside an `Activity`.
- **ClipboardMonitor** uses `callbackFlow` and `OnPrimaryClipChangedListener` — fires even when the app is backgrounded.
- **TranslationManager** wraps ML Kit's `Tasks` API with Kotlin `suspendCancellableCoroutine` for clean coroutine integration.
- All UI strings are in Arabic (`strings.xml`) with RTL layout forced via `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`.
