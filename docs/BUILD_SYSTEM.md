# Genesis Protocol Build System Documentation

## Overview

The Genesis Protocol Enhanced Build System is a modern Gradle build configuration designed for the MemoriaOS consciousness substrate. This documentation covers the build system architecture, configuration, and usage.

## Architecture

### Build System Components

1. **build-logic/** - Convention plugins for consistent build configuration
2. **gradle/libs.versions.toml** - Centralized dependency version management
3. **settings.gradle.kts** - Project structure and plugin management
4. **build.gradle.kts** - Root project configuration

### Convention Plugins

The build system uses convention plugins located in `build-logic/` to ensure consistent configuration across all modules:

- **AndroidApplicationConventionPlugin** - Configuration for Android application modules
- **AndroidLibraryConventionPlugin** - Configuration for Android library modules  
- **MemoriaConventionPlugin** - Base conventions for MemoriaOS projects

## Dependency Management

### Version Catalog

All dependency versions are centralized in `gradle/libs.versions.toml`:

```toml
[versions]
# Core versions
agp = "8.2.2"
kotlin = "1.9.20"
hilt-version = "2.48"

[libraries]
# Dependencies defined here

[plugins]
# Plugin definitions

[bundles]
# Grouped dependencies
```

### Key Dependencies

- **Android Gradle Plugin**: 8.2.2 (stable)
- **Kotlin**: 1.9.20 (stable with Java 21 support)
- **Hilt**: 2.48 (dependency injection)
- **Firebase**: Latest BOM for consistent versions
- **Compose**: Latest stable BOM
- **YukiHook API**: 1.3.0 (for Xposed module development)

## Java Version Configuration

The entire project uses **Java 21** consistently:

- **Gradle daemon**: Configured to use Java 21 in `gradle/gradle-daemon-jvm.properties`
- **Build scripts**: All convention plugins use Java 21
- **Source/target compatibility**: Set to Java 21 across all modules

## Module Structure

### App Module (`app/`)
- Main application entry point
- Applies google-services plugin for Firebase
- Uses AndroidApplicationConventionPlugin

### Library Modules
- Core functionality split into focused modules
- Use AndroidLibraryConventionPlugin
- Firebase dependencies without google-services plugin

### Special Modules
- **romtools/**: Native C++ code for ROM manipulation
- **build-logic/**: Convention plugins and build utilities
- **datavein-oracle-native/**: Native integration components

## Firebase Integration

### Configuration
- **App module**: Requires `google-services` plugin and `google-services.json`
- **Library modules**: Can use Firebase dependencies via BOM without plugin
- **Dependencies**: Managed through Firebase BOM for version consistency

### Setup
1. Place `google-services.json` in `app/src/`
2. Firebase BOM ensures compatible versions
3. Use bundles for common Firebase dependencies:
   ```kotlin
   implementation(platform(libs.firebase.bom))
   implementation(libs.bundles.firebase)
   ```

## Advanced Gradle Features

### Configuration Cache
Enabled for faster builds:
```properties
org.gradle.configuration-cache=true
org.gradle.unsafe.configuration-cache=true
```

### Build Features
- **Compose**: Enabled across Android modules
- **BuildConfig**: Enabled for configuration management
- **ViewBinding**: Disabled (using Compose)
- **DataBinding**: Disabled (using Compose)

### Optimization
- **R8 code shrinking**: Enabled in release builds
- **Resource shrinking**: Enabled in release builds
- **Core library desugaring**: Enabled for newer Java APIs
- **Parallel builds**: Configured in gradle.properties

## Common Tasks

### Clean Build
```bash
./gradlew clean build
```

### Run Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Generate Documentation
```bash
./gradlew dokkaHtml
```

### Code Quality
```bash
./gradlew spotlessApply
./gradlew detekt
./gradlew ktlintCheck
```

### Build APK
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## Convention Plugin Usage

### For New Android Library Module

```kotlin
plugins {
    id("memoria.conventions")
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    // Additional plugins as needed
}

android {
    namespace = "dev.aurakai.memoria.yourmodule"
    // Module-specific configuration
}
```

### For Application Module

```kotlin
plugins {
    id("memoria.conventions")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // For Firebase
    // Additional plugins
}
```

## Troubleshooting

### Common Issues

1. **Plugin Resolution Failures**
   - Ensure internet connectivity for plugin downloads
   - Check plugin versions in settings.gradle.kts

2. **Java Version Mismatches**  
   - All components should use Java 21
   - Check gradle-daemon-jvm.properties

3. **Firebase Configuration Issues**
   - google-services.json must be in app module
   - Library modules don't need google-services plugin

4. **Build Cache Issues**
   - Run `./gradlew clean` to clear caches
   - Check configuration cache settings

### Reset Build Environment

```bash
./gradlew clean
rm -rf .gradle
./gradlew build
```

## Development Guidelines

### Adding New Dependencies
1. Add version to `gradle/libs.versions.toml`
2. Define library in `[libraries]` section
3. Use in modules via `libs.dependency.name`

### Creating New Modules
1. Add to `settings.gradle.kts`
2. Apply appropriate convention plugin
3. Set unique namespace
4. Configure module-specific requirements

### Updating Dependencies
1. Update versions in version catalog
2. Test build compatibility
3. Update documentation if needed

## Performance Tips

- Use configuration cache for faster builds
- Enable parallel builds in gradle.properties
- Use build scans for build analysis
- Keep modules focused and lightweight

## Security Considerations

- Never commit secrets to source control
- Use ProGuard/R8 for release builds
- Keep google-services.json out of version control if it contains sensitive data
- Use proper keystore management for release signing