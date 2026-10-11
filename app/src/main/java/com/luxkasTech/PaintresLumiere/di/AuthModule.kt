package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.auth.AuthApi
import com.luxkasTech.PaintresLumiere.data.auth.AuthRepository
import com.luxkasTech.PaintresLumiere.data.auth.DefaultAuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    }
}
