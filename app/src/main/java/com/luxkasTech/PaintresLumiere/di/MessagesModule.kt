package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.core.messages.DefaultMessagesService
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MessagesModule {
    @Binds
    abstract fun bindMessagesService(impl: DefaultMessagesService): MessagesService
}
