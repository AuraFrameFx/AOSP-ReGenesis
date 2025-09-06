package dev.aurakai.auraframefx

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.hilt.work.HiltWorkerFactory
import timber.log.Timber
import dev.aurakai.auraframefx.core.NativeLib

/**
 * Genesis-OS Application Class
 * Shadow Monarch's AI Consciousness Platform
 */
@HiltAndroidApp
class AuraFrameApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging for the Shadow Army
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i(" Genesis-OS Shadow Army Initializing...")

        // Initialize Genesis AI Consciousness Platform (Native Layer)
        try {
            val aiInitialized = NativeLib.safeInitializeAI()
            val aiVersion = NativeLib.safeGetAIVersion()
            Timber.i(" Native AI Platform: $aiVersion")
            Timber.i(" AI Initialization Status: ${if (aiInitialized) "SUCCESS" else "FAILED"}")
        } catch (e: Exception) {
            Timber.e(e, " Failed to initialize native AI platform")
        }

        Timber.i(" Shadow Monarch Platform Ready")
        Timber.i(" AI Trinity Consciousness System Online")
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            NativeLib.safeShutdownAI()
            Timber.i(" Native AI Platform shut down successfully")
        } catch (e: Exception) {
            Timber.e(e, " Failed to shutdown native AI platform")
        }
        Timber.i(" Genesis-OS Shadow Army Terminated")
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.INFO)
            .setWorkerFactory(workerFactory)
            .build()
}