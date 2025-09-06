# YukiHookAPI Universal Setup Guide

This guide provides comprehensive instructions for setting up YukiHookAPI across all modules in your project.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Version Catalog Setup](#version-catalog-setup)
3. [Module Configuration](#module-configuration)
4. [Basic Xposed Module Setup](#basic-xposed-module-setup)
5. [Advanced Configuration](#advanced-configuration)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

- Android Studio Flamingo (2022.2.1) or later
- Gradle 8.0+
- Android Gradle Plugin 8.0.0+
- Kotlin 1.8.0+
- JDK 17+

## Version Catalog Setup

Your `libs.versions.toml` should include these YukiHookAPI dependencies (no Gradle plugin required):

```toml
[versions]
yukihookapi = "1.3.0"

[libraries]
# Core YukiHookAPI (naming aligned with project catalog)
yukihook-core  = { group = "com.highcapable.yukihookapi", name = "api",        version.ref = "yukihookapi" }
yukihook-ksp   = { group = "com.highcapable.yukihookapi", name = "ksp-xposed",  version.ref = "yukihookapi" }
yukihook-prefs = { group = "com.highcapable.yukihookapi", name = "prefs",       version.ref = "yukihookapi" }

# Optional bridge (only if you actually use it)
# yukihook-bridge = { group = "com.highcapable.yukihookapi", name = "bridge", version.ref = "yukihookapi" }

# Xposed Framework API (compileOnly)
xposed-api = { group = "de.robv.android.xposed", name = "api", version = "82" }
```

## Module Configuration

### For Xposed Modules

Use only standard Android/Kotlin plugins plus KSP (no YukiHook plugin exists / required):

```kotlin
plugins {
    id("com.android.application") // or id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    // Hilt / Serialization / etc as needed
}

android {
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    // Xposed API available at compile time only (provided by LSPosed at runtime)
    compileOnly(libs.xposed.api)

    // YukiHookAPI core + prefs + KSP processor
    implementation(libs.yukihook.core)
    implementation(libs.yukihook.prefs)
    ksp(libs.yukihook.ksp)

    // Optional bridge if enabled in catalog
    // implementation(libs.yukihook.bridge)
}
```

## Basic Xposed Module Setup

1. Create your module entry class:

```kotlin
@ModuleEntry
class YourModuleEntry : IModuleEntry {
    override fun onHook() = YukiHookAPI.encase {
        // Your hook logic here
    }
}
```

2. Update `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.your.package.name">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.YourApp">
        
        <meta-data
            android:name="xposedmodule"
            android:value="true" />
            
        <meta-data
            android:name="xposeddescription"
            android:value="Your module description" />
            
        <meta-data
            android:name="xposedminversion"
            android:value="93" />
            
    </application>
</manifest>
```

## Advanced Configuration

### For Multi-Module Projects

For non-Xposed library modules (utility or feature modules) that only share hook-related utilities (and don't need to declare an Xposed entry):

```kotlin
dependencies {
    // Provide hook-related helpers without the Xposed API if not hooking directly
    implementation(libs.yukihook.core)
    implementation(libs.yukihook.prefs)
    // KSP only if you use annotations that generate code in that module
    // ksp(libs.yukihook.ksp)
}
```

### ProGuard/R8 Rules

Add to your `proguard-rules.pro`:

```
# YukiHookAPI
-keep class com.highcapable.yukihookapi.** { *; }
-keepclassmembers class * extends com.highcapable.yukihookapi.hook.xposed.prefs.YukiHookModulePrefs {
    <init>(...);
}
```

### Offline Fallback (Repository Outage)
If Maven hosting for YukiHookAPI is temporarily unavailable, place the jars in `Libs/` and swap:
```kotlin
dependencies {
    // Comment out catalog references
    // implementation(libs.yukihook.core)
    // ksp(libs.yukihook.ksp)
    // implementation(libs.yukihook.prefs)

    implementation(files("Libs/yukihookapi-core.jar"))
    ksp(files("Libs/yukihookapi-ksp.jar"))
    implementation(files("Libs/yukihookapi-prefs.jar"))
}
```
Restore catalog usage once repos are back to avoid jar drift.

## Troubleshooting

### Common Issues

1. **Class not found: YukiHookAPI**
   - Ensure core dependency is present: `implementation(libs.yukihook.core)`
   - Verify Gradle sync completed without repository errors
   - If offline, confirm local fallback jars exist

2. **KSP not generating files**
   - Confirm `id("com.google.devtools.ksp")` is applied
   - Clean build: `./gradlew clean :app:assembleDebug`
   - Check generated sources under `build/generated/ksp/` for module

3. **Xposed module not loading**
   - Ensure `assets/xposed_init` (generated or manual) resolves to entry class
   - Module enabled in LSPosed scope for target process
   - Inspect LSPosed logs for class resolution errors

### Debugging

Enable debug logging in your application class:

```kotlin
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            YLog.Config().apply {
                isDebug = true
                isRecord = true
                elements(3)
                tag = "YukiHookAPI"
            }
        }
    }
}
```

## Best Practices

1. Always use the latest stable version of YukiHookAPI
2. Use the `@ModuleEntry` annotation for your main module class
3. Implement proper error handling for hooks
4. Use the preferences API for module settings
5. Follow the principle of least privilege when requesting permissions
6. Test thoroughly on different Android versions
7. Track one authoritative version: libs.versions.toml (avoid hardcoding in module scripts)
8. Use compileOnly for xposed-api ONLY
9. Avoid shading/packaging the Xposed API (will cause runtime conflicts)
10. Keep hook logic minimal and delegate heavy work to coroutine-friendly layers
11. Use preferences API (yukihook-prefs) for user-facing module settings

## Additional Resources

- [YukiHookAPI Documentation](https://highcapable.github.io/YukiHookAPI/)
- [GitHub Repository](https://github.com/HighCapable/YukiHookAPI)
- [Sample Module](https://github.com/HighCapable/YukiHookAPI-Sample-Module)
- [Telegram Group](https://t.me/YukiHookAPI)
