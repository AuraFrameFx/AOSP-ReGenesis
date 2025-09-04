package dev.aurakai.delegate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
abstract class AuraKaiHiltApplication : Application() {
    // Add any application-level logic here, for example, overriding onCreate:
    // override fun onCreate() {
    //     super.onCreate()
    //     // Initialization code
    // }
}
