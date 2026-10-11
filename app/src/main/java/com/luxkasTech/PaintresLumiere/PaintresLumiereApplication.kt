package com.luxkasTech.PaintresLumiere

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Owns the Hilt component graph — the Android counterpart of the
 * Swinject `DependencyContainer` composed in the iOS `AppDelegate`.
 */
@HiltAndroidApp
class PaintresLumiereApplication : Application()
