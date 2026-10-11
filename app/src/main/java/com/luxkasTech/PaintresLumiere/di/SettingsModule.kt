package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.settings.LocalNotificationPreferences
import com.luxkasTech.PaintresLumiere.data.settings.NotificationPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds
    abstract fun bindNotificationPreferences(impl: LocalNotificationPreferences): NotificationPreferences
}
