package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.BuildConfig
import com.luxkasTech.PaintresLumiere.data.cart.CartApi
import com.luxkasTech.PaintresLumiere.data.cart.CartRepository
import com.luxkasTech.PaintresLumiere.data.cart.CartStorage
import com.luxkasTech.PaintresLumiere.data.cart.LocalCartRepository
import com.luxkasTech.PaintresLumiere.data.cart.RemoteCartRepository
import com.luxkasTech.PaintresLumiere.data.cart.SharedPreferencesCartStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CartModule {

    @Binds
    abstract fun bindCartStorage(impl: SharedPreferencesCartStorage): CartStorage

    companion object {
        @Provides
        @Singleton
        fun provideCartApi(retrofit: Retrofit): CartApi = retrofit.create(CartApi::class.java)

        /** `BuildConfig.REMOTE_CART` picks the implementation; both satisfy the same contract. */
        @Provides
        @Singleton
        fun provideCartRepository(
            local: Provider<LocalCartRepository>,
            remote: Provider<RemoteCartRepository>,
        ): CartRepository = if (BuildConfig.REMOTE_CART) remote.get() else local.get()
    }
}
