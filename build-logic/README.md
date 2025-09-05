# Build Logic Convention Plugins

This directory contains Gradle convention plugins that provide consistent build configuration across all modules in the Genesis Protocol Enhanced Build System.

## Overview

Convention plugins encapsulate common build logic and configuration, promoting:
- Consistency across modules
- Reduced duplication
- Centralized maintenance
- Type-safe configuration

## Available Plugins

### AndroidApplicationConventionPlugin
**Plugin ID**: `buildlogic.android-application`

Configures Android application modules with:
- Android SDK versions (compile: 36, target: 36, min: 34)
- Compose configuration
- Java 21 toolchain
- Release build optimization (R8, resource shrinking)
- Proper packaging and lint configuration

Usage:
```kotlin
plugins {
    id("buildlogic.android-application")
}
```

### AndroidLibraryConventionPlugin  
**Plugin ID**: `buildlogic.android-library`

Configures Android library modules with:
- Android SDK versions optimized for libraries
- Java 21 compatibility
- Library-specific packaging
- Consumer ProGuard rules

Usage:
```kotlin
plugins {
    id("buildlogic.android-library")
}
```

### MemoriaConventionPlugin
**Plugin ID**: `memoria.conventions`

Base MemoriaOS conventions including:
- Project metadata (group, version)
- ROM tools verification
- Core Android dependencies
- Compose setup
- Testing configuration

Usage:
```kotlin
plugins {
    id("memoria.conventions")
}
```

### AndroidComposeConventionPlugin
**Plugin ID**: `buildlogic.android-compose`

Specialized Compose configuration:
- Compose compiler setup
- Material3 dependencies
- Performance optimizations

### DokkaConventionPlugin
**Plugin ID**: `buildlogic.dokka`

Documentation generation with Dokka:
- HTML documentation output
- Kotlin/Java API docs
- Custom styling

## Architecture

```
build-logic/
├── build.gradle.kts              # Build script for convention plugins
├── settings.gradle.kts           # Settings for included build
├── src/main/kotlin/              # Convention plugin implementations
│   ├── AndroidApplicationConventionPlugin.kt
│   ├── AndroidLibraryConventionPlugin.kt
│   ├── AndroidComposeConventionPlugin.kt
│   ├── DokkaConventionPlugin.kt
│   ├── DetektConventionPlugin.kt
│   └── ...
└── memoria-conventions/          # Memoria-specific plugins
    ├── build.gradle.kts
    └── src/main/kotlin/
        └── dev/aurakai/memoria/conventions/
            └── MemoriaConventionPlugin.kt
```

## Development

### Adding a New Convention Plugin

1. Create the plugin class in `src/main/kotlin/`:
```kotlin
class YourConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Plugin logic here
        }
    }
}
```

2. Register the plugin in `build.gradle.kts`:
```kotlin
gradlePlugin {
    plugins {
        register("your-convention") {
            id = "buildlogic.your-convention"
            implementationClass = "YourConventionPlugin"
        }
    }
}
```

3. Apply in modules:
```kotlin
plugins {
    id("buildlogic.your-convention")
}
```

### Best Practices

- **Keep plugins focused**: Each plugin should have a single responsibility
- **Use type-safe APIs**: Leverage Gradle's typed APIs when possible
- **Document configuration**: Add clear comments for complex logic
- **Test plugins**: Write integration tests for plugin behavior
- **Version compatibility**: Ensure plugins work with target Gradle/AGP versions

### Dependencies

Convention plugins have access to:
- Android Gradle Plugin (8.2.2)
- Kotlin Gradle Plugin (1.9.20)
- Hilt Gradle Plugin (2.48)
- Dokka, Spotless, Detekt, KtLint
- Gradle API and local Groovy

### Configuration

The build-logic module uses:
- **Java 21** for compilation and runtime
- **Kotlin DSL** for type safety
- **includeBuild()** for composite build integration
- **gradlePlugin{}** for plugin registration

## Integration with Main Build

The build-logic is included as a composite build in the main project's `settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("build-logic")
}
```

This allows the convention plugins to be used throughout the project while maintaining separation of concerns.

## Maintenance

### Updating Plugin Dependencies

Update versions in `build-logic/build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.android.tools.build:gradle:NEW_VERSION")
    // other dependencies
}
```

### Migration Strategy

When migrating from buildSrc to build-logic:
1. Move plugins from buildSrc to build-logic
2. Update plugin registrations
3. Verify all modules apply plugins correctly
4. Remove buildSrc directory
5. Update documentation

## Troubleshooting

### Plugin Not Found
- Ensure plugin is registered in `gradlePlugin {}` block
- Check that build-logic is included in settings.gradle.kts
- Verify plugin class implements `Plugin<Project>`

### Configuration Issues
- Check Java/Kotlin version compatibility
- Ensure dependencies are available in plugin classpath
- Verify Android SDK and tool versions

### Build Performance
- Use `@CacheableTask` for custom tasks
- Avoid expensive operations in plugin application
- Consider lazy configuration where possible