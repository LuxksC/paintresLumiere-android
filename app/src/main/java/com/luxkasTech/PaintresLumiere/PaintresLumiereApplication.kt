package com.luxkasTech.PaintresLumiere

import android.app.Application
import com.luxkasTech.PaintresLumiere.core.telemetry.CrashHandler
import com.luxkasTech.PaintresLumiere.core.telemetry.CrashReporter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point. Owns the Hilt component graph — the Android counterpart of the
 * Swinject `DependencyContainer` composed in the iOS `AppDelegate`.
 */
@HiltAndroidApp
class PaintresLumiereApplication : Application() {

    @Inject
    lateinit var crashReporter: CrashReporter

    override fun onCreate() {
        super.onCreate()
        CrashHandler.install(crashReporter)
    }
}
