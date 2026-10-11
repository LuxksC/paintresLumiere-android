package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.core.telemetry.Analytics
import com.luxkasTech.PaintresLumiere.core.telemetry.CrashReporter
import com.luxkasTech.PaintresLumiere.core.telemetry.LogcatTelemetry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Swap [LogcatTelemetry] for the Crashlytics/Sentry implementation once credentials exist. */
@Module
@InstallIn(SingletonComponent::class)
abstract class TelemetryModule {
    @Binds
    abstract fun bindCrashReporter(impl: LogcatTelemetry): CrashReporter

    @Binds
    abstract fun bindAnalytics(impl: LogcatTelemetry): Analytics
}
