package dev.aurakai.delegate

import android.app.Application

// Removed @HiltAndroidApp to avoid multiple annotated Application classes.
// This can remain as a simple base if needed, or be deleted if unused.
abstract class AuraKaiHiltApplication : Application() {
    // Optional shared logic
}
