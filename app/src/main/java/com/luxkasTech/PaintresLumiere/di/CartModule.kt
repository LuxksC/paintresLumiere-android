package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.cart.CartRepository
import com.luxkasTech.PaintresLumiere.data.cart.CartStorage
import com.luxkasTech.PaintresLumiere.data.cart.LocalCartRepository
import com.luxkasTech.PaintresLumiere.data.cart.SharedPreferencesCartStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CartModule {

    /** Swap for the remote implementation once the cart endpoints exist (#44). */
    @Binds
    abstract fun bindCartRepository(impl: LocalCartRepository): CartRepository

    @Binds
    abstract fun bindCartStorage(impl: SharedPreferencesCartStorage): CartStorage
}
