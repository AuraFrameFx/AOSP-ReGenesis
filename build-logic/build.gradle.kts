// ==== GENESIS PROTOCOL - BUILD LOGIC ====
// Convention plugins for consistent build configuration

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Access to Android Gradle Plugin and Kotlin plugin APIs
    implementation("com.android.tools.build:gradle:8.2.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.48")
    
    // Other plugins  
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.10")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.23.3")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.4")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:12.0.3")
    
    // Development dependencies
    implementation(gradleApi())
    implementation(localGroovy())
}

gradlePlugin {
    plugins {
        register("android-application") {
            id = "buildlogic.android-application"
            implementationClass = "AndroidApplicationConventionPlugin"
            displayName = "Android Application Convention"
            description = "Convention plugin for Android application modules"
        }
        register("android-library") {
            id = "buildlogic.android-library"
            implementationClass = "AndroidLibraryConventionPlugin"
            displayName = "Android Library Convention"
            description = "Convention plugin for Android library modules"
        }
        register("android-compose") {
            id = "buildlogic.android-compose"
            implementationClass = "AndroidComposeConventionPlugin"
            displayName = "Android Compose Convention"
            description = "Convention plugin for Compose configuration"
        }
        register("dokka") {
            id = "buildlogic.dokka"
            implementationClass = "DokkaConventionPlugin"
            displayName = "Dokka Documentation Convention"
            description = "Convention plugin for Dokka documentation"
        }
        register("detekt") {
            id = "buildlogic.detekt"
            implementationClass = "DetektConventionPlugin"
            displayName = "Detekt Code Quality Convention"
            description = "Convention plugin for Detekt static analysis"
        }
    }
}
