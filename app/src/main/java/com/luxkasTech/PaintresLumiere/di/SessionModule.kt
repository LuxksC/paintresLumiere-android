package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.core.session.AccessTokenProvider
import com.luxkasTech.PaintresLumiere.core.session.KeystoreTokenStorage
import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import com.luxkasTech.PaintresLumiere.core.session.TokenStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    abstract fun bindTokenStorage(impl: KeystoreTokenStorage): TokenStorage

    @Binds
    abstract fun bindAccessTokenProvider(impl: SessionManager): AccessTokenProvider
}
