/*
 * LSPosed and Yuki API conventions for Android modules
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    // id("org.lsposed.lsparanoid") // Plugin not available, commented out
}

// Apply common Kotlin conventions
apply(plugin = "buildlogic.kotlin-common-conventions")

// Configure Dokka for documentation
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(24)
            suppress.set(true)
            suppressInheritedMembers.set(true)
            skipEmptyPackages.set(true)

            // Add Android SDK documentation
            externalDocumentationLink {
                url.set(uri("https://developer.android.com/reference/").toURL())
                packageListUrl.set(uri("https://developer.android.com/reference/package-list").toURL())
            }

            // Add Yuki API documentation
            externalDocumentationLink {
                url.set(uri("https://fankes.github.io/YukiHookAPI/").toURL())
                packageListUrl.set(uri("https://fankes.github.io/YukiHookAPI/package-list").toURL())
            }
        }
    }
}
